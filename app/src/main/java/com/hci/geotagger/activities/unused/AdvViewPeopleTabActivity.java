package com.hci.geotagger.activities.unused;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.hci.geotagger.R;
import com.hci.geotagger.activities.UserProfileActivity;
import com.hci.geotagger.activities.common.BaseListActivity;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.ImageHandler;
import com.hci.geotagger.connectors.AdventureHandler;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.UserAccount;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Taglist activity displays a scrollable list of tags for 
 * a given user account and gives the user the ability
 * to click on them to view the tag. 
 */

public class AdvViewPeopleTabActivity extends BaseListActivity {
	private ArrayList<UserAccount> ua = null;
	private UserAdapter UA;	
	private AdventureHandler advHandler;
	private Adventure adventure;
	private Runnable viewPeople;
	private int userID;
	private int CONTEXT_DELETE_ID = 1;	
	private ImageHandler imageHandler;	

	HashMap<String, Bitmap> thumbCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adv_view_people_tab);		
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		adventure = (Adventure) bundle.getSerializable("adventure");
		
		// initialize objects
		imageHandler = new ImageHandler(this);
		thumbCache = new HashMap<String, Bitmap>();
		ua = new ArrayList<UserAccount>();
		this.UA = new UserAdapter(this, R.layout.row, ua);
		setListAdapter(this.UA);
		registerForContextMenu(getListView());
		
		advHandler = new AdventureHandler(this);				
		// action when a list item is clicked
		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				UserAccount u = (UserAccount) parent.getItemAtPosition(position);

				// Open tag view, pass current position in taglist along with
				// whole taglist
				// this can be used to implement swype through tags without
				// downloading each time
				Intent i = new Intent(parent.getContext(),
						UserProfileActivity.class);
				i.putExtra("startPos", position);
				i.putExtra("peopleList", ua);
				// if user is viewing their own tag list, open the tag view
				// expecting a result incase
				// the user deletes the tag from the tagview
				if (userID == UserSession.CURRENTUSER_ID)
					startActivityForResult(i, CONTEXT_DELETE_ID);
				else
					startActivity(i);
			}
		});

		// get the UserID that was passed to this activity to determine whose
		// users to load
//		Intent i = getIntent();
//		int id = i.getIntExtra("id", -1);
//		if (id != -1) {
//			this.userID = id;
//			retrievePeople();
//		}
		retrievePeople();
	}

	// setup separate thread to retrieve users
	private void retrievePeople() {
		// retrieve the tags in separate thread
		viewPeople = new Runnable() {
			@Override
			public void run() {								
				// get the user's adventure people and display
				// them in the list
				GetPeople();
				// after getting users, download the images to the cache and
				// update the ui
				loadImagesToCache();
			}
		};
		Thread thread = new Thread(null, viewPeople, "GetUserThread");
		thread.start();
		setupProgress(getString(R.string.progress_retrieving_users));
		showProgress();
	}

	/*
	 * Functions
	 */	

	// get the users from the database, then create user objects for them and add
	// them to the array list
	private void GetPeople()  
	{
		ua = advHandler.getPeopleInAdventure(adventure.getId());
	}

	private void loadImagesToCache() {
		// retrieve the users in separate thread
		Runnable loadImages = new Runnable() {
			@Override
			public void run() {
				int width = (int)(getResources().getDimension(R.dimen.thumbnail_width));
				int height = (int)(getResources().getDimension(R.dimen.thumbnail_height));

				// loop through users and cache their images if they have them
				for (UserAccount u : ua) {
					if(u != null)
					{
						String url = u.getImage();
						// if user has image url, download image and cache it
						if (!url.isEmpty()) {
							final Bitmap b = imageHandler.getScaledBitmapFromUrl(url, width, height);
							if (b != null)
								thumbCache.put(url, b);
						}
					}	
				}
				// update array adapter on ui thread
				runOnUiThread(returnRes);
			}
		};
		Thread thread = new Thread(null, loadImages, "LoadImageThread");
		thread.start();

	}

	/*
	 * Bindings
	 */
	// update list with the results of getPeople
	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if (ua != null && ua.size() > 0) {
				UA.notifyDataSetChanged();
				for (int i = 0; i < ua.size(); i++)
					UA.add(ua.get(i));
			}
			stopProgress();
			UA.notifyDataSetChanged();
		}
	};
	
	// arrayadapter to bind users to list view
	private class UserAdapter extends ArrayAdapter<UserAccount> {

		private ArrayList<UserAccount> ua;
		Context c;

		public UserAdapter(Context context, int textViewResourceId,
				ArrayList<UserAccount> ua) {
			super(context, textViewResourceId, ua);
			this.ua = ua;
			this.c = context;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = vi.inflate(R.layout.row, null);
			}
			UserAccount u = ua.get(position);
			if (u != null) {
				TextView nameTxt = (TextView) row
						.findViewById(R.id.row_txtName);
				TextView descTxt = (TextView) row
						.findViewById(R.id.row_txtdesc);
				TextView timeTxt = (TextView) row
						.findViewById(R.id.row_txtTime);
				final ImageView imgView = (ImageView) row
						.findViewById(R.id.row_thumbnail);

				if (nameTxt != null)
					nameTxt.setText(u.getName());
				if (descTxt != null)
					descTxt.setText(u.getDescription());
				if (timeTxt != null) {
					Date date = u.getCreatedDateTime();
					SimpleDateFormat df = new SimpleDateFormat(
							Constants.DATETIME_FORMAT);
					String formatted = df.format(date);
					timeTxt.setText(formatted);
				}
				/*
				 * Set thumbnail of user image to imageview
				 */
				if (imgView != null) {
					if (!u.getImage().isEmpty()) {
						final String url = u.getImage();
						// first try to get image from cache
						if (thumbCache.containsKey(url)) {
							imgView.setImageBitmap(thumbCache.get(url));
							Log.d("UserAdapter", "Got image from cache!");
						} else {
							Log.d("UserAdapter",
									"User has imageurl but it isnt in cache");

						}// end else
					}// end if imgurl
					else {
						Bitmap default_bitmap = BitmapFactory.decodeResource(
								c.getResources(), R.drawable.icon);
						imgView.setImageBitmap(default_bitmap);
					}
				} // end if imgview
			}// end if t
			return row;
		}// end getView

	}// end useradapter
}
