package com.hci.geotagger.connectors;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class NameValuePairList {

	public static long getLong(List<NameValuePair> params, String key) {
		long retVal = 0;
		for (NameValuePair nvp : params) {
			if (nvp.getName().equals(key)) {
				String value = nvp.getValue();
				retVal = Long.parseLong(value);
				break;
			}
		}
		return retVal;
	}
	
	public static int getInt(List<NameValuePair> params, String key) {
		int retVal = 0;
		for (NameValuePair nvp : params) {
			if (nvp.getName().equals(key)) {
				String value = nvp.getValue();
				retVal = Integer.parseInt(value);
				break;
			}
		}
		return retVal;
	}
	
	public static String getString(List<NameValuePair> params, String key) {
		String retVal = "";
		for (NameValuePair nvp : params) {
			if (nvp.getName().equals(key)) {
				retVal = nvp.getValue();
				break;
			}
		}
		return retVal;
	}
	
	public static List<NameValuePair> removeEntry(List<NameValuePair> params, String key) {
        List<NameValuePair> newList = new ArrayList<NameValuePair>();
		for (NameValuePair nvp : params) {
			if (!nvp.getName().equals(key)) {
				newList.add(nvp);
			}
		}
        return newList;
	}
	
	public static JSONObject toJSONObject(List<NameValuePair> params, String name) {
		JSONObject jsonparams = new JSONObject();
		JSONObject cmt = new JSONObject();

		// Convert the list to a json object
		try {
			for (NameValuePair nvp : params) {
					cmt.put(nvp.getName(), nvp.getValue());
			}
			if (name != null)
				jsonparams.put(name, cmt);
			else
				jsonparams = cmt;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return jsonparams;
	}

}
