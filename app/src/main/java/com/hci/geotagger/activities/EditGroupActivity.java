package com.hci.geotagger.activities;

import java.util.Date;
import java.util.ArrayList;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.gui.GenericEntryListAdapter;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.Group;
import com.hci.geotagger.objects.Tag;
import com.hci.geotagger.objects.UserAccount;
import com.hci.geotagger.activities.common.BaseActivity;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Activity;

import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;


/**
 * This class currently is not being used in this iteration of GeoTagger. Originally, this
 * Activity allows a user to modify an Group.
 */
public class EditGroupActivity extends BaseActivity implements DbMessageResponseInterface {
	private String TAG = "EditGroupActivity";
	
	private Button save;	
	private EditText nameE, descriptionE;
	private Group group;	
	private boolean isNewGroup = false;
	
	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication app;
	
	private ArrayList<Tag> tags = null;
	private ArrayList<UserAccount> members = null;
	private ArrayList<Adventure> adventures = null;
	
	private ArrayList<Tag> selectedTags;
	private ArrayList<UserAccount> selectedMembers;
	private ArrayList<Adventure> selectedAdventures;

	private static final int GET_SELECTED_TAGS = 101;
	private static final int GET_SELECTED_MEMBERS = 102;
	private static final int GET_SELECTED_ADVENTURES = 103;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_group);
		
		Intent intent = getIntent();				
		if(intent.getBooleanExtra("newGroup", false) == true) {
			group = new Group(Constants.VISIBILITY_FULL, UserSession.CURRENTUSER_ID, null, null,
										/*UserSession.CURRENT_USER.getName(),*/ new Date());			
			isNewGroup = true;
		} else {
			Bundle bundle = intent.getExtras();		
			group = (Group) bundle.getSerializable("group");
		}
		
		nameE = (EditText)findViewById(R.id.groupEdit_name);
		descriptionE = (EditText) findViewById(R.id.groupEdit_desc);
		if (isNewGroup == false) {			
			nameE.setText(group.getName());				
			descriptionE.setText(group.getDescription());
		}
				
		save = (Button) findViewById(R.id.groupEdit_btnOk);
		if (!isNewGroup)
			save.setText(R.string.save);
		save.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				//TODO: disable the OK button if these fields are not filled in yet
				if(nameE.getText().toString().isEmpty() || descriptionE.getText().toString().isEmpty()) {
					Toast t = Toast.makeText(EditGroupActivity.this, getString(R.string.toast_missing_adv_fields), Toast.LENGTH_SHORT);
					t.show();
				} else {
					group.setName(nameE.getText().toString());
					group.setDescription(descriptionE.getText().toString());
					
					if(isNewGroup == true) {
						app.sendMsgToDbHandler(rspHandler, EditGroupActivity.this, DbHandlerConstants.DBMSG_ADD, group);

						setupProgress(getString(R.string.progress_add_group));
						showProgress();
					} else {
						app.sendMsgToDbHandler(rspHandler, EditGroupActivity.this, DbHandlerConstants.DBMSG_UPDATE, group);

						setupProgress(getString(R.string.progress_updating_group));
						showProgress();
					}
				}				
			}
		});
		
		initializeUIComponents();
		
		setupProgress(getString(R.string.progress_loading_records));
		
		app = (GeotaggerApplication)getApplication();
		if (app != null) {
			rspHandler = new DbHandlerResponse(TAG, this, this);
			app.addResponseHandler(rspHandler);

			showProgress();
			
			if (!isNewGroup) {
				app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_GROUP_TAGS, group);
				app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_GROUP_MEMBERS, group);
				app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_GROUP_ADVENTURES, group);
			}
			app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_ALL_TAGS);
			app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_USERS);
		}
	}//end onCreate
	
	@SuppressWarnings("unchecked")
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		Log.d(TAG, "Entered DBGetCallback");
		String msg;
		Intent returnIntent;
		Bundle bundle;

		
		if (success) {
			switch (action) {
			case DbHandlerConstants.DBMSG_GET_GROUP_TAGS:
				selectedTags = (ArrayList<Tag>) response;
				group.setTags(selectedTags);
        		if (listAdapter != null && !tagsTabBtn.isEnabled()) {
        			listAdapter.updateList(Tag.LIST_LAYOUT_ID, selectedTags);
        		}
				break;
			case DbHandlerConstants.DBMSG_GET_GROUP_MEMBERS:
				selectedMembers = (ArrayList<UserAccount>) response;
				group.setUsers(selectedMembers);
        		if (listAdapter != null && !membersTabBtn.isEnabled()) {
        			listAdapter.updateList(Tag.LIST_LAYOUT_ID, selectedMembers);
        		}
				break;
			case DbHandlerConstants.DBMSG_GET_GROUP_ADVENTURES:
				selectedAdventures = (ArrayList<Adventure>) response;
				group.setAdventures(selectedAdventures);
        		if (listAdapter != null && !adventuresTabBtn.isEnabled()) {
        			listAdapter.updateList(Tag.LIST_LAYOUT_ID, selectedAdventures);
        		}
				break;
			case DbHandlerConstants.DBMSG_GET_ALL_TAGS:
				tags = (ArrayList<Tag>)response;
				break;
			case DbHandlerConstants.DBMSG_GET_USERS:
				members = (ArrayList<UserAccount>) response;
				members.add(UserSession.CURRENT_USER);
				stopProgress();
				break;
			case DbHandlerConstants.DBMSG_ADD:
				msg = this.getResources().getString(R.string.add_group_success);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				// return to group list screen
				returnIntent = new Intent();
				bundle = new Bundle();
				bundle.putSerializable("group", group);
				returnIntent.putExtras(bundle);
				setResult(RESULT_OK, returnIntent);        
				stopProgress();
				finish();
				break;
			case DbHandlerConstants.DBMSG_UPDATE:
				msg = this.getResources().getString(R.string.update_group_success);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				// return to group list screen
				returnIntent = new Intent();
				bundle = new Bundle();
				bundle.putSerializable("group", group);
				returnIntent.putExtras(bundle);
				setResult(RESULT_OK, returnIntent);        
				stopProgress();
				finish();
				break;
			}
		} else {
			switch (action) {
			case DbHandlerConstants.DBMSG_GET_USERS:
				stopProgress();
				break;
			case DbHandlerConstants.DBMSG_ADD:
				stopProgress();
				msg = this.getResources().getString(R.string.add_group_failure);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				break;
			case DbHandlerConstants.DBMSG_UPDATE:
				stopProgress();
				msg = this.getResources().getString(R.string.update_group_failure);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}



	
	private LinearLayout tagsTabBtn;		// press to view tags list
	private LinearLayout membersTabBtn;		// press to view members list
	private LinearLayout adventuresTabBtn;	// press to view adventures list
	private Button addDataBtn;				// Press to show dialog to select Tags or People
	private ListView listView;				// List for the tags and people
	private GenericEntryListAdapter listAdapter;

	private void initializeUIComponents() {
		addDataBtn = (Button) findViewById(R.id.groupEdit_addData);
		addDataBtn.setText(R.string.group_add_tags);

		// implement the button listeners
		addDataBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Call the appropriate selection activity
				if (!membersTabBtn.isEnabled()) {
					if (members == null || members.size() == 0)
						return;
					for (int i=0; i<members.size(); i++) {
						UserAccount friend = members.get(i);
						friend.selected = friend.inList(selectedMembers);
						members.set(i, friend);
					}
					// select the members
					Intent i = new Intent(EditGroupActivity.this, SelectFromListActivity.class);
					i.putExtra(SelectFromListActivity.EXTRA_LIST, members);
					i.putExtra(SelectFromListActivity.EXTRA_TITLE, R.string.selectRecords_title_people);
					//TODO: Need to set a return value
					startActivityForResult(i, GET_SELECTED_MEMBERS);
				} else if (!tagsTabBtn.isEnabled()){
					if (tags == null || tags.size() == 0)
						return;
					for (int i=0; i<tags.size(); i++) {
						Tag tag = tags.get(i);
						tag.selected = tag.inList(selectedTags);
						tags.set(i, tag);
					}
					Intent i = new Intent(EditGroupActivity.this, SelectFromListActivity.class);
					i.putExtra(SelectFromListActivity.EXTRA_LIST, tags);
					i.putExtra(SelectFromListActivity.EXTRA_TITLE, R.string.selectRecords_title_tags);
					//TODO: Need to set a return value
					startActivityForResult(i, GET_SELECTED_TAGS);
				} else {
					if (adventures == null || adventures.size() == 0)
						return;
					for (int i=0; i<adventures.size(); i++) {
						Adventure adventure = adventures.get(i);
						adventure.selected = adventure.inList(selectedAdventures);
						adventures.set(i, adventure);
					}
					Intent i = new Intent(EditGroupActivity.this, SelectFromListActivity.class);
					i.putExtra(SelectFromListActivity.EXTRA_LIST, adventures);
					i.putExtra(SelectFromListActivity.EXTRA_TITLE, R.string.selectRecords_title_adventures);
					//TODO: Need to set a return value
					startActivityForResult(i, GET_SELECTED_ADVENTURES);
				}
			}
		});


		tagsTabBtn = (LinearLayout) findViewById(R.id.tags_button);
		membersTabBtn = (LinearLayout) findViewById(R.id.members_button);
		adventuresTabBtn = (LinearLayout) findViewById(R.id.adventure_button);

		tagsTabBtn.setEnabled(false); // do not allow multiple clicks causing

		// implement the button listeners
		tagsTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				hide_keyboard(EditGroupActivity.this);

				// disable the Tags button, enable the People
				tagsTabBtn.setEnabled(false);
				membersTabBtn.setEnabled(true);
				adventuresTabBtn.setEnabled(true);
				addDataBtn.setText(R.string.group_add_tags);

				listAdapter = new GenericEntryListAdapter(EditGroupActivity.this, Tag.LIST_LAYOUT_ID, selectedTags);
				listView.setAdapter(listAdapter);
			}
		});

		membersTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hide_keyboard(EditGroupActivity.this);
				
				// disable the map view button, re-enable the others
				membersTabBtn.setEnabled(false);
				tagsTabBtn.setEnabled(true);
				adventuresTabBtn.setEnabled(true);
				addDataBtn.setText(R.string.group_add_people);

				listAdapter = new GenericEntryListAdapter(EditGroupActivity.this, UserAccount.LIST_LAYOUT_ID, selectedMembers);
				listView.setAdapter(listAdapter);
			}
		});

		adventuresTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hide_keyboard(EditGroupActivity.this);
				
				// disable the map view button, re-enable the others
				adventuresTabBtn.setEnabled(false);
				membersTabBtn.setEnabled(true);
				tagsTabBtn.setEnabled(true);
				addDataBtn.setText(R.string.group_add_adventures);

				listAdapter = new GenericEntryListAdapter(EditGroupActivity.this, UserAccount.LIST_LAYOUT_ID, selectedAdventures);
				listView.setAdapter(listAdapter);
			}
		});

		selectedTags = group.getTags();
		selectedMembers = group.getUsers();
		selectedAdventures = group.getAdventures();

		listView = (ListView) findViewById(R.id.groupEdit_list);
		listAdapter = new GenericEntryListAdapter(this, Tag.LIST_LAYOUT_ID, selectedTags);
		listView.setAdapter(listAdapter);
	}
	
	public static void hide_keyboard(Activity activity) {
	    InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    //Find the currently focused view, so we can grab the correct window token from it.
	    View view = activity.getCurrentFocus();
	    //If no view currently has focus, create a new one, just so we can grab a window token from it
	    if(view == null) {
	        view = new View(activity);
	    }
	    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	/**
	 * This method will be called when the called activity returns.
	 */
	@SuppressWarnings("unchecked")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
        	switch(requestCode) {
        	case GET_SELECTED_TAGS:
        		selectedTags = (ArrayList<Tag>)data.getSerializableExtra(SelectFromListActivity.EXTRA_LIST);
        		
        		group.mergeTagsList(selectedTags);
        		if (listAdapter != null) {
        			listAdapter.updateList(Tag.LIST_LAYOUT_ID, selectedTags);
        		}
        		break;
        	case GET_SELECTED_MEMBERS:
        		selectedMembers = (ArrayList<UserAccount>)data.getSerializableExtra(SelectFromListActivity.EXTRA_LIST);
        		
        		group.mergeUserList(selectedMembers);
        		if (listAdapter != null) {
        			listAdapter.updateList(UserAccount.LIST_LAYOUT_ID, selectedMembers);
        		}
        		break;
        	case GET_SELECTED_ADVENTURES:
        		selectedAdventures = (ArrayList<Adventure>)data.getSerializableExtra(SelectFromListActivity.EXTRA_LIST);
        		
        		group.mergeAdventuresList(selectedAdventures);
        		if (listAdapter != null) {
        			listAdapter.updateList(UserAccount.LIST_LAYOUT_ID, selectedAdventures);
        		}
        		break;
            }
        }
    }
}