package com.hci.geotagger.activities.common;

import android.app.Fragment;
import android.app.ProgressDialog;

/**
 * Class BaseFragment is used to create an options menu that will be used in all 
 * other fragments throughout the application. Any fragment that wants to use
 * this options menu should extend BaseFragment instead of Fragment. 
 * 
 * @author paulcushman
 */
public class BaseFragment extends Fragment {
	protected String TAG = "BaseFragment";

	// Generic progress dialog
	private ProgressDialog progressDialog = null;

	/**
	 * This method will setup the progress dialog. The input string will be displayed
	 * in the progress dialog.
	 * @param message The string to display in the progress dialog.
	 */
	protected void setupProgress(String message) {
		if (progressDialog != null) {
			progressDialog = null;
		}
		progressDialog = new ProgressDialog(this.getActivity());
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
