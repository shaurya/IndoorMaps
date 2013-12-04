package com.example.indoormaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/*
 * Generates and sends the REST methods for the app.
 * Currently only has POST, that takes in the JSON Array
 * with the data to post and the URL to post to.
 * Author: Shaurya Saluja
 */

public class RestMethods extends Activity {

	public static Handler UIHandler;
	
	static {
	    UIHandler = new Handler(Looper.getMainLooper());
	}
	public static void runOnUI(Runnable runnable) {
	    UIHandler.post(runnable);
	}
	
	public static void doPost(JSONArray jsonArray, String url, int i, final Context context) throws ClientProtocolException, IOException 
	{
		Log.i("Log", "Preparing Post Request");
		HttpParams httpParams = new BasicHttpParams();
		HttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpPost request = new HttpPost(url);
		Log.i("Log", "Post Url - " + url);
		StringEntity entity = new StringEntity(jsonArray.toString(), HTTP.UTF_8);
		if (i == 1) {
			try {
				entity = new StringEntity(jsonArray.getJSONObject(0).toString(), HTTP.UTF_8);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
		request.setEntity(entity);			
		request.addHeader("accept", "application/json");
		request.addHeader("x_username", "freecharge");
		request.addHeader("x_password", "x%r8Jdx2@");
		Log.i("Log", "Executing full POST Request");
		HttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity());
		Log.i("Log", "Response - " + response.getStatusLine());
		Log.i("Log", "Response - " + response.getStatusLine().getReasonPhrase());
		Log.i("Log", "Response Body- " + responseBody);
		if (responseBody.matches("200")) {	
			RestMethods.runOnUI(new Runnable() {
		        public void run() {
		           Toast.makeText(context, "Successfully Posted", Toast.LENGTH_LONG).show();
		        }
		    });
		} else {
			RestMethods.runOnUI(new Runnable() {
		        public void run() {
		           Toast.makeText(context, "Unsuccessfull Post", Toast.LENGTH_LONG).show();
		        }
		    });
		}
	}



	public static String doGet(String urlString) {

		HttpResponse response = null;
		try {        
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(urlString));
			response = client.execute(request);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		//return response;
		String res = "Nothing";
		try {
			res = convertStreamToString(response.getEntity().getContent());
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("Response", res);
		return res;
	}


	public static String convertStreamToString(InputStream inputStream) throws IOException {
		if (inputStream != null) {
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),1024);
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				inputStream.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}






	//		try {
	//			HttpURLConnection connection;
	//			URL url = null;
	//			String response = null;
	//			//String parameters = "param1=value1&param2=value2";
	//			url = new URL(urlString);
	//			//create the connection
	//			connection = (HttpURLConnection) url.openConnection();
	//			connection.setDoOutput(true);
	//			connection.setRequestProperty("Content-Type",
	//					"application/x-www-form-urlencoded");
	//			//set the request method to GET
	//			connection.setRequestMethod("GET");
	//			//get the output stream from the connection you created
	//			HttpRequest request = new OutputStreamWriter(connection.getOutputStream());
	//			//write your data to the ouputstream
	//			request.write(parameters);
	//			request.flush();
	//			request.close();
	//			String line = "";
	//			//create your inputsream
	//			InputStreamReader isr = new InputStreamReader(
	//					connection.getInputStream());
	//			//read in the data from input stream, this can be done a variety of ways
	//			BufferedReader reader = new BufferedReader(isr);
	//			StringBuilder sb = new StringBuilder();
	//			while ((line = reader.readLine()) != null) {
	//				sb.append(line + "\n");
	//			}
	//			//get the string version of the response data
	//			response = sb.toString();
	//			//do what you want with the data now
	//
	//			//always remember to close your input and output streams 
	//			isr.close();
	//			reader.close();
	//		} catch (IOException e) {
	//			Log.e("HTTP GET:", e.toString());
	//		}
}


