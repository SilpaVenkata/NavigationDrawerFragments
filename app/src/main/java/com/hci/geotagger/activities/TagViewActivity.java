package com.hci.geotagger.activities;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.util.Log;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.CommentViewFragment.ICommentViewCallBack;
import com.hci.geotagger.activities.DescriptionViewFragment.IDescriptionViewCallback;
import com.hci.geotagger.activities.common.BaseActivity;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageReq;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageRsp;
import com.hci.geotagger.gui.MapViewHandler;
import com.hci.geotagger.gui.ScaleImageView;
import com.hci.geotagger.gui.ScaleImageView.ScaleImageCallbacks;
import com.hci.geotagger.objects.Comment;
import com.hci.geotagger.objects.GeoLocation;
import com.hci.geotagger.objects.Tag;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;


/**
 * This class allows user to view all information associated with a given tag.
 * This includes comments, location data, and time stamp. This class implements
 * sensorEventListener although it does not use its overrided
 * methods(onAccuracyChanged and onSensorChanged) as they are not used in this
 * iteration of geoTagger. Once the developer is sure, this implentation can be
 * removed from this class. This class also implements ICommentViewCallBack and
 * IDescriptionViewCallback which are used to populate the
 * DesciptionViewFragment and CommentViewFragment after they have been resolved.
 * Note these callbacks were deemed neccessary since we must be sure the
 * fragments were completely instantiated and rendered before populating them
 * with downloaded information. Therefore, we must utilize callbacks used in
 * those fragments.
 */
// public class TagViewActivity extends Activity implements SensorEventListener

public class TagViewActivity extends BaseActivity implements
		SensorEventListener, ICommentViewCallBack, IDescriptionViewCallback, DbMessageResponseInterface,
		ScaleImageCallbacks {
	private static final String TAG = "TagViewActivity";

	public static final String TAG_UPDATED = "Updated";
	public static final String TAG_DELETED = "Deleted";
	public static final String EXTRA_TAGID = "TagID";
	
	ActionBar actionBar;
	TextView txt_ownerAndTime, txt_tagLocation,
			txt_tagDescription, txt_Rating, txt_currentLoc, txt_distance,
			txt_latLong;
	ImageView img_tagImage, commentrow_thumbnail, compassTriangle;
	ImageView img_commentImage;
	ImageView btnRating;
	Button commentBtn, navBtn, handle, revealedHandle;
	RelativeLayout drawer;
	EditText commentTxt;
	Dialog ratingDialog;
	RatingBar ratingBar;
	ListView commentList;
	String url;

	private Drawable icon;

	private DecimalFormat lldf = new DecimalFormat("#.000000");

	private long currentTagID = 0;
	private Tag currentTag = null;

	// fields needed for the location on tag
	private GeoLocation geo;

	private ArrayList<Comment> comments = null;

	private HashMap<String, Bitmap> thumbCache;

	/*
	 * Below variables are for New MobSci UI
	 */

	private LinearLayout lowerArea;

	private LinearLayout descriptionTabBtn; // press to view tag description
	// Below comments variable renamed to commentsToggleBtnto prevent collission
	// with comments above
	private LinearLayout commentsTabBtn; // press to view tag comments
	private LinearLayout mapViewTabBtn; // press to view tag's position on a map
	private FrameLayout tagViewContent; // utilizes a frame layout to use
										// fragments
	private LinearLayout mapViewContent;

	// changed below because polymorphism didn't seem to work for some
	// reason(only superclass methods visible)
	private Fragment commentViewFragment = null; // fragment that contains the
													// comments for the tag
	private Fragment descriptionViewFragment = null; // fragment that contains
														// the description for
														// the tag
	private MapViewHandler mapViewHandler;
	private FragmentManager fm; // FragmentManager that allows for switching
								// between fragments
	private FragmentTransaction transaction; // used to switch between fragments

	// New UI Elements
	TextView descriptionTxtView;
	TextView dateCreatedTxtView;
	TextView timeStampTxtView;
	TextView lblLocationTxtView;
	TextView locationTxtView;
	ScaleImageView tagPhotoImgView;

	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication mApp = null;
	private int removeCommentIndex;
	private int commentImageGetID;
	private int tagImageGetID;

	private boolean commentsInitialized = false;

	/**
	 * Overidded onCreate method that instantiates ui elements, downloads
	 * associated tag information, and displays them to the UI once they are
	 * finished. 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_view);
		
		mApp = (GeotaggerApplication)getApplication();
		if (mApp != null) {
			rspHandler = new DbHandlerResponse(TAG, this, this);
			mApp.addResponseHandler(rspHandler);
		}
		
		// if the current tag is owned by the logged in user, give option to
		// delete it
		/* TODO: Need to determine if this Tag is owned by this user
		if (currentTag.getOwnerId() == UserSession.CURRENT_USER.getId()) {
			optionsMenuID =  R.menu.view_tag_withdelete;
		} else {
			optionsMenuID = R.menu.view_tag;			
		}
		*/
		optionsMenuID = R.menu.view_tag;			

		// set up comments
		comments = new ArrayList<Comment>();

		thumbCache = new HashMap<String, Bitmap>();

		lowerArea = (LinearLayout)findViewById(R.id.below_image);
		
		descriptionTabBtn = (LinearLayout) findViewById(R.id.description_button);																		// buttons
		commentsTabBtn = (LinearLayout) findViewById(R.id.comments_button);
		mapViewTabBtn = (LinearLayout) findViewById(R.id.map_button);

		tagViewContent = (FrameLayout) findViewById(R.id.tagViewContent);
		mapViewContent = (LinearLayout) findViewById(R.id.mapViewContent);
		mapViewContent.setVisibility(View.GONE);
		tagViewContent.setVisibility(View.VISIBLE);

		commentViewFragment = new CommentViewFragment(this);
		descriptionViewFragment = new DescriptionViewFragment(this);

		Fragment mapFragment = getFragmentManager().findFragmentById(R.id.map);
		mapViewHandler = new MapViewHandler(mapFragment, this);

		fm = getFragmentManager();

		// show description tab by default
		transaction = fm.beginTransaction();
		transaction.replace(R.id.tagViewContent, descriptionViewFragment);
		transaction.commit();

		descriptionTabBtn.setEnabled(false); // do not allow multiple clicks causing
										// multiple transactions

		Log.d(TAG, "Displaying new UI elements");

		// implement the button listeners
		descriptionTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!descriptionViewFragment.isAdded()) {

					// show the description fragment
					transaction = fm.beginTransaction();
					transaction.replace(R.id.tagViewContent, descriptionViewFragment);

					transaction.commit();
				} else {
					transaction.show(descriptionViewFragment);
				}

				if (tagViewContent.getVisibility() != View.VISIBLE) {
					mapViewContent.setVisibility(View.GONE);
					tagViewContent.setVisibility(View.VISIBLE);
				}

				// disable the description button, re-enable the others
				descriptionTabBtn.setEnabled(false);
				commentsTabBtn.setEnabled(true);
				mapViewTabBtn.setEnabled(true);

				// displayTagInformation
				Log.d(TAG, "Description Toggled");

			}
		});

		commentsTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Comments toggle button pressed");

				((CommentViewFragment) commentViewFragment).setTagIndex(currentTag);
				
				if (!commentViewFragment.isAdded()) {
					// show the description fragment
					transaction = fm.beginTransaction();
					transaction.replace(R.id.tagViewContent, commentViewFragment);
					transaction.commit();
				} else {
					transaction.show(commentViewFragment);
				}

				if (tagViewContent.getVisibility() != View.VISIBLE) {
					mapViewContent.setVisibility(View.GONE);
					tagViewContent.setVisibility(View.VISIBLE);
				}

				// disable the comments button, re-enable the others
				descriptionTabBtn.setEnabled(true);
				commentsTabBtn.setEnabled(false);
				mapViewTabBtn.setEnabled(true);

			}
		});

		mapViewTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Mapview toggle button pressed");

				if (mapViewContent.getVisibility() != View.VISIBLE) {
					tagViewContent.setVisibility(View.GONE);
					mapViewContent.setVisibility(View.VISIBLE);
				}

				mapViewHandler.addLocation(currentTag);

				// set location name
//				mapViewHandler.setLocationName(currentTag.getName());

				mapViewHandler.show();

				// disable the map view button, re-enable the others
				descriptionTabBtn.setEnabled(true);
				commentsTabBtn.setEnabled(true);
				mapViewTabBtn.setEnabled(false);
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		if (lowerArea.getVisibility() == View.VISIBLE) {
			Intent returnIntent = new Intent();
			returnIntent.putExtra(TAG_UPDATED, currentTagID);
			setResult(RESULT_OK, returnIntent);

			finish();
		} else {
			lowerArea.setVisibility(View.VISIBLE);
		}
	}

	
	private void updateCommentFragment() {
		commentsInitialized = true;
		((CommentViewFragment) commentViewFragment).clearComments();
		((CommentViewFragment) commentViewFragment).setCommentThumbCache(thumbCache);
		((CommentViewFragment) commentViewFragment).notifyCommentAdapterDataChanged();
		for (int i = 0; i < comments.size(); i++) {
			((CommentViewFragment) commentViewFragment).addComment(comments.get(i));
		}
		// commentList.setSelection(comments.size()-1);
		((CommentViewFragment) commentViewFragment).setCommentListSelection(comments.size() - 1);
		((CommentViewFragment) commentViewFragment).notifyCommentAdapterDataChanged();
	}
	
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		Log.d(TAG, "Entered DBGetCallback");
		switch (action) {
		case DbHandlerConstants.DBMSG_GET_TAG:
			// This is a response when we re-get the tag after an update
			currentTag = (Tag)response;
			/*
			String url = tag.getImageUrl();
			if (url != null && url.length() > 0) {
				DbHandlerScaledImageReq gsi = new DbHandlerScaledImageReq();
				gsi.width = (int) (getResources().getDimension(R.dimen.tag_image_max_size));
				gsi.height = (int) (getResources().getDimension(R.dimen.tag_image_max_size));
				gsi.urls = new String[1];
				gsi.urls[0] = url;

				getScaledImageID = app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_SCALED_IMAGES, gsi);
			} else {
			*/
			displayTag();
			break;

		case DbHandlerConstants.DBMSG_GET_TAG_COMMENTS:
			if (success) {
				comments = (ArrayList<Comment>)response;
				if (comments != null && comments.size() > 0) {
					DbHandlerScaledImageReq gsi = new DbHandlerScaledImageReq();
					gsi.width = (int) (getResources().getDimension(R.dimen.thumbnail_width));
					gsi.height = (int) (getResources().getDimension(R.dimen.thumbnail_height));

					int numImages = 0;
					// Calculate the number of comments that have images
					for (Comment c : comments) {
						String url = c.getImageURL();
						if (url != null && !url.equals("") && !thumbCache.containsKey(url))
							numImages++;
					}
					
					if (numImages > 0) {
						gsi.urls = new String[numImages];
						int curUrl = 0;
						// loop through tags and cache their images if they have them
						for (Comment c : comments) {
							String url = c.getImageURL();
							Log.d(TAG, "URL is " + url);
							// if tag has image url, download image and cache it
							if (url != null && !url.equals("") && !thumbCache.containsKey(url)) {
								gsi.urls[curUrl++] = url;
							}
						}
						commentImageGetID = mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_SCALED_IMAGES, gsi);
						return;
					}
					updateCommentFragment();
				}
			}
			stopProgress();
			break;
		case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
			if (msgID == commentImageGetID) {
				if (success) {
					DbHandlerScaledImageRsp sir = (DbHandlerScaledImageRsp)response;
					thumbCache.put(sir.url, sir.bitmap);
				}
				if (done) {
					updateCommentFragment();
					stopProgress();
				}
			} else if (msgID == tagImageGetID) {
				if (success) {
					DbHandlerScaledImageRsp sir = (DbHandlerScaledImageRsp)response;

					if (Constants.USE_NEW_TAGVIEW_UI) {
						tagPhotoImgView.setImageBitmap(sir.bitmap);
						tagPhotoImgView.setHandleTouchEvents(true);
					} else {
						img_tagImage.setImageBitmap(sir.bitmap);
					}
				}
			}
			break;
			
		case DbHandlerConstants.DBMSG_DELETE:
			// once the tag is removed from the db, remove it
			// from the arraylist and update.
			comments.remove(removeCommentIndex);
			updateCommentFragment();
			stopProgress();
			Toast.makeText(TagViewActivity.this, "Comment Removed", Toast.LENGTH_SHORT).show();
			break;
		case DbHandlerConstants.DBMSG_ADD:
			if (response != null && response instanceof Comment) {
				Comment comm = (Comment)response;
				comments.add(comm);
				position = comments.size()-1;
				
				if (position != null) {
					updateCommentFragment();
					commentList.setSelection(position);
				}
				position = null;
				commentTxt.setText("");
				stopProgress();
				String msg = this.getResources().getString(R.string.toast_addcomment_success);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				img_commentImage.setImageDrawable(icon);
			}
			break;
		}
	}


	/*
	 * Implemented so that the sensors for the compass are only working when the
	 * application is active and not just running in the background which would
	 * affect battery life
	 * 
	 * @see android.app.Activity#onResume()
	 */
	/**
	 * Overrided onResume method. Currently, sensorManager code was causing
	 * crash. DisplayTag must be called in this method as opposed to onCreate
	 * method to be assured that fragments are created. DisplayTag method
	 * populates fragments with information.
	 */
	@Override
	protected void onResume() {

		/**
		 * Kale Commented this out because it was effecting launching this
		 * activity. Have to investigate sensorManager.registerListener(this,
		 * sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
		 * SensorManager.SENSOR_DELAY_GAME); locationListener = new
		 * MyLocationListener();
		 * locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		 * 0, 0, locationListener);
		 * locationManager.requestLocationUpdates(LocationManager
		 * .NETWORK_PROVIDER, 0, 0, locationListener);
		 */
		setupTag();
//		displayTag();	// This must be called here so that onCreate has time to
						// create fragments. Otherwise crash..
		// retrieveComments();//causing crash at getComments method
		super.onResume();
	}

	/*
	 * Implemented so that the sensors for the compass are only working when the
	 * application is active and not just running in the background which would
	 * affect battery life
	 * 
	 * @see android.app.Activity#onPause()
	 */
	/**
	 * SensorManager code to prevent crashes
	 */
	@Override
	protected void onPause() {
		// sensorManager.unregisterListener(this, sensorAccelerometer);
		// sensorManager.unregisterListener(this, sensorMagneticField);
		/*
		 * Kale commented out because casuing crashing
		 * 
		 * sensorManager.unregisterListener(this);
		 * locationManager.removeUpdates(locationListener); locationListener =
		 * null;
		 */
		super.onPause();
	}

	/*
	 * Method must be defined to implement SensorEventListener
	 * 
	 * @see
	 * android.hardware.SensorEventListener#onAccuracyChanged(android.hardware
	 * .Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	/*
	 * This method detects whenever the sensors pick up a change in location
	 * and/or position. When a change is detected, the compass will be updated
	 * to point in regards to the current location.
	 * 
	 * Code modified from:
	 * http://sunil-android.blogspot.com/2013/02/create-our-android-compass.html
	 * 
	 * @see
	 * android.hardware.SensorEventListener#onSensorChanged(android.hardware
	 * .SensorEvent)
	 */

	/*
	 * @Override public void onSensorChanged(SensorEvent event) {
	 * switch(event.sensor.getType()) //determine what sensor event has occured
	 * { case Sensor.TYPE_MAGNETIC_FIELD: for(int i = 0; i < 3; i++) {
	 * valuesMagneticField[i] = event.values[i]; } break; case
	 * Sensor.TYPE_ACCELEROMETER: for(int i = 0; i < 3; i++) {
	 * valuesAccelerometer[i] = event.values[i]; } break; }
	 * 
	 * boolean success = SensorManager.getRotationMatrix(matrixR, matrixI,
	 * valuesAccelerometer, valuesMagneticField);
	 * 
	 * if(success) //if the rotation matrix was found above, update the compass
	 * { SensorManager.getOrientation(matrixR, matrixValues);
	 * //myCompass.update(matrixValues[0]);
	 * compassTriangle.setRotation(matrixValues[0]); } }
	 */

	@Override
	public void onSensorChanged(SensorEvent event) {
		// spencerOnSensorChanged(event);
		// emilyOnSensorChanged(event);
	}

	/**
	 * Creates the context menu that allows the user to delete tags
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		// show delete context menu only if user created the comment, or if the
		// comment is on their tag
		if (comments.get(info.position).getUsername()
				.equalsIgnoreCase(UserSession.CURRENT_USER.getuName())
				|| currentTag.getOwnerName().equalsIgnoreCase(
						UserSession.CURRENT_USER.getuName())) {
			menu.setHeaderTitle("Comment");
			menu.add(1, 1, 1, "Remove Comment");
		}
	}

	/*
	 * Implements the click listeners for selecting an item from the context
	 * menu
	 */
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item)
	// public boolean onContextItemSelected(MenuItem item)
	{
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		// delete the selected tag
		if (item.getItemId() == 1) {
			removeComment(info.position);
		}
		return true;
	}

	/*
	 * FUNCTIONS
	 */
	private void removeComment(int index) {
		removeCommentIndex = index;
		setupProgress(getString(R.string.progress_removing_comment));
		showProgress();
		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_DELETE, comments.get(index));
	}

	Integer position = null; // Is this necessary? -SK 9/2


	/**
	 * Retrieves the comment for the tag by creating a new thread
	 */
	@Override
	public void onCreateCommentViewCallback(boolean refresh) {
		if (refresh || !commentsInitialized) {
			retrieveComments();
		} else {
			updateCommentFragment();
		}
	}

	private void retrieveComments() {
		// this should be commentViewFragmentComments
		if (currentTag == null) {
			Log.e(TAG, "currentTag is NULL!");
			return;
		}

		setupProgress(getString(R.string.progress_retrieving_comments));
		showProgress();

		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_TAG_COMMENTS, currentTag);
	}

	/**
	 * Displays the tag in new UI for viewing by the user
	 */

	@Override
	public void onCreateDescriptionViewCallback() {
		setupTag();
		displayTag();
	}

	/**
	 * Displays the tags information in the appropriate fragment
	 */
	@SuppressWarnings("unchecked")
	// for unchecked cast from serializable to ArrayList<Tag>
	private void setupTag() {
		Intent i = getIntent();
		if (i != null && i.hasExtra(EXTRA_TAGID)) {
			currentTagID = i.getLongExtra(EXTRA_TAGID, 0);
			currentTag = null;
		}

		tagPhotoImgView = (ScaleImageView) findViewById(R.id.tag_photo);
		tagPhotoImgView.setCallbacks(this);
		tagPhotoImgView.setHandleTouchEvents(false);


		/*
		 * initialize Description View Fragment
		 */
		descriptionTxtView = (TextView) ((DescriptionViewFragment) descriptionViewFragment)
				.getView().findViewById(R.id.description);
		dateCreatedTxtView = (TextView) ((DescriptionViewFragment) descriptionViewFragment)
				.getView().findViewById(R.id.label_date_created);
		timeStampTxtView = (TextView) ((DescriptionViewFragment) descriptionViewFragment)
				.getView().findViewById(R.id.timestamp);
		lblLocationTxtView = (TextView) ((DescriptionViewFragment) descriptionViewFragment)
				.getView().findViewById(R.id.label_location);
		locationTxtView = (TextView) ((DescriptionViewFragment) descriptionViewFragment)
				.getView().findViewById(R.id.location);

		// Make a DB request for the Tag
		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_TAG, new Tag(currentTagID));
	}
	
	private void displayTag() {
		if (currentTag == null) {
			return;
		}
		
		// get location for MapViewHandler
		geo = currentTag.getLocation();

		// display tag name
		actionBar = getActionBar();
		actionBar.setTitle(currentTag.getName());

		// set Description in description fragment
		descriptionTxtView.setText(currentTag.getDescription());
		Log.d(TAG, "Desc is " + currentTag.getDescription());

		Date date = currentTag.getCreatedDateTime();
		SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
		SimpleDateFormat tf = new SimpleDateFormat(Constants.TIME_FORMAT);
		String fDate = df.format(date);
		String fTime = tf.format(date);

		// display date and time in description fragment
		dateCreatedTxtView.setText("Date Created:");
		timeStampTxtView.setText(fDate + " " + fTime);
		// dateCreatedTxtView.setText(fDate);
		// timeStampTxtView.setText(fTime);

		// display location string in description fragment
		lblLocationTxtView.setText("Location:");
		locationTxtView.setText(lldf.format(geo.getLatitude()) + ", "
				+ lldf.format(geo.getLongitude()));

		// display tag image
		if (currentTag.getImageUrl() != null && currentTag.getImageUrl().length() > 0) {
			String url = currentTag.getImageUrl();
			loadImage(url);
		}
	}

	/**
	 * Load the tag's image from the URL and into the ImageView
	 */
	private void loadImage(String imgUrl) {
		DbHandlerScaledImageReq gsi = new DbHandlerScaledImageReq();
		
		gsi.width = (int) (getResources().getDimension(R.dimen.image_width));
		gsi.height = (int) (getResources().getDimension(R.dimen.image_height));
		gsi.urls = new String[1];
		gsi.urls[0] = imgUrl;
		tagImageGetID = mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_SCALED_IMAGES, gsi);
	}

	/*
	 * EVENT HANDLERS
	 */
	public class DeleteConfirmationDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.dialog_deletetag_message)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent returnIntent = new Intent();
						returnIntent.putExtra(TAG_DELETED, currentTagID);
	        			setResult(RESULT_OK, returnIntent);
	        			finish();
	                   }
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}
	
	DeleteConfirmationDialogFragment confirmDelete = null;
	
	static final int ACTIVITY_EDITTAG = 1;
	
	/**
	 * Handles the event of a user clicking on an item in the options menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_edit:
			Intent intent = new Intent(this, AddTagActivity.class);
			intent.putExtra(Constants.EXTRA_TAGID, currentTag.getId());
			startActivityForResult(intent, ACTIVITY_EDITTAG);
			return true;
		case R.id.action_delete:
			FragmentManager manager = TagViewActivity.this.getFragmentManager();
			confirmDelete = new DeleteConfirmationDialogFragment();
			confirmDelete.show(manager, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean doubleClickCallback() {
		String url = currentTag.getImageUrl();
		if (url == null || url.length() == 0)
			return false;
		
		int visibility;
		visibility = lowerArea.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
		lowerArea.setVisibility(visibility);

		return true;
	}
	
	/**
	 * Handle activity results here. Specifically when the edit tag 
	 * activity returns.  Will need to update the Tag data.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVITY_EDITTAG) {
			if (resultCode == RESULT_OK) {
				// Get the updated Tag record
				mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_TAG, currentTag);
			}
		}
	}
	
}
