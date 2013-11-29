package com.example.indoormaps;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public final static String EXTRA_MESSAGE = "com.example.indoormaps.MESSAGE";
	public Integer shopNumber;
	TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	StringBuilder sb = new StringBuilder();
	StringBuilder submitted = new StringBuilder();
	Intent LocationIntent;
	PendingIntent pintent;
	AlarmManager timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * Called to record the current scan of the network. Used for our initial
	 * record collection to create the preliminary data.
	 */
	public void scanWifi(View view) {
		mainText = (TextView) findViewById(R.id.mainText);
		mainText.setText("\n\nStarting Scan...\n\n");
		mainText.setMovementMethod(new ScrollingMovementMethod());
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (mainWifi.isWifiEnabled() == false) {  								// If wifi disabled then enable it
			Toast.makeText(getApplicationContext(), "wifi is disabled..enabling it now!", Toast.LENGTH_LONG).show();
			mainWifi.setWifiEnabled(true);
		}
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		mainWifi.startScan();
	}

	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			sb = new StringBuilder();
			wifiList = mainWifi.getScanResults();
			sb.append("\n      Number Of Wifi connections :"+wifiList.size()+"\n\n");
			submitted.append("\nNumber Of Wifi connections :"+wifiList.size()+"\n\n");
			for(int i = 1; i <= wifiList.size(); i++){
				sb.append(Integer.valueOf(i).toString() + ".");
				sb.append((wifiList.get(i-1)).toString());
				sb.append("\n\n");
			}
			unregisterReceiver(receiverWifi);
			mainText.setText(sb);
		}
	}


	/*
	 * Called when the user clicks the Save Data button. Starts the asynchronous method
	 * to spawn separate thread to submit data to server through HTTP post
	 */
	public void sendMessage(View view) {
		EditText shopNum = (EditText) findViewById(R.id.shop_number);
		String n = shopNum.getText().toString();
		if (n.matches("")) {
			Toast.makeText(this, "You did not enter a shop number", Toast.LENGTH_SHORT).show();
			return;
		} else {
			Intent intent = new Intent(this, SubmitDataActivity.class);
			shopNumber = Integer.parseInt(n);
			new AddData().execute(shopNumber);
			intent.putExtra("SHOP_NUMBER", shopNumber);
			addHotspots(wifiList, shopNumber);
			intent.putExtra(EXTRA_MESSAGE, submitted.toString());
			startActivity(intent);
		}
	}

	/*
	 * Asynchronously puts the data into a JSON Array and then on a separate
	 * thread submits it to the Rest Methods to post it to the server.
	 * Creates Toast on completion.
	 */
	private class AddData extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer...shopNum) {
			JSONArray jsonArray = new JSONArray();
			for(int i = 0; i < wifiList.size(); i++){
				JSONObject hotspot = new JSONObject();
				try {
					hotspot.put("shopNo", shopNum[0]);
					hotspot.put("bssId", wifiList.get(i).BSSID);
					hotspot.put("ssId", wifiList.get(i).SSID);
					hotspot.put("caps", wifiList.get(i).capabilities);
					hotspot.put("level", wifiList.get(i).level);
				} catch (JSONException e) {
					e.printStackTrace();
					Log.i("JSON Put Error: ", e.toString());
				}
				jsonArray.put(hotspot);
				Log.i("Log", "Hotspot into jsonArray - " + hotspot.toString()); 
			}
			try {
				Log.i("Log", "Submitting Post Request");
				RestMethods.doPost(jsonArray, "http://ec2-54-201-114-123.us-west-2.compute.amazonaws.com:8080/indoor");
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				Log.i("Post Error: ", e.toString());
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("Post Error: ", e.toString());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void x) {
			Toast.makeText(MainActivity.this, "Sent Http Request", Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * Helper function to just show what information its is sending to the server
	 */
	public void addHotspots(List<ScanResult> wifiList, int shopNum) {
		for(int i = 0; i < wifiList.size(); i++){
			String log = wifiList.get(i).BSSID + "  ->  " + wifiList.get(i).level;
			submitted.append(log);
			submitted.append("\n\n");
			Log.i("Inserting: ", log); 
		}
	}

	/*
	 * Starts tracking the user - creates a pending intent which is
	 * triggered every 30 seconds, sending the current info to the database
	 */
	public void startLocating(View view) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.SECOND, 30);
		Log.d("Log", "Calender Set time:"+ cal.getTime());
		LocationIntent = new Intent(this, LocationService.class);
		pintent = PendingIntent.getService(this, 0, LocationIntent, 0);
		timer = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		timer.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30*1000, pintent);
		startService(LocationIntent);
		Log.d("Log", "Started Location Service");
	}

	/*
	 * Stops tracking the user - stops the pending intent
	 * that sends the suers current info to the database.
	 */
	public void stopLocating(View view) {
		stopService(LocationIntent);
		timer.cancel(pintent);   
		Log.i("Log", "Stopping Location Service");
		Toast.makeText(MainActivity.this, "Location Service Stopped", Toast.LENGTH_LONG).show();
	}

}
