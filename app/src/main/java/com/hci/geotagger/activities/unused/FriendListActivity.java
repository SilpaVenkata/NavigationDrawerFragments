package com.hci.geotagger.activities.unused;

import java.util.ArrayList;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.UserProfileActivity;
import com.hci.geotagger.activities.common.BaseListActivity;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.AccountHandler;
import com.hci.geotagger.connectors.ImageHandler;
import com.hci.geotagger.connectors.ReturnInfo;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.UserAccount;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This class is not used in current iteration of GeoTagger. Originally, this class allows a user 
 * to view friends associated with their profile. You could also add or remove a friend from an adventure based on their username. Lastly, you can view a friend's 
 * profile
 * 
 *
 */
public class FriendListActivity extends BaseListActivity implements DbMessageResponseInterface {
	private String TAG = "FriendListActivity";
	
	private ImageButton btnAddFriend;
	private TextView nameTxt;
	
	private AccountHandler accountHandler;
	private Runnable addFriend, loadSingleFriend;
	private final Context c = this;
	
	private ArrayList<UserAccount> friends = null;
	private FriendAdapter FA;
	
	private String friendAdded = ""; //callback field used to pass data between threads when adding friend
	private UserAccount acctToAdd = null;
	private long friendListOwnerId; //id of the user account whose friends list is being shown
	
	private int flag;	
	private Adventure adventure;

	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication app;

	/*
	 * Event Handlers
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_list);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		adventure = (Adventure) bundle.getSerializable("adventure");
		
		//initialize objects
		friends = new ArrayList<UserAccount>();
		accountHandler = new AccountHandler(this);
		friendListOwnerId = UserSession.CURRENTUSER_ID;
		
		this.FA = new FriendAdapter(this, R.layout.peoplerow, friends);
		setListAdapter(this.FA);
		registerForContextMenu(getListView());
		
		//init ui elements
		nameTxt = (TextView) findViewById(R.id.friendlist_username);
		nameTxt.setText(UserSession.CURRENT_USER.getuName() + "'s Friends");
		btnAddFriend = (ImageButton) findViewById(R.id.friends_btnAdd);
		
		//call separate thread to retrieve friends list
		app = (GeotaggerApplication)getApplication();
		if (app != null) {
			rspHandler = new DbHandlerResponse(TAG, this, this);
			app.addResponseHandler(rspHandler);

			setupProgress(getString(R.string.progress_retrieving_friends));
			showProgress();
			app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_USERS);
		}

		// Set onClick action for add friend button
		btnAddFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// get the xml layout for the dialog
				LayoutInflater inflater = LayoutInflater.from(c);
				View searchView = inflater.inflate(R.layout.addfriend_dialog,
						null);
				// create dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(c);
				// set the xml layout to the dialog
				builder.setView(searchView);
				// get the input box
				final EditText searchInput = (EditText) searchView
						.findViewById(R.id.searchdlg_txtusername);
				// set dialog message
				builder.setCancelable(true)
						.setPositiveButton("Add Friend",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// get the input from the search and
										// search the db for that username
										String searchName = searchInput
												.getText().toString();
										if (!searchName.isEmpty())
											addFriend(searchName);
										else
											Toast.makeText(
													c,
													"Please enter a username to search for!",
													Toast.LENGTH_SHORT).show();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				// create the dialog and show
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
		});
		
		//when a friend item is clicked, open their profile
		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				UserAccount acct = (UserAccount) parent.getItemAtPosition(position);
				//Toast.makeText(FriendListActivity.this, "Position: " + position +  " User: " + acct.getuName(), Toast.LENGTH_SHORT).show();
				Intent i = new Intent(parent.getContext(),UserProfileActivity.class);
				i.putExtra("account", acct);
				startActivity(i);
			}
		});		
		
		if(intent.getFlags() == 1)
		{
			flag = 1;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		Log.d(TAG, "Entered DBGetCallback");
		if (success) {
			switch (action) {
			case DbHandlerConstants.DBMSG_GET_USERS:
				friends = (ArrayList<UserAccount>) response;
				FA.notifyDataSetChanged();
				for(int i = 0; i < friends.size(); i++)
					FA.add(friends.get(i));
	            FA.notifyDataSetChanged();

				stopProgress();
				break;
			}
		} else {
			switch (action) {
			case DbHandlerConstants.DBMSG_GET_USERS:
				stopProgress();
				break;
			}
		}
	}




	//Context menu to allow user to delete tags
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) 
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		//show delete context menu only if user is viewing their own tag list
		if(this.friendListOwnerId == UserSession.CURRENTUSER_ID)
		{			
			menu.setHeaderTitle("Remove Friend");			
			if(flag == 1)
			{
				menu.add(1,1,1,"Add " + friends.get(info.position).getuName());
			}
			else
			{
				menu.add(1,1,1,"Remove " + friends.get(info.position).getuName());
			}
		}
	} 
	
	//actions for context menu items
	@Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        //delete the selected tag
        if(item.getTitle().equals("Remove"))
        {
        	removeFriend(info.position);
        }
        else if(item.getTitle().equals("Add"))
        {
        	addFriendToAdventure(info.position);
        }	        
        else if(item.getTitle().equals("Remove") && flag == 1)
        {
        	removeFriendFromAdventure(info.position);
        }
        return true;
    }

	/*
	 * Functions
	 */
		
	/**
	 * remove a friend from the friends list/db
	 * @param index
	 */
	private void removeFriend(final int index)
	{
		final AccountHandler ah = new AccountHandler(this);
		Runnable deleteTag = new Runnable() 
		{
			@Override
			public void run() 
			{
				boolean success = ah.deleteFriend(friendListOwnerId, friends.get(index).getId());
				if (success)
				{
					   runOnUiThread(new Runnable() {
						    public void run() {
						    	stopProgress();
						    	//once the tag is removed from the db, remove it from the arraylist and update.
						    	FA.remove(friends.get(index));
								FA.notifyDataSetChanged();
								friends.remove(index);
								Toast.makeText(FriendListActivity.this, "Friend Removed", Toast.LENGTH_SHORT).show();	
						    }	    
					});	
				}
				else
					Toast.makeText(FriendListActivity.this, "Error Removing Friend..", Toast.LENGTH_SHORT).show();
			}
		};
		Thread thread = new Thread(null, deleteTag, "DeleteTagThread");
		thread.start();
		setupProgress(getString(R.string.progress_removing_friend));
		showProgress();
	}
	
	/**
	 * Adds an existing friend to an adventure.
	 */
	private void addFriendToAdventure(final int index) {			
		Runnable addFriend = new Runnable() {
			@Override
			public void run() {
				boolean success = adventure.addStoreUserList(friends.get(index));
				if (success) {
					runOnUiThread(new Runnable() {
						public void run() {
							stopProgress();
							Toast.makeText(FriendListActivity.this,
									"Person Added!", Toast.LENGTH_SHORT).show();
						}
					});
				} else {
					setupProgress(getString(R.string.progress_adding_person));
					showProgress();
				}
			}
		};
		Thread thread = new Thread(null, addFriend, "AddFriendThread");
		thread.start();
		setupProgress(getString(R.string.progress_adding_person));
		showProgress();
	}
	
	/*
	 * Removes an existing friend from an adventure.
	 */
	private void removeFriendFromAdventure(final int index) {			
		Runnable removeFriend = new Runnable() {
			@Override
			public void run() {
				boolean success = adventure.removeStoreUserList(friends.get(index));
				if (success) {
					runOnUiThread(new Runnable() {
						public void run() {
							stopProgress();
							Toast.makeText(FriendListActivity.this,
									"Person Removed!", Toast.LENGTH_SHORT).show();
						}
					});
				} else
					Toast.makeText(FriendListActivity.this,
							"Error Removing Person...", Toast.LENGTH_SHORT).show();
			}
		};
		Thread thread = new Thread(null, removeFriend, "RemoveFriendThread");
		thread.start();
		setupProgress(getString(R.string.progress_retrieving_users));
		showProgress();
	}

	
	//Attempt to add a friend based on the given username
	private void addFriend(String userName)
	{
		final String uName = userName;
		
		// create new thread to add friend
		addFriend = new Runnable() {
			@Override
			public void run() {
				
				final StringBuilder sb = new StringBuilder();
				ReturnInfo response = accountHandler.addFriend(UserSession.CURRENTUSER_ID, uName);
				String msg;
				//if friend was added successfully
				if (response.success)
				{
					msg = "Added " + uName + " as a friend!";
					sb.append(msg);
					friendAdded = uName;
				}
				else //find out what went wrong
				{
					switch (response.detail)
					{
						case Constants.ADDFRIEND_ALREADYFRIENDS: 
							msg = uName + " is already in your friends list!";
							sb.append(msg);
							break;
						case Constants.ADDFRIEND_USERNOTFOUND: 	
							msg = "Sorry! We can't find that user...please try another name!";
							sb.append(msg);
							break;
						case Constants.ADDFRIEND_ERROR: 	
							msg = "Oops! Something went wrong...Please search again!";
							sb.append(msg);
							break;
						default: 
							msg = "Please try again.";
							sb.append(msg);
							break;
					}
				}
				//show result on the UI thread and close the dialog
				runOnUiThread(new Runnable() {
					public void run() {
						stopProgress();
						if (! friendAdded.isEmpty())
						{
							//if a friend was added, update the list with their user account
							addSingleFriend();
						}		
						Toast.makeText(c, sb.toString(), Toast.LENGTH_LONG).show();
					}
				});

			}
		};
		Thread thread = new Thread(null, addFriend, "AddFriendThread");
		thread.start();
		setupProgress(getString(R.string.progress_adding_friend));
		showProgress();
	}
	
	//when a friend is added, get their user account and add it to the list 
	//so the whole list does not need to be reloaded
	private void addSingleFriend()
	{
			//retrieve the account in separate thread
			loadSingleFriend = new Runnable() {
				@Override
				public void run() {
					// get the user account from the database
					UserAccount acct = accountHandler.getUser(friendAdded);
					acctToAdd = acct;
					runOnUiThread(new Runnable(){
						public void run(){
							if(acctToAdd != null)
							{
								friends.add(acctToAdd);
								FA.add(acctToAdd);
								FA.notifyDataSetChanged();
								//clear the callback fields after friend is added
								acctToAdd = null;
								friendAdded = "";
							}
							stopProgress();
						}
					});	
				}
			};
			Thread thread = new Thread(null, loadSingleFriend, "GetFriendThread");
			thread.start();
			setupProgress(getString(R.string.progress_updating_list));
			showProgress();
	}
	 // ArrayAdapter to bind useraccounts to list view
	private class FriendAdapter extends ArrayAdapter<UserAccount> 
	{

		private ArrayList<UserAccount> friends;
		private Runnable loadImage;
		Context c;

		public FriendAdapter(Context context, int textViewResourceId, ArrayList<UserAccount> friends) {
			super(context, textViewResourceId, friends);
			this.friends = friends;
			this.c = context;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			View row = convertView;
			if (row == null) 
			{
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = vi.inflate(R.layout.friendrow, null);
			}
			
			UserAccount account = friends.get(position);
			if (account != null) 
			{
				TextView nameTxt = (TextView) row.findViewById(R.id.friendrow_txtName);
				final ImageView imgView = (ImageView) row.findViewById(R.id.friendrow_thumbnail);

				if (nameTxt != null) 
					nameTxt.setText(account.getuName());                            
				/*
				 * Set thumbnail of tag image to imageview
				 */
					
				 if(imgView != null)
				 {

					 if(account.getImage() != null)
					 {
						 final String url = account.getImage();
						 //retrieve the tags in separate thread
						 loadImage = new Runnable() 
						 {
							 @Override
							 public void run() 
							 {
								 int width = (int)(getResources().getDimension(R.dimen.thumbnail_width));
								 int height = (int)(getResources().getDimension(R.dimen.thumbnail_height));

								 ImageHandler handler = new ImageHandler(c);
								 final Bitmap b = handler.getScaledBitmapFromUrl(url, width, height);
								 //if the thumbnail gets returned, set it in the image view
								 if (b != null)
								 {
									 runOnUiThread(new Runnable()
									 {
										 public void run()
										 {
											 imgView.setImageBitmap(b);
										 }
									 });
								 }		
							 }
						 };
						 Thread thread = new Thread(null, loadImage, "LoadImageThread");
						 thread.start();
					 }
				 }       
			}
			return row;
		}//end getView
	}//end FriendAdapter
	
	
}//end addFriendActivity
