/*
 * Alert Handler Class- Reusable class to quickly show alert dialogs
 * http://developer.android.com/guide/topics/ui/dialogs.html#AlertDialog
 * Chris Loeschorn 
 * Spring 2013
 */

package com.hci.geotagger.gui;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.hci.geotagger.R;

public class AlertHandler {
	
	/**
	 * Show an alert dialog with a message and an OK button
	 * @in c: Context
	 * @in title: String
	 * @in message: String
	 */
	public void showAlert(Context c, String title, String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setMessage(message);
		if (title != null) { builder.setTitle(title); }
		
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               dialog.dismiss();
	           }
	       });
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	/**
	 * Shows an alert indicating GPS is disabled
     * Settings button brings user to device GPS settings to enable it.
	 * @param c
	 */
	
	public void showGpsDisabledAlert(final Context c)
	{
		 AlertDialog.Builder builder = new AlertDialog.Builder(c);
		    builder.setMessage("GPS is currently disabled. Would you like to enable it?")
		           .setCancelable(false)
		           .setPositiveButton("GPS Settings", new DialogInterface.OnClickListener() {
		               public void onClick(final DialogInterface dialog, final int id) {
		                   c.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		               }
		           })
		           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		               public void onClick(final DialogInterface dialog, final int id) {
		                    dialog.cancel();
		               }
		           });
		    final AlertDialog alert = builder.create();
		    alert.show();
	}
		
}

