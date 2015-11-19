package com.hci.geotagger.activities.common;

import com.hci.geotagger.R;
import com.hci.geotagger.activities.HomeActivity;
import com.hci.geotagger.activities.LoginActivity;
import com.hci.geotagger.common.UserSession;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

/**
 * Class BaseActivity is used to create several capabilities that are consistent
 * will all of the activities of this application. Any activity that wants to use these
 * capabilities should extend BaseActivity instead of Activity. Current Activity capabilities
 * include the following:
 *   - Options menus
 *   - Progress dialog
 *   - Device specific Activity setup
 * This base class will create an options menu using the optionsMenuID variable. To change the 
 * options associated with the menu this variable should be overridden.  The progress dialog
 * methods allow the activity to setup, start and stop a progress dialog. This is helpful to
 * Activities that perform actions that need to restrict the user from navigating from the
 * Activity. This class also will restrict non-tablet based devices to only work in portrait
 * mode. 
 * 
 * @author Paul Cushman
 */
public class BaseActivity extends Activity {
	protected String TAG = "BaseActivity";

	// Override this with a menu if additional menu options are desired
	protected int optionsMenuID = R.menu.basic_options;
	
	// Generic progress dialog
	private ProgressDialog progressDialog = null;

	/**
	 * This method overrides the onCreate method. Currently this method will restrict the
	 * orientation of the device to portrait mode if it is not a tablet.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isTablet(this))
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	/**
	 * Setup the options for this activity
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(optionsMenuID, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.action_logout:
			//log out the user, then open the login screen
			UserSession.logout(this);
			
			Intent i = new Intent(getBaseContext(), LoginActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
						Intent.FLAG_ACTIVITY_CLEAR_TASK |
						Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			finish();
			return true;
		case R.id.action_home:
			Intent homeIntent = new Intent(getBaseContext(), HomeActivity.class); 
			startActivity(homeIntent);
			finish();
		}
		
		return super.onOptionsItemSelected(item);
	}

	/**
	 * This method will identify if the current device is a tablet device. If the device
	 * has a screenlayout that is equal to or larger than the large screenlayout then this
	 * is considered a tablet and a true value is returned.
	 * @param context The context needed to get the needed device information
	 * @return true if this is a tablet, false if not.
	 */
	public static boolean isTablet(Context context) {
		Boolean isTablet = (context.getResources().getConfiguration().screenLayout & 
				Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
		if(isTablet) {
			Log.d("BaseActivity", "Is Tablet Device");
		} else {
			Log.d("BaseActivity", "Is Phone Device");
		}
		return isTablet;
	}

	/**
	 * This method will setup the progress dialog. The input string will be displayed
	 * in the progress dialog.
	 * @param message The string to display in the progress dialog.
	 */
	protected void setupProgress(String message) {
		if (progressDialog != null) {
			progressDialog = null;
		}
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(message);
		progressDialog.setCancelable(false);
		progressDialog.setIndeterminate(true);
	}
	
	/**
	 * This method will make the progress dialog visible by calling the dialog show method.
	 * @return Returns true if the dialog is shown, false if the dialog was not setup.
	 */
	protected boolean showProgress() {
		if (progressDialog == null)
			return false;
		progressDialog.show();
		return true;
	}
	
	/**
	 * This method will stop the display of the progress dialog.
	 */
	protected void stopProgress() {
		if (progressDialog != null)
			progressDialog.dismiss();
	}
}
