package com.hci.geotagger.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.common.BaseListActivity;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.gui.GenericEntryListAdapter;
import com.hci.geotagger.objects.Adventure;

import java.util.ArrayList;

/**
 * Adventurelist activity displays a scrollable list of adventures for 
 * a given user account and gives the user the ability
 * to click on them to view the adventure. Currently, the ability to add
 * a new adventure has been temporarily disabled
 */

public class AdventureListActivity extends BaseListActivity implements DbMessageResponseInterface {
	private static final String TAG = "AdventureListActivity";

	private ArrayList<Adventure> adventures = null;
	private GenericEntryListAdapter listAdapter;
	private Long userID;
	private int CONTEXT_DELETE_ID = 1;
	private int CONTEXT_ADD_ID = 2;
	private Adventure a;
	private int recordBeingDeleted;
	
	private int whichFilter = 0;
	
	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication mApp = null;
	
	// saved preferences
	private static final String ADVLIST_PREFERENCES = "AdvListData";
	private static final String ADVLIST_FILTER = "AdvListFilter"; 

	
	/**
	 * TODO: Update this comment
	 * Override the onCreate method that retrieves list of adventures in background thread 
	 * from server via retrieveAdventures method and populates AdventureAdapter with this list of adventures.
	 * This method also sets up listview onClick listener so that when an single adventure is selected,
	 * it is passed to the AdventureViewActivity for viewing along with the entire adventure list for
	 * future functionality of scrolling sequentially thru each adventure in AdventureViewActivity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adventurelist);
		
		optionsMenuID = R.menu.adventure_list;
		
		// Get any saved preference values
		SharedPreferences advlist_settings = this.getApplicationContext().getSharedPreferences(ADVLIST_PREFERENCES, Constants.MODE_PRIVATE);
		whichFilter = advlist_settings.getInt(ADVLIST_FILTER, 0);
		
		// Update the title based on the whichFilter setting
		setTitle();
		
		// initialize objects				
		adventures = new ArrayList<Adventure>();
		listAdapter = new GenericEntryListAdapter(this, R.layout.row, adventures);
		setListAdapter(listAdapter);
		registerForContextMenu(getListView());		

//		nameTxt = (TextView) findViewById(R.id.adventurelist_username);
		// action when a list item is clicked
		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				a = (Adventure) parent.getItemAtPosition(position);

				// Open adventure view, pass current position in adventurelist along with
				// whole adventurelist
				// this can be used to implement swipe through adventures without
				// downloading each time
				Intent i = new Intent(parent.getContext(), AdventureViewActivity.class);
				i.putExtra("startPos", position);
				i.putExtra("adventureList", adventures);
				Bundle bundle = new Bundle();
				bundle.putSerializable("adventure", a);
				i.putExtras(bundle);
				// if user is viewing their own adventure list, open the adventure view
				// expecting a result in case
				// the user deletes the adventure from the adventureview
				if (userID == UserSession.CURRENTUSER_ID)
					startActivityForResult(i, CONTEXT_DELETE_ID);
				else
					startActivity(i);
			}
		});		
		
		// get the UserID that was passed to this activity to determine whose
		// adventures to load
		Intent i = getIntent();
		long id = i.getLongExtra(Constants.EXTRA_ID, -1);
		if (id != -1) {
			this.userID = id;
		}
		
		mApp = (GeotaggerApplication)getApplication();
		if (mApp != null) {
			rspHandler = new DbHandlerResponse(TAG, this, this);
			mApp.addResponseHandler(rspHandler);

			getAdventures();
		}
	}
	
	private void setTitle() {
		ActionBar actionbar = this.getActionBar();
		String title;
		if (whichFilter == 0) {
			title = this.getString(R.string.title_adventurelist_all);
		} else if (whichFilter == 1) {
			title = this.getString(R.string.title_adventurelist_ownerof);
		} else {
			title = this.getString(R.string.title_adventurelist_memberof);
		}

		actionbar.setTitle(title);
	}
	
	/**
	 * This method will make the appropriate database call to get the
	 * Adventures. This depends on the current setting of the 
	 * whichFilter variable.
	 */
	private void getAdventures() {
		setupProgress(getString(R.string.progress_retrieving_adventures));
		showProgress();
		
		int action;
		
		if (whichFilter == 1) {
			// Get Adventures for this user
			action = DbHandlerConstants.DBMSG_GET_ADVENTURES_OWNEROF;
		} else if (whichFilter == 2) {
			// Get Adventures this user is a member of
			action = DbHandlerConstants.DBMSG_GET_ADVENTURES_MEMBEROF;
		} else {
			// Get all adventures
			action = DbHandlerConstants.DBMSG_GET_ALL_ADVENTURES;
		}
		mApp.sendMsgToDbHandler(rspHandler, this, action);
	}
	
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		Log.d(TAG, "Entered DBGetCallback");
		if (action == DbHandlerConstants.DBMSG_DELETE) {
			// once the tag is removed from the db, remove it
			// from the arraylist and update.
			listAdapter.remove(adventures.get(recordBeingDeleted));
			listAdapter.notifyDataSetChanged();
			adventures.remove(recordBeingDeleted);
			stopProgress();
			Toast.makeText(AdventureListActivity.this, getString(R.string.toast_adventure_deleted), Toast.LENGTH_SHORT).show();
	
		} else {
			if (success) {
				@SuppressWarnings("unchecked")
				ArrayList<Adventure> advResponse = (ArrayList<Adventure>)response;
				adventures = advResponse;
				
				listAdapter.notifyDataSetChanged();
				listAdapter.clear();
				for (int i = 0; i < adventures.size(); i++)
					listAdapter.add(adventures.get(i));
				listAdapter.notifyDataSetChanged();
			}
			stopProgress();
		}
	}

	/**
	 *  When the AdventureView activity is closed, this method checks to see if a delete call was sent back.
	 *  It is was, the adventure is deleted from the server
	 *  */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	
		if (requestCode == CONTEXT_DELETE_ID) {
			if (resultCode == RESULT_OK) {
				int removeIndex = data.getIntExtra("Delete", -1);
				// if so, delete adventure and update list (adventure was deleted from
				// database in other activity)
				if (removeIndex >= 0) {
					deleteAdventure(removeIndex);
				}
			}
		} else if (requestCode == CONTEXT_ADD_ID) {
			if (resultCode == RESULT_OK) {
				getAdventures();
			}
		}
	} // onActivityResult

	/*
	 * Event Handlers
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_add:
			Intent intent = new Intent(getBaseContext(), EditAdventureActivity.class);
			intent.putExtra("newAdventure", true);
			startActivityForResult(intent, CONTEXT_ADD_ID);
			return true;
		case R.id.action_filter:
			Dialog dialog = onCreateDialog();
			dialog.show();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	// create context menu when list item is long-pressed
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		// show delete context menu only if user is viewing their own adventure list
		if (this.userID == UserSession.CURRENTUSER_ID) {
			menu.setHeaderTitle("Adventure " + adventures.get(info.position).getName());
			menu.add("Delete");
		}
	}

	// actions for context menu items
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		// delete the selected adventure
		if (item.getTitle() == "Delete") {
			deleteAdventure(info.position);
		}
		return true;
	}

	private Dialog onCreateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_filter)
	    	   .setSingleChoiceItems(R.array.adventure_list_filter, whichFilter, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	if (whichFilter != which) {
	        		whichFilter = which;

	        		// Update the title based on the whichFilter setting
	        		setTitle();

		        	SharedPreferences advlist_settings = AdventureListActivity.this.getApplicationContext().getSharedPreferences(ADVLIST_PREFERENCES, Constants.MODE_PRIVATE);
		        	SharedPreferences.Editor editor = advlist_settings.edit();
		        	editor.putInt(ADVLIST_FILTER, whichFilter);
		        	editor.commit();
		            	   
		        	dialog.dismiss();
		        	getAdventures();
	        	}
	        }
	    });
	    return builder.create();
	}
	
	/**
	 * Deletes the adventure listed at position. This method is called if selected in
	 * following AdventureViewActivity or if selected from the AdventureListActivity's context menu
	 * @param position Specifies the Adventure's position in the List to be deleted.
	 */
	private void deleteAdventure(final int position) {
		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_DELETE, adventures.get(position));
		
		recordBeingDeleted = position;
		
		setupProgress(getString(R.string.progress_deleting_adventure));
		showProgress();
	}

}