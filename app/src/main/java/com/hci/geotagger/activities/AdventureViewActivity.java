package com.hci.geotagger.activities;

import java.util.ArrayList;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.AdvViewTagFragment.UpdateDatabaseListener;
import com.hci.geotagger.activities.common.BaseActivity;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageReq;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageRsp;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.gui.MapViewHandler;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.Tag;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class should be re-written since TabActivity is deprecated. Currently, this activity allows a user to view 
 * Adventures.
 */
public class AdventureViewActivity extends BaseActivity 
implements UpdateDatabaseListener, DbMessageResponseInterface {
	private String TAG = "AdventureViewActivity";
	
	private static final int EDIT_ACTIVITY = 1;

	private TextView descriptionTextView;
	private Adventure adventure;
	private ActionBar actionBar;
	
	private GeotaggerApplication mApp = null;
	private static DbHandlerResponse rspHandler;
	
	ArrayList<Tag> tags = new ArrayList<Tag>();
	
	/**
	 * Populates view with Adventure's list of tags
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adventure_view_wtabs);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		adventure = (Adventure) bundle.getSerializable("adventure");
		
		if (adventure.getCreatorID() == UserSession.CURRENTUSER_ID)
			optionsMenuID = R.menu.adventure_view;
		else
			optionsMenuID = R.menu.basic_options;

		Log.d(TAG, "onCreate: adventure: id="+adventure.getId()+",name="+adventure.getName());

		mApp = (GeotaggerApplication)getApplication();
		if (mApp != null) {
			rspHandler = new DbHandlerResponse(TAG, this, this);
			mApp.addResponseHandler(rspHandler);
		}

		initializeUIComponents();

		actionBar = getActionBar();
		descriptionTextView = (TextView) findViewById(R.id.adventureView_desc);
		
		updateGuiData();
	}
	
	private void updateGuiData() {
		actionBar.setTitle(adventure.getName());
		descriptionTextView.setText(adventure.getDescription());		
	}
	
	private void updateTags(ArrayList<Tag> tags) {
		mapViewHandler.addLocations(tags);
		tagsViewFragment.setTags(tags);
	}
	
	@Override
	public void onDatabaseChanged() {
		setupProgress(getString(R.string.progress_loading));
		showProgress();
//		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_ADVENTURE_TAGS, adventure, DbHandlerConstants.FLAG_CACHE);
		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_ADVENTURE_TAGS, adventure);
	}

	private int tagImageGetID = 0;
	
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		switch (action) {
		case DbHandlerConstants.DBMSG_GET_ADVENTURE_TAGS:
			if (success) {
				tags = (ArrayList<Tag>)response;
				if (tags != null && tags.size() > 0) {
					DbHandlerScaledImageReq gsi = new DbHandlerScaledImageReq();
					gsi.width = (int) (getResources().getDimension(R.dimen.thumbnail_width));
					gsi.height = (int) (getResources().getDimension(R.dimen.thumbnail_height));

					int numImages = 0;
					// Calculate the number of tags that have images
					for (Tag t : tags) {
						String url = t.getImageUrl();
						if (url != null && !url.equals(""))
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
							if (url != null && !url.equals("")) {
								gsi.urls[curUrl++] = url;
							}
						}
						tagImageGetID = mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_SCALED_IMAGES, gsi);
						return;
					}
				}
				updateTags(tags);
			}
			stopProgress();
			break;
		case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
			if (msgID == tagImageGetID) {
				if (success) {
					DbHandlerScaledImageRsp sir = (DbHandlerScaledImageRsp)response;
					setTagThumbnail(sir.url, sir.bitmap);
				}
				if (done) {
					updateTags(tags);
					stopProgress();
				}
			}
			break;
		}
	}

	private void setTagThumbnail(String url, Bitmap bitmap) {
		for (Tag tag : tags) {
			String tagurl = tag.getImageUrl();
			if (tagurl != null && tagurl.equals(url)) {
				tag.setThumbnail(bitmap);
			}
		}
	}
	
	
	private FrameLayout tagsViewContent;
	private LinearLayout mapViewContent;
	private AdvViewTagFragment tagsViewFragment = null;
	private MapViewHandler mapViewHandler;
	private FragmentManager fm;
	private FragmentTransaction transaction;

	private LinearLayout tagsTabBtn;	// press to view tag comments
	private LinearLayout mapTabBtn;	// press to view activity's tags position on a map


	private void initializeUIComponents() {
		
		tagsTabBtn = (LinearLayout) findViewById(R.id.tags_button);
		mapTabBtn = (LinearLayout) findViewById(R.id.map_button);

		tagsViewContent = (FrameLayout) findViewById(R.id.advViewContent);
		mapViewContent = (LinearLayout) findViewById(R.id.mapViewContent);
		
		// Start with the Tags List view visible and the Map gone
		mapViewContent.setVisibility(View.GONE);
		tagsViewContent.setVisibility(View.VISIBLE);

		Fragment mapFragment = this.getFragmentManager().findFragmentById(R.id.map);
		mapViewHandler = new MapViewHandler(mapFragment, this);

		
		setupProgress(getString(R.string.progress_loading));
		showProgress();
		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_ADVENTURE_TAGS, adventure);

		
		tagsViewFragment = new AdvViewTagFragment();

		fm = getFragmentManager();

		// show description tab by default
		transaction = fm.beginTransaction();
		transaction.replace(R.id.advViewContent, tagsViewFragment);
		transaction.commit();

		tagsTabBtn.setEnabled(false); // do not allow multiple clicks causing
										// multiple transactions

		Log.d(TAG, "Displaying new UI elements");

		// implement the button listeners
		tagsTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!tagsViewFragment.isAdded()) {

					// show the description fragment
					transaction = fm.beginTransaction();
					transaction.replace(R.id.tagViewContent, tagsViewFragment);

					transaction.commit();
				} else {
					transaction.show(tagsViewFragment);
				}

				if (tagsViewContent.getVisibility() != View.VISIBLE) {
					mapViewContent.setVisibility(View.GONE);
					tagsViewContent.setVisibility(View.VISIBLE);
				}

				// disable the description button, re-enable the others
				tagsTabBtn.setEnabled(false);
				mapTabBtn.setEnabled(true);

				// displayTagInformation
				Log.d(TAG, "Description Toggled");

			}
		});

		mapTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Mapview toggle button pressed");

				if (mapViewContent.getVisibility() != View.VISIBLE) {
					tagsViewContent.setVisibility(View.GONE);
					mapViewContent.setVisibility(View.VISIBLE);
				}

				// Need to get the list of locations and call map view handler
//				mapViewHandler.addLocation(geo);

				// set location name
//				mapViewHandler.setLocationName(currentTag.getName());

				mapViewHandler.show();

				// disable the map view button, re-enable the others
				mapTabBtn.setEnabled(false);
				tagsTabBtn.setEnabled(true);
			}
		});

	}
	
	/**
	 * Setup the options for this activity
	 */
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId())
		{
		case R.id.action_edit:
			intent = new Intent(getBaseContext(), EditAdventureActivity.class);
			Bundle bundle1 = new Bundle();
			bundle1.putSerializable("adventure", adventure);
			intent.putExtras(bundle1);
			
			startActivityForResult(intent, EDIT_ACTIVITY);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
        	switch(requestCode) {
        	case EDIT_ACTIVITY:
        		onDatabaseChanged();
        		/*
        		Bundle bundle = data.getExtras();		
        		adventure = (Adventure) bundle.getSerializable("adventure");
        		updateGuiData();
        		
				ArrayList<Tag> tags = adventure.getTags();
				updateTags(tags);*/
        		break;
            }
        } else {
        	
        }
    }

}
