package com.hci.geotagger.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.common.BaseListActivity;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageReq;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageRsp;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.Tag;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Taglist activity displays a scrollable list of tags for 
 * a given user account and gives the user the ability
 * to click on them to view the tag. 
 */

public class TagListActivity extends BaseListActivity implements DbMessageResponseInterface {
	private String TAG = "TagListActivity";

	private ArrayList<Tag> tags = null;
	private TagAdapter TA;
	private int userID;
	private int TAGVIEW_ACTIVITY = 1;
	private int flag;	
	private Adventure adventure;
	
	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication mApp;
	private int tagsImageGetID;

	HashMap<String, Bitmap> thumbCache;
	private int deletePosition;
	
	/**
	 * Override the onCreate method that initializes some handler classes as well as retrieves the tags associated
	 * with this users account asynchronously via the dbHandler interface. Images associated with tags are also loaded
	 * to local cache for quick access in subsequent calls to this Activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_taglist);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		adventure = (Adventure) bundle.getSerializable("adventure");
		
		// initialize objects
		thumbCache = new HashMap<String, Bitmap>();
		tags = new ArrayList<Tag>();
		this.TA = new TagAdapter(this, R.layout.row, tags);
		setListAdapter(this.TA);
		registerForContextMenu(getListView());

		// action when a list item is clicked
		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Tag t = (Tag) parent.getItemAtPosition(position);				
				// Open tag view, pass current position in taglist along with
				// whole taglist
				// this can be used to implement swype through tags without
				// downloading each time
			
				Intent i = new Intent(parent.getContext(),TagViewActivity.class);
				
				i.putExtra("startPos", position);
				i.putExtra("tagList", tags);
				// if user is viewing their own tag list, open the tag view
				// expecting a result in case
				// the user deletes the tag from the tagview
				if (userID == UserSession.CURRENTUSER_ID)
					startActivityForResult(i, TAGVIEW_ACTIVITY);
				else
					startActivity(i);
			}
		});

		
		mApp = (GeotaggerApplication)getApplication();
		if (mApp != null) {
			rspHandler = new DbHandlerResponse(TAG, this, this);
			mApp.addResponseHandler(rspHandler);
		}

		
		setupProgress(getString(R.string.progress_retrieving_tags));
		showProgress();
		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_ALL_TAGS);
		
		if(intent.getFlags() == 1) {
			flag = 1;
		}		
	}

	private void updateTagAdapter() {
		if (tags != null && tags.size() > 0) {
			TA.notifyDataSetChanged();
			for (int i = 0; i < tags.size(); i++)
				TA.add(tags.get(i));
		}
		TA.notifyDataSetChanged();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		Log.d(TAG, "Entered DBGetCallback");
		
		if (success) {
			switch (action) {
			case DbHandlerConstants.DBMSG_GET_ALL_TAGS:
				tags = (ArrayList<Tag>)response;
				if (tags != null && tags.size() > 0) {
					DbHandlerScaledImageReq gsi = new DbHandlerScaledImageReq();
					gsi.width = (int) (getResources().getDimension(R.dimen.thumbnail_width));
					gsi.height = (int) (getResources().getDimension(R.dimen.thumbnail_height));

					int numImages = 0;
					// Calculate the number of comments that have images
					for (Tag t : tags) {
						String url = t.getImageUrl();
						if (url != null && !url.equals("") && !thumbCache.containsKey(url))
							numImages++;
					}
					
					if (numImages > 0) {
						gsi.urls = new String[numImages];
						int curUrl = 0;
						// loop through tags and cache their images if they have them
						for (Tag t : tags) {
							String url = t.getImageUrl();
							Log.d(TAG, "URL is " + url);
							// if tag has image url, download image and cache it
							if (url != null && !url.equals("") && !thumbCache.containsKey(url)) {
								gsi.urls[curUrl++] = url;
							}
						}
						tagsImageGetID = mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_SCALED_IMAGES, gsi);
						return;
					}
					// There are no images, so update the Tags Adapter
					updateTagAdapter();
				}
				stopProgress();
				break;
			case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
				if (msgID == tagsImageGetID) {
					if (success) {
						DbHandlerScaledImageRsp sir = (DbHandlerScaledImageRsp)response;
						thumbCache.put(sir.url, sir.bitmap);
					}
					if (done) {
						updateTagAdapter();
						stopProgress();
					}
				}
				break;
			case DbHandlerConstants.DBMSG_DELETE:
				stopProgress();
				// once the tag is removed from the db, remove it
				// from the arraylist and update.
				TA.remove(tags.get(deletePosition));
				TA.notifyDataSetChanged();
				tags.remove(deletePosition);
				Toast.makeText(TagListActivity.this, getResources().getString(R.string.toast_deletetag_success), Toast.LENGTH_SHORT).show();
				break;
			}
		} else {
			switch (action) {
			case DbHandlerConstants.DBMSG_GET_ALL_TAGS:
				stopProgress();
				break;
			case DbHandlerConstants.DBMSG_DELETE:
				Toast.makeText(TagListActivity.this, getResources().getString(R.string.toast_deletetag_failure), Toast.LENGTH_SHORT).show();
				break;
			case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
				if (done) {
					updateTagAdapter();
					stopProgress();
				}
				break;
			}
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// when the tagview activity is closed, check to see if a delete call
		// was sent back
		if (requestCode == TAGVIEW_ACTIVITY) {
			if (resultCode == RESULT_OK) {
				if (data.hasExtra(TagViewActivity.TAG_DELETED)) {
					int removeIndex = data.getIntExtra(TagViewActivity.TAG_DELETED, -1);
					// if so, delete tag and update list (tag was deleted from
					// database in other activity)
					if (removeIndex >= 0) {
						deleteTag(removeIndex);
					}
				} else if (data.hasExtra(TagViewActivity.TAG_UPDATED)) {
					//TODO: Need to update the display of the TAG
				}
			}
		}
	}// onActivityResult

	/*
	 * Event Handlers
	 */

	// create context menu when list item is long-pressed
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		// show delete context menu only if user is viewing their own tag list
		if (this.userID == UserSession.CURRENTUSER_ID) 
		{
			menu.setHeaderTitle("Tag " + tags.get(info.position).getName());
			if(flag == 1) {				
				menu.add(getResources().getString(R.string.add));
			} else {				
				menu.add(getResources().getString(R.string.delete));	
			}
		}
	}

	// actions for context menu items
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		// delete the selected tag
		if (item.getTitle().equals("Delete")) 
		{
			deleteTag(info.position);
		}
		else if(item.getTitle().equals("Add"))
		{
			addTagToAdventure(info.position);
		}
		else if(item.getTitle().equals("Remove") && flag == 1)
		{
			removeTagFromAdventure(info.position);
		}
		return true;
	}

	/**
	 * If selected in context menu or returned from onActivityResult, deletes this tag from list. Currently, the tag is 
	 * only deleted on the front end, and not the backend server side.
	 * @param position Index of tag to be deleted.
	 */
	private void deleteTag(final int position) {
		deletePosition = position;
		setupProgress(getString(R.string.progress_deleting_tag));
		showProgress();
		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_DELETE, tags.get(position));
	}
	
	/**
	 * Adds an existing tag to an adventure if the user is in an adventure and wants to add an existing tag.
	 */
	private void addTagToAdventure(final int position) {				
		Runnable addTag = new Runnable() {
			@Override
			public void run() {
				boolean success = adventure.addStoreTagList(tags.get(position));
				if (success) {
					runOnUiThread(new Runnable() {
						public void run() {
							stopProgress();
							Toast.makeText(TagListActivity.this, "Tag Added!", Toast.LENGTH_SHORT).show();
						}
					});
				} else
					Toast.makeText(TagListActivity.this, "Error Adding Tag...", Toast.LENGTH_SHORT).show();
			}
		};
		Thread thread = new Thread(null, addTag, "AddTagThread");
		thread.start();
		setupProgress(getString(R.string.progress_adding_tag));
		showProgress();
	}
	
	/**
	 * Removes an existing tag from an adventure.
	 */
	private void removeTagFromAdventure(final int position) {				
		Runnable removeTag = new Runnable() {
			@Override
			public void run() {
				boolean success = adventure.removeStoreTagList(tags.get(position));
				if (success) {
					runOnUiThread(new Runnable() {
						public void run() {
							stopProgress();
							Toast.makeText(TagListActivity.this,
									"Tag Removed!", Toast.LENGTH_SHORT).show();
						}
					});
				} else
					Toast.makeText(TagListActivity.this,
							"Error Removing Tag...", Toast.LENGTH_SHORT).show();
			}
		};
		Thread thread = new Thread(null, removeTag, "AddTagThread");
		thread.start();
		setupProgress(getString(R.string.progress_deleting_tag));
		showProgress();
	}

	/**
	 * Arrayadapter to bind tags to list view
	 * 
	 *
	 */
	private class TagAdapter extends ArrayAdapter<Tag> {

		private ArrayList<Tag> tags;
		Context c;
		private TextView nameTxt, timeTxt, descTxt;

		public TagAdapter(Context context, int textViewResourceId,
				ArrayList<Tag> tags) {
			super(context, textViewResourceId, tags);
			this.tags = tags;
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
			Tag t = tags.get(position);
			if (t != null) {
				nameTxt = (TextView) row
						.findViewById(R.id.row_txtName);
				descTxt = (TextView) row
						.findViewById(R.id.row_txtdesc);
				timeTxt = (TextView) row
						.findViewById(R.id.row_txtTime);
				
				final ImageView imgView = (ImageView) row
						.findViewById(R.id.row_thumbnail);

				if (nameTxt != null)
					nameTxt.setText(t.getName());
				if (descTxt != null)
					descTxt.setText(t.getDescription());
				if (timeTxt != null) {
					Date date = t.getCreatedDateTime();
					SimpleDateFormat df = new SimpleDateFormat(
							Constants.DATETIME_FORMAT);
					String formatted = df.format(date);
					timeTxt.setText(formatted);
				}
				/*
				 * Set thumbnail of tag image to imageview
				 */
				if (imgView != null) {
					if (!t.getImageUrl().isEmpty()) {
						final String url = t.getImageUrl();
						// first try to get image from cache
						if (thumbCache.containsKey(url)) {
							imgView.setImageBitmap(thumbCache.get(url));
							Log.d("TagAdapter", "Got image from cache!");
						} else {
							Log.d("TagAdapter",
									"Tag has imageurl but it isnt in cache");

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

	}// end tagadapter
}
