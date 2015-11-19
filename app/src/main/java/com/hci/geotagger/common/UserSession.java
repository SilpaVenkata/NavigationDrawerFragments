package com.hci.geotagger.common;

import java.util.Map;

import com.hci.geotagger.connectors.WebAPIConstants;
import com.hci.geotagger.objects.GeoLocation;
import com.hci.geotagger.objects.MyUserAccount;
import com.hci.geotagger.objects.UserAccount;

import android.content.SharedPreferences;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
/**
 * UserSession class keeps track of the current session. It 
 * knows if a user is logged in, and also caches the user account
 * object of the logged in user. 
 * 
 * Chris Loeschorn
 * Spring 2013
 */
public final class UserSession {
	
	public static boolean LOGGED_IN = false;
	public static UserAccount CURRENT_USER;
	public static long CURRENTUSER_ID;
	
	/**
	 * Set LoggedIn flag to true, cache current user account object
	 * and set shared preferences so the login is saved even if
	 * the application is killed or crashes
	 */
	public static void login(Context c, MyUserAccount account)
	{
		LOGGED_IN = true;
		CURRENT_USER = account;
		CURRENTUSER_ID = account.getId();
		
		//set shared preferences (private)
		SharedPreferences app_settings = c.getApplicationContext().getSharedPreferences(Constants.LOGIN_DATAFILE, Constants.MODE_PRIVATE);
		SharedPreferences.Editor editor = app_settings.edit();
		editor.putBoolean(Constants.KEY_LOGGEDIN, true);
		editor.putLong(Constants.KEY_UID, account.getId());
		editor.putString(Constants.KEY_PASS, account.getPass());
		editor.commit();
		Log.d("UserSession Login", "Shared Preferences Set.");
	}
	
	/**
	 * This method will return true if the refresh token is valid.
	 * otherwise a value of false is returned.
	 * @return true if we have a refresh token value
	 */
	public static boolean refreshTokenExists() {
		if (WebAPIConstants.REFRESH_TOKEN.isEmpty())
			return false;
		return true;
	}
	
	/**
	 * This static method will save the access_token and refresh_token values to the
	 * settings file, which can be retrieved when the application starts if not 
	 * after a logout.
	 * @param c this is the context to use
	 */
	public static void saveTokens(Context c) {
		SharedPreferences app_settings = c.getApplicationContext().getSharedPreferences(Constants.LOGIN_DATAFILE, Constants.MODE_PRIVATE);
		SharedPreferences.Editor editor = app_settings.edit();
		editor.putString(Constants.KEY_ACCESS_TOKEN, WebAPIConstants.ACCESS_TOKEN);
		editor.putString(Constants.KEY_REFRESH_TOKEN, WebAPIConstants.REFRESH_TOKEN);
		editor.commit();
	}

	/**
	 * This static method will retrieve the access_token and refresh_token that
	 * are saved in the settings file.
	 * @param c this is the context to use
	 */
	public static void retrieveTokens(Context c) {
		SharedPreferences app_settings = c.getApplicationContext().getSharedPreferences(Constants.LOGIN_DATAFILE, Constants.MODE_PRIVATE);
		WebAPIConstants.ACCESS_TOKEN = app_settings.getString(Constants.KEY_ACCESS_TOKEN, "");
		WebAPIConstants.REFRESH_TOKEN = app_settings.getString(Constants.KEY_REFRESH_TOKEN, "");
	}
	
	/**
	 * Logout method clears static fields, sets LoggedIn to false, and clears shared preferences
	 */
	public static void logout(Context c)
	{
		LOGGED_IN = false;
		CURRENT_USER = null;
		CURRENTUSER_ID = -1;
		WebAPIConstants.ACCESS_TOKEN = null;
		SharedPreferences app_settings = c.getApplicationContext().getSharedPreferences(Constants.LOGIN_DATAFILE, Constants.MODE_PRIVATE);
		app_settings.edit().clear().commit();
		
		Map<String,?> keys = app_settings.getAll();

		for(Map.Entry<String,?> entry : keys.entrySet()){
		            Log.d("map values",entry.getKey() + ": " + 
		                                   entry.getValue().toString());            
		 }
	}
	
}
