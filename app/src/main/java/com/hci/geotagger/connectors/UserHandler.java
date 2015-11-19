/*
 * Account Handler class handles the calls to the webservice that deal
 * with user account operations, such as adding accounts, adding friends,
 * logging in, etc.
 */
package com.hci.geotagger.connectors;
 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.NetworkUtils;
import com.hci.geotagger.objects.MyUserAccount;
import com.hci.geotagger.objects.Tag;
import com.hci.geotagger.objects.UserAccount;

import android.content.Context;
import android.util.Log;

 
public class UserHandler extends GeotaggerHandler {
    private static final String TAG = "UserHandler";
    public static final String NAME = "UserHandler";
	protected String [] mActionsSupported = {
			WebAPIConstants.OP_EDIT_PROFILE
	};
    
	private OAuthHandler oauthHandler;
	public static JSONObject access;
    
    // constructor
    public UserHandler(Context context){
    	super(context, WebAPIConstants.LOGIN_URL);
        oauthHandler = new OAuthHandler(context);
    }
 
	/**
	 * Overridden method to process the actions supported by this handler. This function
	 * is called during the handling of cached actions.
	 */
	@Override
	public ReturnInfo performServerDbOperation(String operation, JSONObject params) {
		if (operation.equals(WebAPIConstants.OP_EDIT_PROFILE))
			return editToServerDB(params);

		return new ReturnInfo(ReturnInfo.FAIL_BADACTION);
	}

	public ArrayList<UserAccount> getUsers() {
		ArrayList<UserAccount> users = null;
		int curIndex = 0;
		final int maxusers = 20;
		
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			users = new ArrayList<UserAccount>();
			
			// Continue to get users from the server till all have been retrieved
			while (true) {
				JSONArray userData = getUsersFromServer(curIndex, maxusers);
				JSONObject obj;
				if (userData == null)
					break;
				
				for (int i = 0; i < userData.length(); i++) {
					obj = null;
					try {
						obj = userData.getJSONObject(i);
					} catch (JSONException e) {
						Log.d(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
		
					if (obj != null) {
						UserAccount a = AccountHandler.createAccountFromJSON(obj);
						users.add(a);
					}
				}
				
				if (userData.length() < maxusers)
					break;
				curIndex += userData.length();
			}
		}
		return users;
	}
	
	private JSONArray getUsersFromServer(int startIndex, int count) {
		String url = WebAPIConstants.BASE_URL_GTDB+WebAPIConstants.ACC_USERS;
		JSONArray resultsArray = null;
				
		 // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(WebAPIConstants.FILTER_OFFSET, Integer.toString(startIndex)));
		params.add(new BasicNameValuePair(WebAPIConstants.FILTER_LIMIT, Integer.toString(count)));

		try {
			 //make webservice call to get the list of users
			resultsArray = jsonParser.getJSONArrayForFriends_REST(url, params);
			if (resultsArray != null) {
				Log.d(TAG, "getUsersFromServer: JSON Response from PHP: " + resultsArray.toString());
			}
		} catch (Exception ex) {
			Log.d(TAG, "getUsersFromServer: Exception occurred getting friends, returning null.");
		}
		return resultsArray;
	}
	
	public ReturnInfo editUser(UserAccount user) {
		ReturnInfo dbresponse;
		Log.d(TAG, "Entering editUser");
		
		// Building Parameters
		JSONObject params = new JSONObject();
		JSONObject userParams = new JSONObject();
		
		try {
//			userParams.put("username", user.getuName());
			userParams.put("name", user.getName());
			userParams.put("email", user.getEmail());
			userParams.put("biography", user.getDescription());
			userParams.put("location", user.getLocation());
			userParams.put("quote", user.getQuote());
        
			params.put("user", userParams);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
 
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			dbresponse = editToServerDB(user.getId(), params);
			if (!dbresponse.success) {
				// TODO: if a failure then what?
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
				Log.d(TAG,"editTag: Failure");
			}
		} else {
			try {
				// ID Needed for the call to edit the tag later
				params.put("id", user.getId());
				// add the AddTag request to the cached list of DB transactions
				cache.cacheAction(NAME, WebAPIConstants.OP_EDIT_PROFILE, params);
				dbresponse = new ReturnInfo();
			} catch (Exception e) {
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
			}
		}
		Log.d(TAG, "Leaving editTag");
		return dbresponse;
	}
	
	private ReturnInfo editToServerDB(JSONObject params) {
		try {
			long userID = params.getLong("id");
			params.remove("id");
			return editToServerDB(userID, params);
		} catch (Exception e) {
			ReturnInfo response = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
			return response;
		}
	}

	public ReturnInfo editToServerDB(long userID, JSONObject params) {
		ReturnInfo result;
		
		String url = String.format(WebAPIConstants.ACC_FORMAT_EDITUSER, WebAPIConstants.BASE_URL_GTDB, userID);
		Log.d(TAG, "editToServerDB" + url);
		
		try {
			if (jsonParser.restPutCall(url, params)) {
				result = new ReturnInfo();
			} else {
				result = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
			}
		} catch (Exception ex) {
			Log.e(TAG, "editToServerDB: Exception occurred editing profile, returning null.");
			result = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		
		Log.d(TAG, "Leaving editToServerDB");
		return result;
	}


}
