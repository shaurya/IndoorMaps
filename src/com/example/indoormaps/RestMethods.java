package com.example.indoormaps;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
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
import android.util.Log;

/*
 * Generates and sends the REST methods for the app.
 * Currently only has POST, that takes in the JSON Array
 * with the data to post and the URL to post to.
 * Author: Shaurya Saluja
 */

public class RestMethods extends Activity {

	public static void doPost(JSONArray jsonArray, String url, int i) throws ClientProtocolException, IOException 
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
		//Log.i("Log", "Response Body- " + responseBody);
	}
}
