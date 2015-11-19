package com.hci.geotagger.connectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.hci.geotagger.common.Constants;
import com.hci.geotagger.exceptions.UnknownErrorException;

import android.util.Log;

public final class ReturnInfo {
	public boolean success;					// Set to true if the operation was successful, false if a failure
	public int detail;						// detail of the failure, or 0
	public String url = null;				// Set if the connector function returns a URL
	public String exceptionString = null;	// Exception string
	public Object object = null;
	public String test = null;// If a record is returned, it is placed here
	
	public static final int SUCCESS = 0;
	public static final int FAIL_NONETWORK = -1;
	public static final int FAIL_GENERAL = -2;
	public static final int FAIL_UPLOAD = -3;
	public static final int FAIL_JSONERROR = -4;
	public static final int FAIL_NOCACHE = -5;
	public static final int FAIL_BADACTION = -6;
	
	/**
	 * Create a ReturnInfo object for a success case.  No URL is assigned.
	 */
	public ReturnInfo() {
		success = true;
		detail = SUCCESS;
	}
	
	/**
	 * Create a ReturnInfo object for a failure case.  The input code is associated with the failure.
	 * @param failureCode the detail value to assign to the ReturnInfo object
	 */
	public ReturnInfo(int failureCode) {
		success = (failureCode == SUCCESS);
		detail = failureCode;
	}
	public ReturnInfo(String gotIt) {
		this.test = gotIt;

	}
	/**
	 * @author Syed M Shah
	 * New Constructor for ReturnInfor for the new Database as it does not send the data for operation 
	 * success and failure 
	 */
	public ReturnInfo(JSONObject jsonResponse, int code) {
		if (jsonResponse != null) {
			try {
				Log.d("ReturnInfo", "Enter try.");
				String successString = Integer.toString(code);
				Log.d("ReturnInfo", "Post-Enter try.");

				if (successString != null) {
					int successCode = Integer.parseInt(successString);
					if (successCode == 1) {
						success = true;
						detail = SUCCESS;
					} else {
						success = false;
						detail = jsonResponse.getInt(Constants.ERROR);
					}
				} else {
					success = false;
					detail = FAIL_GENERAL;
				}
			} catch (JSONException e) {
				success = false;
				detail = FAIL_JSONERROR;
				exceptionString = e.getMessage();
				Log.d("ReturnInfo", "UnknownError Cause");
			}
		} else {
			success = false;
			detail = FAIL_GENERAL;
		}
		Log.d("ReturnInfo", "End of ReturnInfo.");
	}
	
	/**
	 * Create a ReturnInfo object based on a JSONObject, which is a response from a JSON request
	 * @param jsonResponse
	 */
	public ReturnInfo(JSONObject jsonResponse) {
		if (jsonResponse != null) {
			try {
				Log.d("ReturnInfo", "Enter try.");
				String successString = jsonResponse.getString(Constants.SUCCESS);
				Log.d("ReturnInfo", "Post-Enter try.");

				if (successString != null) {
					int successCode = Integer.parseInt(successString);
					if (successCode == 1) {
						success = true;
						Log.d("ReturnInfo SUCCESS:", "SET SUCCESS.");
						detail = SUCCESS;
					} else {
						success = false;
						Log.d("ReturnInfo FAILED:", "SET SUCCESS FAILED.");
						detail = jsonResponse.getInt(Constants.ERROR);
					}
				} else {
					success = false;
					detail = FAIL_GENERAL;
				}
			} catch (JSONException e) {
				success = false;
				detail = FAIL_JSONERROR;
				exceptionString = e.getMessage();
				Log.d("ReturnInfo", "UnknownError Cause");
			}
		} else {
			success = false;
			detail = FAIL_GENERAL;
		}
	}
	
	public String getMessage() {
		if (exceptionString != null)
			return exceptionString;
		else if (success)
			return "Success";
		else
			return "Unknown failure";
	}
	public void print(String tag) {
		if (success) {
			if (url != null)
				Log.d(tag, "Got response, img url = " + url);
			else
				Log.d(tag, "Got response, success");
		} else {
			Log.d(tag, "Got response, failure = " + detail);
		}
	}
}
