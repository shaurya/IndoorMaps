package com.example.indoormaps;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
	int TRACKING_TIME = 60;
	Map<String, Integer> locationMap;
	Map<String, Integer> shopMap;
	Spinner spinner1;
	Spinner spinner2;
	Shop shops[] = null;
	Mall malls[] = null;

	public class Mall {

		public int locationid = 0;
		public String locationname = "";
		public String locationcity = "";

		// A simple constructor for populating our member variables for this tutorial.
		public Mall(int _locationid, String _locationname, String _locationcity)
		{
			locationid = _locationid;
			locationname = _locationname;
			locationcity = _locationcity;
		}

		public String toString()
		{
			return(locationname);
		}
	}

	public class Shop {

		public int shopid = 0;
		public String shopname = "";
		public int level = 0;
		public String locationid = "";
		//"shopid":77,"shopname":"Costa Coffee","level":0,"locationid":1
		// A simple constructor for populating our member variables for this tutorial.
		public Shop( int _shopid, String _shopname, int _level, String _locationid )
		{
			shopid = _shopid;
			shopname = _shopname;
			level = _level;
			locationid = _locationid;
		}

		public String toString()
		{
			return(shopname);
		}
	}




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (android.os.Build.VERSION.SDK_INT > 8) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		//RestMethods.doGet("http://ec2-54-201-114-123.us-west-2.compute.amazonaws.com:8080/fetchlocations");
		try {
			malls = createMallList( new JSONArray(RestMethods.doGet("http://ec2-54-201-114-123.us-west-2.compute.amazonaws.com:8080/fetchlocations")));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("Log", "created Mall List");

		spinner1 = (Spinner)findViewById(R.id.spinner1);
		ArrayAdapter<Mall> adapter = new ArrayAdapter<Mall> (MainActivity.this, android.R.layout.simple_spinner_item, malls);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter);

		Log.i("Log", "created mall spinner");
		//updateShopList();
		Log.i("Log", "created shop List");
		spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				updateShopList();
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		//		RestMethods.setContext(getApplication(), MainActivity.this);

	}

	public void updateShopList() {
		Mall currentMall = (Mall)spinner1.getSelectedItem();
		try {
			shops = createShopList( new JSONArray(RestMethods.doGet("http://ec2-54-201-114-123.us-west-2.compute.amazonaws.com:8080/fetchshops/" + currentMall.locationid)));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateShopSpinner();
	}

	public void updateShopSpinner() {
		spinner2 = (Spinner)findViewById(R.id.spinner2);
		ArrayAdapter<Shop> adapter2 = new ArrayAdapter<Shop> (MainActivity.this, android.R.layout.simple_spinner_item, shops);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter2);
		Log.i("Log", "created Shop Spinner");
		spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				updateShopId();
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		Log.i("Log", "Added shop spinner listner");
	}

	public void updateShopId() {
		Shop currentShop = (Shop)spinner2.getSelectedItem();
		shopNumber = currentShop.shopid;
		TextView idText = (TextView) findViewById(R.id.textView3);
		idText.setText(shopNumber.toString());
		Log.i("Log", "Updated Shop ID to " + shopNumber);
	}

	public Mall[] createMallList(JSONArray array) throws JSONException {
		Mall malls[] = new Mall[array.length()];
		for (int i = 0; i < array.length(); i++) {
			malls[i] = new Mall(array.getJSONObject(i).getInt("locationid"), array.getJSONObject(i).getString("locationname"), array.getJSONObject(i).getString("locationcity"));
		}
		return malls;
	}

	public Shop[] createShopList(JSONArray array) throws JSONException {
		Shop shops[] = new Shop[array.length()];
		for (int i = 0; i < array.length(); i++) {
			shops[i] = new Shop(array.getJSONObject(i).getInt("shopid"), array.getJSONObject(i).getString("shopname"), array.getJSONObject(i).getInt("level"), array.getJSONObject(i).getString("locationid"));
			// _shopid, String _shopname, int _level, String _locationid
		}
		return shops;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//	public void pushData(View view) {
	//		scanWifi();
	//	}

	public void push() {
		//		Button Button2 = (Button)findViewById(R.id.Button2);
		//		Button2.requestFocus();
		//		Button2.performClick();
	}

	/*
	 * Called to record the current scan of the network. Used for our initial
	 * record collection to create the preliminary data.
	 */
	public void pushData(View view) {
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
			new AddData().execute(shopNumber);
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
			//			Intent intent = new Intent(this, SubmitDataActivity.class);
			shopNumber = Integer.parseInt(n);
			Button Button2 = (Button)findViewById(R.id.Button01);
			Button2.requestFocus();
			Button2.performClick();
			//new AddData().execute(shopNumber);
			//			intent.putExtra("SHOP_NUMBER", shopNumber);
			//			addHotspots(wifiList, shopNumber);
			//			intent.putExtra(EXTRA_MESSAGE, submitted.toString());
			//			startActivity(intent);
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
				RestMethods.doPost(jsonArray, "http://ec2-54-201-114-123.us-west-2.compute.amazonaws.com:8080/indoor", 0, getApplicationContext());
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
			Toast.makeText(MainActivity.this, "Http Request Done", Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * Helper function to just show what information its is sending to the server
	 */
	public void addHotspots(List<ScanResult> wifiList, int shopNum) {
		Log.i("Log", "adding hotspots" + shopNum);
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
		cal.add(Calendar.SECOND, TRACKING_TIME);
		Log.d("Log", "Calender Set time:"+ cal.getTime());
		LocationIntent = new Intent(this, LocationService.class);
		pintent = PendingIntent.getService(this, 0, LocationIntent, 0);
		timer = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		timer.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), TRACKING_TIME*1000, pintent);
		startService(LocationIntent);
		Log.d("Log", "Started Location Service");
	}

	/*
	 * Stops tracking the user - stops the pending intent
	 * that sends the users current info to the database.
	 */
	public void stopLocating(View view) {
		stopService(LocationIntent);
		timer.cancel(pintent);   
		Log.i("Log", "Stopping Location Service");
		Toast.makeText(MainActivity.this, "Location Service Stopped", Toast.LENGTH_LONG).show();
	}

}
