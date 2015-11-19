package com.hci.geotagger.activities;

import java.util.Date;
import java.util.ArrayList;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.gui.GenericEntryListAdapter;
import com.hci.geotagger.objects.Adventure;
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
 * Activity allows a user to modify an Adventure.
 */
public class EditAdventureActivity extends BaseActivity implements DbMessageResponseInterface {
	private String TAG = "EditAdventureActivity";
	
	private Button save;	
	private EditText nameE, descriptionE;
	private Adventure adventure;	
	private boolean isNewAdv = false;
	
	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication app;
	
	private ArrayList<Tag> tags = null;
	private ArrayList<UserAccount> friends = null;
	
	private ArrayList<Tag> selectedTags;
	private ArrayList<UserAccount> selectedFriends;

	private static final int GET_SELECTED_TAGS = 101;
	private static final int GET_SELECTED_FRIENDS = 102;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_adventure);
		
		Intent intent = getIntent();				
		if(intent.getBooleanExtra("newAdventure", false) == true) {
			adventure = new Adventure(Constants.VISIBILITY_FULL, UserSession.CURRENTUSER_ID, null, null,
										/*UserSession.CURRENT_USER.getName(),*/ new Date());			
			isNewAdv = true;
		} else {
			Bundle bundle = intent.getExtras();		
			adventure = (Adventure) bundle.getSerializable("adventure");
		}
		
		nameE = (EditText)findViewById(R.id.adventureEdit_name);
		descriptionE = (EditText) findViewById(R.id.adventureEdit_desc);
		if (isNewAdv == false) {			
			nameE.setText(adventure.getName());				
			descriptionE.setText(adventure.getDescription());
		}

		save = (Button) findViewById(R.id.adventureEdit_btnOk);
		if (!isNewAdv)
			save.setText(R.string.save);
		save.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				//TODO: disable the OK button if these fields are not filled in yet
				if(nameE.getText().toString().isEmpty() || descriptionE.getText().toString().isEmpty()) {
					Toast t = Toast.makeText(EditAdventureActivity.this, getString(R.string.toast_missing_adv_fields), Toast.LENGTH_SHORT);
					t.show();
				} else {
					adventure.setName(nameE.getText().toString());
					adventure.setDescription(descriptionE.getText().toString());
					
					if(isNewAdv == true) {
						app.sendMsgToDbHandler(rspHandler, EditAdventureActivity.this, DbHandlerConstants.DBMSG_ADD, adventure);

						setupProgress(getString(R.string.progress_add_adventure));
						showProgress();
					} else {
						app.sendMsgToDbHandler(rspHandler, EditAdventureActivity.this, DbHandlerConstants.DBMSG_UPDATE, adventure);

						setupProgress(getString(R.string.progress_updating_adventure));
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
			
			if (!isNewAdv) {
				app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_ADVENTURE_TAGS, adventure);
				app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_ADVENTURE_USERS, adventure);
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
			case DbHandlerConstants.DBMSG_GET_ADVENTURE_TAGS:
				selectedTags = (ArrayList<Tag>) response;
				adventure.setTags(selectedTags);
        		if (listAdapter != null && peopleTabBtn.isEnabled()) {
        			listAdapter.updateList(Tag.LIST_LAYOUT_ID, selectedTags);
        		}
				break;
			case DbHandlerConstants.DBMSG_GET_ADVENTURE_USERS:
				selectedFriends = (ArrayList<UserAccount>) response;
				adventure.setUsers(selectedFriends);
        		if (listAdapter != null && tagsTabBtn.isEnabled()) {
        			listAdapter.updateList(Tag.LIST_LAYOUT_ID, selectedFriends);
        		}
				break;
			case DbHandlerConstants.DBMSG_GET_ALL_TAGS:
				tags = (ArrayList<Tag>)response;
				break;
			case DbHandlerConstants.DBMSG_GET_USERS:
				friends = (ArrayList<UserAccount>) response;
				friends.add(UserSession.CURRENT_USER);
				stopProgress();
				break;
			case DbHandlerConstants.DBMSG_ADD:
				msg = this.getResources().getString(R.string.add_adventure_success);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				// return to adventure list screen
				returnIntent = new Intent();
				bundle = new Bundle();
				bundle.putSerializable("adventure", adventure);
				returnIntent.putExtras(bundle);
				setResult(RESULT_OK, returnIntent);        
				stopProgress();
				finish();
				break;
			case DbHandlerConstants.DBMSG_UPDATE:
				msg = this.getResources().getString(R.string.update_adventure_success);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				// return to adventure list screen
				returnIntent = new Intent();
				bundle = new Bundle();
				bundle.putSerializable("adventure", adventure);
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
				msg = this.getResources().getString(R.string.add_adventure_failure);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				break;
			case DbHandlerConstants.DBMSG_UPDATE:
				stopProgress();
				msg = this.getResources().getString(R.string.update_adventure_failure);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}



	
	private LinearLayout tagsTabBtn;	// press to view tags list
	private LinearLayout peopleTabBtn;	// press to view people list
	private Button addDataBtn;			// Press to show dialog to select Tags or People
	private ListView listView;			// List for the tags and people
	private GenericEntryListAdapter listAdapter;

	private void initializeUIComponents() {
		addDataBtn = (Button) findViewById(R.id.adventureEdit_addData);
		addDataBtn.setText(R.string.adventure_add_tags);

		// implement the button listeners
		addDataBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Call the appropriate selection activity
				if (tagsTabBtn.isEnabled()) {
					if (friends == null || friends.size() == 0)
						return;
					for (int i=0; i<friends.size(); i++) {
						UserAccount friend = friends.get(i);
						friend.selected = friend.inList(selectedFriends);
						friends.set(i, friend);
					}
					// select the friends
					Intent i = new Intent(EditAdventureActivity.this, SelectFromListActivity.class);
					i.putExtra(SelectFromListActivity.EXTRA_LIST, friends);
					i.putExtra(SelectFromListActivity.EXTRA_TITLE, R.string.selectRecords_title_people);
					//TODO: Need to set a return value
					startActivityForResult(i, GET_SELECTED_FRIENDS);
				} else {
					if (tags == null || tags.size() == 0)
						return;
					for (int i=0; i<tags.size(); i++) {
						Tag tag = tags.get(i);
						tag.selected = tag.inList(selectedTags);
						tags.set(i, tag);
					}
					Intent i = new Intent(EditAdventureActivity.this, SelectFromListActivity.class);
					i.putExtra(SelectFromListActivity.EXTRA_LIST, tags);
					i.putExtra(SelectFromListActivity.EXTRA_TITLE, R.string.selectRecords_title_tags);
					//TODO: Need to set a return value
					startActivityForResult(i, GET_SELECTED_TAGS);
				}
			}
		});


		tagsTabBtn = (LinearLayout) findViewById(R.id.tags_button);
		peopleTabBtn = (LinearLayout) findViewById(R.id.people_button);

		tagsTabBtn.setEnabled(false); // do not allow multiple clicks causing

		// implement the button listeners
		tagsTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				hide_keyboard(EditAdventureActivity.this);

				// disable the Tags button, enable the People
				tagsTabBtn.setEnabled(false);
				peopleTabBtn.setEnabled(true);
				addDataBtn.setText(R.string.adventure_add_tags);

				listAdapter = new GenericEntryListAdapter(EditAdventureActivity.this, Tag.LIST_LAYOUT_ID, selectedTags);
				listView.setAdapter(listAdapter);
			}
		});

		peopleTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hide_keyboard(EditAdventureActivity.this);
				
				// disable the map view button, re-enable the others
				peopleTabBtn.setEnabled(false);
				tagsTabBtn.setEnabled(true);
				addDataBtn.setText(R.string.adventure_add_people);

				listAdapter = new GenericEntryListAdapter(EditAdventureActivity.this, UserAccount.LIST_LAYOUT_ID, selectedFriends);
				listView.setAdapter(listAdapter);
			}
		});

		selectedTags = adventure.getTags();
		selectedFriends = adventure.getUsers();

		listView = (ListView) findViewById(R.id.adventureEdit_list);
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
        		
        		adventure.mergeTagsList(selectedTags);
        		if (listAdapter != null) {
        			listAdapter.updateList(Tag.LIST_LAYOUT_ID, selectedTags);
        		}
        		break;
        	case GET_SELECTED_FRIENDS:
        		selectedFriends = (ArrayList<UserAccount>)data.getSerializableExtra(SelectFromListActivity.EXTRA_LIST);
        		
        		adventure.mergeUserList(selectedFriends);
        		if (listAdapter != null) {
        			listAdapter.updateList(UserAccount.LIST_LAYOUT_ID, selectedFriends);
        		}
        		break;
            }
        }
    }
}