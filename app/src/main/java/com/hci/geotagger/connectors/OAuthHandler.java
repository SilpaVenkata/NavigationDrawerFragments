package com.hci.geotagger.connectors;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.hci.geotagger.R;
import com.hci.geotagger.common.Constants;

import android.content.Context;
import android.util.Log;

public class OAuthHandler {
	
	private String client_Secret;
	private String client_ID;
	private Context context;
	private JSONParser jsonParser;
	private String TAG = "OAthHandler";
	
	public OAuthHandler(Context context)
	{
		this.context = context;
		loadConsumerKeys();
	}

	public boolean hasAccessToken()
	{
	   return false;	
	}
	
	private void loadConsumerKeys()
	{
		try {
			Properties prop = new Properties();
			InputStream stream = context.getResources().openRawResource(R.raw.oauth);
			prop.load(stream);
			client_ID = (String)prop.getProperty("client_id");
			client_Secret = (String)prop.getProperty("client_secret");
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException("Can't load key from oauth.properties", e);
		}
	}
	
	public JSONObject getAccessToken(String uName, String password) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Log.d(TAG, "CLIENT_ID: " + client_ID);
        Log.d(TAG, "CLIENT_SECRET: " + client_Secret);
       
        String loginURL = WebAPIConstants.BASE_URL_OAUTH +"/token";
  
        params.add(new BasicNameValuePair("grant_type","password"));
        params.add(new BasicNameValuePair("username", uName));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("client_id", client_ID));
        params.add(new BasicNameValuePair("client_secret", client_Secret));
        
		try {
			JSONObject json = jsonParser.getJSONFromUrlRaw(loginURL, params);
			
			Log.d(TAG, "JSON Response from PHP: " + json.toString());
			return json;
		} catch (Exception ex) {
			Log.d(TAG, "Login: Exception occurred during login, returning null.");
			return null;
		}
    }
}
