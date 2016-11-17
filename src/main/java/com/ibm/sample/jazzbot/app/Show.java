package com.ibm.sample.jazzbot.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet("/show")
public class Show extends HttpServlet {
    private static final long serialVersionUID = 1L;
	
	 @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
		 String stocksymbol = request.getParameter("text");
		 JsonObject output = fetchStockData(stocksymbol);
    	
    	response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		out.println(output);
		
		out.close();
    }

	public JsonObject fetchStockData(String symbol) throws IOException {
		
		JsonObject stockJson = new JsonObject();
		
		String url = "https://query.yahooapis.com/v1/public/yql";
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("q", "select * from yahoo.finance.quotes where symbol = \"" + symbol +"\""));
		urlParameters.add(new BasicNameValuePair("format", "json"));
		urlParameters.add(new BasicNameValuePair("env", "store://datatables.org/alltableswithkeys"));
		
		post.setEntity(new UrlEncodedFormEntity(urlParameters));

		HttpResponse resp = client.execute(post);
		
		BufferedReader rd = new BufferedReader(
		        new InputStreamReader(resp.getEntity().getContent()));

		String result = new String();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result += line;
		}
		JsonParser parser = new JsonParser();
		JsonObject output = parser.parse(result).getAsJsonObject();
		
		JsonObject stockInfo = output.get("query").getAsJsonObject().get("results").getAsJsonObject().get("quote").getAsJsonObject();
		System.out.println(stockInfo);
		stockJson.addProperty("name", stockInfo.get("Name").getAsString());
		stockJson.addProperty("symbol", stockInfo.get("symbol").getAsString());
		stockJson.addProperty("price", stockInfo.get("LastTradePriceOnly").isJsonNull()?"N/A":stockInfo.get("LastTradePriceOnly").getAsString());
		stockJson.addProperty("change", stockInfo.get("Change").isJsonNull()?"N/A":stockInfo.get("Change").getAsString());
		stockJson.addProperty("high", stockInfo.get("DaysHigh").isJsonNull()?"N/A":stockInfo.get("DaysHigh").getAsString());
		stockJson.addProperty("low", stockInfo.get("DaysLow").isJsonNull()?"N/A":stockInfo.get("DaysLow").getAsString());
		stockJson.addProperty("volume", stockInfo.get("Volume").isJsonNull()?"N/A":stockInfo.get("Volume").getAsString());
		
		return stockJson;
	}
}
