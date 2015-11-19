package com.hci.geotagger.activities;

import java.util.ArrayList;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.objects.Group;
import com.hci.geotagger.activities.common.BaseListActivity;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.gui.GenericEntryListAdapter;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * @author Syed M Shah
 * Grouplist activity displays a scrollable list of adventures for 
 * a given user account and gives the user the ability
 * to click on them to view the adventure. 
 */

public class GroupListActivity extends BaseListActivity implements DbMessageResponseInterface {
	private static final String TAG = "GroupListActivity";

	private ArrayList<Group> groups = null;
	private GenericEntryListAdapter listAdapter;

	private int userID;
	private int CONTEXT_DELETE_ID = 1;
	private Group g;
	
	TextView nameTxt;
	private int flag = 1;
	
	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication mApp = null;

	private final static int GROUP_FILTER_ALL = 0;
	private final static int GROUP_FILTER_OWNER = 1;
	private final static int GROUP_FILTER_MEMBER = 2;
	
	int whichFilter = GROUP_FILTER_MEMBER;

	// saved preferences
	private static final String GROUPLIST_PREFERENCES = "GroupListData";
	private static final String GROUPLIST_FILTER = "GroupListFilter"; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_user);
		
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(getString(R.string.group_list_title));
		
		optionsMenuID = R.menu.group_list;
		
		// Get any saved preference values
		SharedPreferences grouplist_settings = this.getApplicationContext().getSharedPreferences(GROUPLIST_PREFERENCES, Constants.MODE_PRIVATE);
		whichFilter = grouplist_settings.getInt(GROUPLIST_FILTER, GROUP_FILTER_OWNER);
		
		// Update the title based on the whichFilter setting
		setTitle();
		
		// initialize objects				
		groups = new ArrayList<Group>();
		listAdapter = new GenericEntryListAdapter(this, R.layout.row, groups);
		setListAdapter(listAdapter);
		registerForContextMenu(getListView());
		
		//nameTxt = (TextView) findViewById(R.id.grouplist1_username);
		// action when a list item is clicked
		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				g = (Group) parent.getItemAtPosition(position);
				Intent i = new Intent(parent.getContext(), GroupViewActivity.class);
				i.putExtra("startPos", position);
				i.putExtra("groupList", groups);
				i.setFlags(flag);
				Bundle bundle = new Bundle();
				bundle.putSerializable("group", g);
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
		/*
		Intent i = getIntent();
		int id = i.getIntExtra("id", -1);
		flag = i.getFlags();
		if(flag ==1)
			retrieveOwnerGroups();
		else if(flag ==2)
			retrieveMemberGroups();
			*/
		
		mApp = (GeotaggerApplication)getApplication();
		if (mApp != null) {
			rspHandler = new DbHandlerResponse(TAG, this, this);
			mApp.addResponseHandler(rspHandler);

			getGroups();
		}
	}
	
	/**
	 * This method will make the appropriate database call to get the Group
	 * records. This depends on the current setting of the whichfilter
	 * variable.
	 */
	private void getGroups() {
		setupProgress(getString(R.string.progress_retrieving_groups));
		showProgress();
		
		int action;
		
		if (whichFilter == GROUP_FILTER_OWNER) {		// My Groups
			   action = DbHandlerConstants.DBMSG_GET_GROUPS_OWNER;
		   } else if (whichFilter == GROUP_FILTER_MEMBER) {		// Member of Groups
			   action = DbHandlerConstants.DBMSG_GET_GROUPS_MEMBER;
		   } else {
			   action = DbHandlerConstants.DBMSG_GET_GROUPS;
		   }

		mApp.sendMsgToDbHandler(rspHandler, this, action);
	}
	
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		Log.d(TAG, "Entered DBGetCallback");
		switch (action) {
		case DbHandlerConstants.DBMSG_GET_GROUPS:
		case DbHandlerConstants.DBMSG_GET_GROUPS_OWNER:
		case DbHandlerConstants.DBMSG_GET_GROUPS_MEMBER:
			if (success) {
				ArrayList<Group> advResponse = (ArrayList<Group>)response;
				groups = advResponse;
				
				listAdapter.notifyDataSetChanged();
				listAdapter.clear();
				for (int i = 0; i < groups.size(); i++)
					listAdapter.add(groups.get(i));
				listAdapter.notifyDataSetChanged();
			}
			stopProgress();
			break;
		}
	}

	private void setTitle() {
		ActionBar actionbar = this.getActionBar();
		String title;
		if (whichFilter == GROUP_FILTER_ALL) {
			title = this.getString(R.string.title_grouplist_all);
		} else if (whichFilter == GROUP_FILTER_OWNER) {
			title = this.getString(R.string.title_grouplist_ownerof);
		} else {
			title = this.getString(R.string.title_grouplist_memberof);
		}

		actionbar.setTitle(title);
	}

	/*
	 * Event Handlers
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_filter:
			Dialog dialog = onCreateDialog();
			dialog.show();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}	
	
	private Dialog onCreateDialog() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(R.string.select_filter)
	    		.setSingleChoiceItems(R.array.adventure_list_filter, whichFilter, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	            	   
	            	   // If the filter has changed then make the appropriate DB call
	            	   if (whichFilter != which) {
		            	   whichFilter = which;
	            		   getGroups();
	            	   }
	            	   
			        	SharedPreferences advlist_settings = GroupListActivity.this.getApplicationContext().getSharedPreferences(GROUPLIST_PREFERENCES, Constants.MODE_PRIVATE);
			        	SharedPreferences.Editor editor = advlist_settings.edit();
			        	editor.putInt(GROUPLIST_FILTER, whichFilter);
			        	editor.commit();

	            	   // Update the title based on the whichFilter setting
	            	   setTitle();

	            	   dialog.dismiss();
	               }
	    });
	    return builder.create();
	}	
}