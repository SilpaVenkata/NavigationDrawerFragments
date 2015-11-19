package com.hci.geotagger.cache;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

/**
 * This class supports the data necessary to perform the caching post action
 * operations. Java has constructs for pairs of values, this class will support
 * the action, id and value tuples.
 * 
 * @author Paul Cushman
 */
public class CachePostAction {
	static private final String TAG = "CachePostAction";
	
	static public final String ARRAY = "postAction";
	static public final String ACTION = "action";
	static public final String ID = "id";
	static public final String VALUE = "value";
	
	String action;
	String fieldID;
	String fieldValue;
	
	/**
	 * Constructor for a String based value.
	 * @param action The post action to be performed
	 * @param id ID associated with the action
	 * @param value Value associated with the action
	 */
	public CachePostAction(String action, String id, String value) {
		this.action = action;
		this.fieldID = id;
		this.fieldValue = value;
	}
	
	/**
	 * Constructor for an Integer based value.
	 * @param action The post action to be performed
	 * @param id ID associated with the action
	 * @param value Value associated with the action
	 */
	public CachePostAction(String action, String id, long value) {
		this.action = action;
		this.fieldID = id;
		this.fieldValue = Long.toString(value);
	}
	
	/**
	 * This static method will convert the input List<NameValuePair> to a JSONObject
	 * and then convert the JSONObject to a string.  This string can be used to 
	 * save the database requests for caching.
	 * @param param The List<NameValuePair> object to convert
	 * @return The string representation of the input param
	 */
	static public String toString(List<CachePostAction> param) {
		if (param.size() <= 0)
			return null;
		
		JSONObject json;

		JSONArray array = new JSONArray();
		
		for (int i=0; i<param.size(); i++) {
			CachePostAction cpa = param.get(i);
			json = new JSONObject();
			
			try {
				json.put(ACTION, cpa.action);
				json.put(ID, cpa.fieldID);
				json.put(VALUE, cpa.fieldValue);
				
				array.put(json);
				
			} catch (Exception e) {
				Log.e(TAG, "Error during JSON Put");
			}
		}
		
		JSONObject postAction = new JSONObject();
		try {
			postAction.put(ARRAY, array);
		} catch (Exception e) {
			Log.e(TAG, "Error during JSON Put");
			return null;
		}
		
		String retString = postAction.toString();
		return retString;
	}

}
