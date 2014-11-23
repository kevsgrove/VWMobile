package com.vw.mobile;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.vw.mobile.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{
	private static String myTagId="-";
	private static String biteTagId="-";
	public static String URL = ("http://192.168.0.109:8080/ServletTest/Bite"); 
	static Button button1, button2; 
	static TextView outputText1, outputText2;
	private static NfcAdapter mNfcAdapter;
	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	
	private void handleIntent(Intent intent){
		 String action = intent.getAction();
		 if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
		         
			 String type = intent.getType();
			 Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			 
			 if(myTagId=="-"){
				 myTagId=byteArrayToString(tag.getId());
			 	 Toast.makeText(this, "Your id set to: "+myTagId, Toast.LENGTH_LONG).show();
		 	 }
			 else{
				 biteTagId=byteArrayToString(tag.getId());
				 GetXMLTask task = new GetXMLTask();
				 Toast.makeText(this, "You bit id: "+biteTagId, Toast.LENGTH_LONG).show();
				 task.execute(new String[] { URL +"?param1="+myTagId+"&param2="+biteTagId});
			 }
		 }
	}
	
	@Override
    protected void onNewIntent(Intent intent) { 
		handleIntent(intent);
	}
	
	
	
	private String byteArrayToString(byte[] ba){
		
		char[] hexChars=new char[ba.length*2];
		int v;
	    for ( int j = 0; j < ba.length; j++ ) {
	        v = ba[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
		return new String(hexChars);
	}
	
	
	private void findViewsById() { 
	    outputText1 = (TextView) findViewById(R.id.textView1);
	    outputText2 = (TextView) findViewById(R.id.textView2);

	}
	
	private class GetXMLTask extends AsyncTask<String, Void, String> {
	    @Override
	    protected String doInBackground(String... urls) {
	        String output = null;
	        for (String url : urls) {
	            output = getOutputFromUrl(url);
	        }
	        return output;
	    }
	    private String getOutputFromUrl(String url) {
	        StringBuffer output = new StringBuffer("");
	        try {
	            InputStream stream = getHttpConnection(url);
	            BufferedReader buffer = new BufferedReader(
	                    new InputStreamReader(stream));
	            String s = "";
	            while ((s = buffer.readLine()) != null)
	                output.append(s);
	        } catch (IOException e1) {
	            e1.printStackTrace();
	        }
	        return output.toString();
	    }

	    private InputStream getHttpConnection(String urlString)
	            throws IOException {
	        InputStream stream = null;
	        URL url = new URL(urlString);
	        URLConnection connection = url.openConnection();
	        try {
	            HttpURLConnection httpConnection = (HttpURLConnection) connection;
	            httpConnection.setRequestMethod("GET");
	            httpConnection.connect();

	            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
	                stream = httpConnection.getInputStream();
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return stream;
	    }
	    @Override
	    protected void onPostExecute(String output) {
	    	
	    	String[] lines = output.split("-", 2);
	        outputText1.setText("Your ID: "+lines[0]);
	        outputText2.setText("Last ID bitten: "+lines[1]);
	    }
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
 
        }
     
           
		findViewsById();
	    handleIntent(getIntent());
	}
	
	public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

	
	public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
		final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
	    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	 
	    final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
	 
	    IntentFilter[] filters = new IntentFilter[1];
	    String[][] techList = new String[][]{};
	    filters[0] = new IntentFilter();
	    filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
	    filters[0].addCategory(Intent.CATEGORY_DEFAULT);
	    try {
	        filters[0].addDataType("text/plain");
	    } catch (MalformedMimeTypeException e) {
	        throw new RuntimeException("Check your mime type.");
	    }
	        
	    adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
	}
	 
	
	@Override
	protected void onResume(){
		super.onResume();
		setupForegroundDispatch(this, mNfcAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
