package com.hci.geotagger.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Class detects whether network connectivity is present
 * 
 * @author Paul Cushman
 *
 */
public class NetworkUtils {
	
	/**
	 * Static method used extensively in application to determine if network communication is available or not. If
	 * not, often locally cached values are used.
	 * @param context Android context to use for system service call.
	 * @return returns boolean value indicating whether network is up or not
	 */
	public static boolean isNetworkUp(Context context) {
		ConnectivityManager conMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = conMgr.getActiveNetworkInfo();
		if (ni != null) {
			if (ni.isAvailable() && ni.isConnected())
				return true;
		}
		return false;
	}

}