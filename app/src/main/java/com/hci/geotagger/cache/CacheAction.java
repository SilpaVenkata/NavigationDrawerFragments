package com.hci.geotagger.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * The CacheAction class is used to maintain information about Action caching.
 * Specifically, information associated with each cached action.
 * @author Paul Cushman
 *
 */
public class CacheAction {
	private static final String TAG = "CacheAction";
	String actionParams;
	public String postActions;
	
	long id;
	String object;
	String handler;
	String operation;
	
	JSONObject json;
	
	public final static String CA_OPERATION = "operation";
	public final static String CA_HANDLER = "handler";
	public final static String CA_OBJECT = "object";
	private final static String CA_OBJECT_DEFAULT = "params";
	private final static String CA_KEY = "key";
	public final static String CA_KEY_ID = "id";

	public final static int UNKNOWN_ID = -1;
	
	/**
	 * Constructor for the CacheAction class
	 * @param handlername Identifier of the Handler to use for the action
	 * @param operation The operation supported by the handler
	 * @param params Parameters to the handler's action
	 */
	public CacheAction(String handlername, String action, JSONObject jsonParams) {
		handler = handlername;
		operation = action;
		
		json = new JSONObject();

		// Convert the list to a json object
		try {
			json.put(CA_HANDLER, handlername);
			json.put(CA_OPERATION, operation);
			json.put(CA_OBJECT, CA_OBJECT_DEFAULT);
			
			json.put(CA_OBJECT_DEFAULT, jsonParams);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		actionParams = json.toString();
		id = UNKNOWN_ID;
		postActions = null;
	}

	/**
	 * Constructor to create a CacheAction with just the Handler name and the
	 * action to be performed.  Good for actions that have no arguments.
	 * @param handlername
	 * @param action
	 */
	public CacheAction(String handlername, String action) {
		handler = handlername;
		operation = action;

		json = new JSONObject();

		// Convert the list to a json object
		try {
			json.put(CA_HANDLER, handlername);
			json.put(CA_OPERATION, action);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		actionParams = json.toString();
		id = UNKNOWN_ID;
		postActions = null;
	}

	/**
	 * This constructor creates a CacheAction object using the input json string
	 * @param jsonString
	 * @param id
	 */
	public CacheAction(String jsonString, long id) {
		this.id = id;
		
		JSONObject json;
		try {
			json = new JSONObject(jsonString);

			if (json.has(CA_HANDLER))
				handler = json.getString(CA_HANDLER);
			else
				handler = null;
			
			if (json.has(CA_OPERATION))
				operation = json.getString(CA_OPERATION);
			else
				operation = null;
		} catch (Exception e) {
			Log.d(TAG, "Error parsing string to JSONObject");
		}
		
		actionParams = jsonString;
	}
	
	/**
	 * This method will add a 'long' key value to the current CacheAction object
	 * @param key
	 * @param id
	 */
	public void addKey(String key, long id) {
		JSONObject jsonkey;
		
		try {
			if (json.has(CA_KEY)) {
				jsonkey = json.getJSONObject(CA_KEY);
				json.remove(CA_KEY);
			} else {
				jsonkey = new JSONObject();
			}
			
			if (jsonkey.has(key)) {
				jsonkey.remove(key);
			}
			
			jsonkey.put(key, id);
			json.put(CA_KEY, jsonkey);
			
			actionParams = json.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public long getKeyLong(String key) {
		long retvalue = -1;
		try {
			if (json.has(CA_KEY)) {
				JSONObject jsonkey = json.getJSONObject(CA_KEY);
				if (jsonkey.has(key)) {
					retvalue = jsonkey.getLong(key);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retvalue;
	}
	
	/**
	 * This method will replace the params part of the CacheAction with the 
	 * input params list.
	 * @param params
	 * @return true if successful, false if a failure
	 */
	public boolean update(JSONObject jsonParams) {
		String objectName;
		JSONObject newjson = new JSONObject();

		// Convert the list to a json object
		try {
			if (json == null) {
				json = new JSONObject(actionParams);
			}
			
			if (json.has(CA_OBJECT))
				objectName = json.getString(CA_OBJECT);
			else
				objectName = CA_OBJECT_DEFAULT;
			
			newjson.put(CA_HANDLER, handler);
			newjson.put(CA_OPERATION, operation);
			newjson.put(CA_OBJECT, objectName);
			newjson.put(objectName, jsonParams);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		json = newjson;
		actionParams = json.toString();
		return true;
	}


	/**
	 * This method will print the parameters associated with the cached action object
	 */
	public void print() {
		Log.d(TAG, "Params="+actionParams);
	}

	/**
	 * This static method will convert the input List<NameValuePair> to a JSONObject
	 * and then convert the JSONObject to a string.  This string can be used to 
	 * save the database requests for caching.
	 * @param param The List<NameValuePair> object to convert
	 * @return The string representation of the input param
	 */
	static public String toStringOLD(List<NameValuePair> param) {
		if (param.size() <= 0)
			return null;
		
		JSONObject json;
		
		json = new JSONObject();
		
		for (int i=0; i<param.size(); i++) {
			NameValuePair nvp = param.get(i);
			
			try {
				json.put(nvp.getName(), nvp.getValue());
			} catch (Exception e) {
				Log.e(TAG, "Error during JSON Put");
			}
		}
		
		return json.toString();
	}
	
	/**
	 * This method will return the List<NameValuePair> parameter saved by
	 * this CacheAction object.
	 * @return
	 */
	public JSONObject getArray() {
		return getArray(actionParams);
	}
	
	/**
	 * This method will return the List<NameValuePair> value for the Post Actions
	 * that are set for this Action Cache.
	 * @return The List<NameValuePair> for the post action
	 */
	public List<CachePostAction> getPostArray() {
		if (postActions == null)
			return null;
		return getPostAction(postActions);
	}
	
	/**
	 * This static method will convert the input JSON string into the associated
	 * List<NameValuePair> object.  The string should have been formed by a
	 * previous call to the CacheAction.toString call, or by the constructor
	 * method of this class. 
	 * @param jsonString The input JSON string
	 * @return The converted List<NameValuePair> object
	 */
	static public JSONObject getArray(String jsonString) {
		JSONObject json;
		JSONObject params = null;
		try {
			json = new JSONObject(jsonString);
		} catch (Exception e) {
			Log.d(TAG, "Error parsing string to JSONObject");
			return null;
		}

		if (json.has(CA_OBJECT)) {
			try {
				String objectName = json.getString(CA_OBJECT);			
				params = json.getJSONObject(objectName);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
		}
		
        return params;
	}

	

	/**
	 * This static method will convert the input JSON string into the associated
	 * List<NameValuePair> object.  The string should have been formed by a
	 * previous call to the CacheAction.toString call, or by the constructor
	 * method of this class. 
	 * @param jsonString The input JSON string
	 * @return The converted List<CachePostAction> object
	 */
	static public List<CachePostAction> getPostAction(String jsonString) {
		JSONObject json;
		JSONArray array;
		try {
			json = new JSONObject(jsonString);
			
			array = json.getJSONArray(CachePostAction.ARRAY);
			if (array == null)
				return null;
		} catch (Exception e) {
			Log.d(TAG, "Error parsing string to JSONObject");
			return null;
		}

		if (json == null)
			return null;
		
        List<CachePostAction> params = new ArrayList<CachePostAction>();
		
		for (int i=0; i<array.length(); i++) {
			try {
				JSONObject entry = array.getJSONObject(i);
				String action = entry.getString(CachePostAction.ACTION);
				String id = entry.getString(CachePostAction.ID);
				String value = entry.getString(CachePostAction.VALUE);
				
		        params.add(new CachePostAction(action, id, value));
				Log.d(TAG, "action="+action+", id="+id+", value="+value);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
				
        return params;
	}

}
