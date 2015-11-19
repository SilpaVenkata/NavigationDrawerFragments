package com.hci.geotagger.activities;

import java.io.File;
import java.text.DecimalFormat;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.common.BaseActivity;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageReq;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageRsp;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.AdventureTags;
import com.hci.geotagger.objects.GeoLocation;
import com.hci.geotagger.objects.Tag;

/**
 * Add Tag activity allows the user to add new tags to the database.
 * This activity also supports Edit Tag capability.
 * This includes setting fields such as name/description and also
 * setting an image for the tag and usinggeo-location.
 * 
 * TODO: For edit should check if fields are changed, if not then keep the Save button disabled
 * TODO: Add ability to change the Tag picture
 */
public class AddTagActivity extends BaseActivity implements DbMessageResponseInterface {
	private String TAG = "AddTagActivity";
	
	private final int CONTEXT_DELETE_ID = 1;
	final Context c = AddTagActivity.this;
	private Adventure adventure;
	
	Button btnOk;
	EditText txtName, txtDesc, txtLoc;
	TextView txtOrigLoc;
	private CheckBox chkGPS;
	private CheckBox chkOrigLocation;
	private TextView locationGPS;
	private TextView labelGPS;
	String gpsUnknown;
	String gpsNoSignal;
	
	AddImageFragment imageFragment;
	
	private DecimalFormat lldf = new DecimalFormat("#.000000");

	private LocationListener listener;
	private boolean gpsEnabled = false;
	private boolean networkEnabled = false;
	private LocationManager lm;
	private Location location;
	
	private int intentFlags = 0;
	
	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication app;
	
	private Tag tag;
	private Long savedTagID = 0L;
	
	private boolean updateTag = false;
	private int getScaledImageID;
	private int getTagAfterAdd = 0;;
	private int getTagInitial = 0;
	
	/**
	 * Intializes GPS location listener in case user uses gps coordinates for
	 * added tag, a new ImageHandler which will retrieve image from mediastore(using generic method regardless of device type), scale it
	 * to avoid using too much memory(which may cause crash on older phone) and upload it to server, and of initialize ui elements.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_tag);
		ActionBar ab = getActionBar();
		
		//buttons
		btnOk = (Button) findViewById(R.id.addtag_btnOk);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		
		intentFlags = intent.getFlags();
		if (bundle.containsKey(Constants.EXTRA_ADVENTURE)) {
			ab.setTitle(R.string.new_tag);
			adventure = (Adventure) bundle.getSerializable(Constants.EXTRA_ADVENTURE);
			updateTag = false;
		} else if (bundle.containsKey(Constants.EXTRA_TAGID)) {
			ab.setTitle(R.string.edit_tag);
			btnOk.setText(R.string.save);
			setupProgress(getString(R.string.progress_loading));
			showProgress();
			savedTagID = bundle.getLong(Constants.EXTRA_TAGID);
			tag = new Tag(savedTagID);
			updateTag = true;
		}
		
		//text fields
		txtName = (EditText) findViewById(R.id.addtag_name);
		txtDesc = (EditText) findViewById(R.id.addtag_desc);
		txtLoc = (EditText) findViewById(R.id.addtag_location);
		txtOrigLoc = (TextView) findViewById(R.id.origlocationvalue);
		
		txtName.addTextChangedListener(setButtonStateWatcher); 
		txtDesc.addTextChangedListener(setButtonStateWatcher); 
		
		// Original Location checkbox (if edit)
		chkOrigLocation = (CheckBox) findViewById(R.id.origlocationcheckbox);
		
		//Check box
		chkGPS = (CheckBox) findViewById(R.id.addtag_useGPS);
		// Set disabled until a valid location is received
		chkGPS.setEnabled(false);
		locationGPS = (TextView) findViewById(R.id.location);
		gpsUnknown = getString(R.string.gps_location_unknown);
		gpsNoSignal = getString(R.string.gps_no_signal);
		locationGPS.setText(gpsUnknown);
		
		labelGPS = (TextView) findViewById(R.id.addtag_lblloc);
		
		labelGPS.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		});

		FragmentManager fm = getFragmentManager();
		imageFragment = (AddImageFragment)fm.findFragmentById(R.id.addcomment_image_fragment);
		
		//initialize location components
        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        
        listener = new LocationListener() //initialization of LocationListener
        {
			@Override
			public void onLocationChanged(Location locat) //update location when it changes
			{
				location = locat;
				Log.d(TAG, "onLocationChanged called");
				String locationText = lldf.format(locat.getLatitude()) + ", " + lldf.format(locat.getLongitude());
				locationGPS.setText(locationText);
				
				chkGPS.setEnabled(true);
			}

			@Override
			public void onProviderDisabled(String provider) 
			{
				// TODO Auto-generated method stub	
				Log.d(TAG, "onProviderDisabled called");
				if (location == null) {
					locationGPS.setText(gpsNoSignal);
					chkGPS.setEnabled(false);
				}
			}

			@Override
			public void onProviderEnabled(String provider) 
			{
				// TODO Auto-generated method stub	
				Log.d(TAG, "onProviderEnabled called");
				if (location == null)
					locationGPS.setText(gpsUnknown);
				else
					chkGPS.setEnabled(true);
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) 
			{
				// TODO Auto-generated method stub	
				Log.d(TAG, "onStatusChanged called");
			} 
        };
        
    	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,500.0f, listener);
		
		//If the orientation was changed, reload the image
		if(savedInstanceState != null)
	    {
			String savedUri = (savedInstanceState.getString("imageUri"));
			if(savedUri != null) {
				Uri imgUri = Uri.parse(savedUri);
				if (! imageFragment.setImage(imgUri))
					Toast.makeText(c, getString(R.string.toast_problem_loadingimage), Toast.LENGTH_SHORT).show();

			} else {
				imageFragment.clearImage();
			}
				
	    }
		
		// Add button action
		btnOk.setOnClickListener(new OnClickListener() {
			public void onClick(View view0) {				
				String name = txtName.getText().toString();
				
				// Make sure the required fields are set
				if (name.isEmpty()) {
					Toast t = Toast.makeText(c, getString(R.string.toast_problem_tagneedsname), Toast.LENGTH_SHORT);
					t.show();
					btnOk.setEnabled(true);
				}

				// Create a basic Tag object, which will be updated/filled
				if (! updateTag) {
					tag = new Tag(savedTagID);
				} else {
					tag.setId(savedTagID);
				}
				
				tag.setName(name);
				tag.setDescription(txtDesc.getText().toString());
				tag.setLocationString(txtLoc.getText().toString());
				
				GeoLocation geo = new GeoLocation(0, 0);
				
				//if the user wants to use GPS coordinates, get the current location to store in tag
				if (chkGPS.isChecked()) {
					if (location == null) {
						Toast.makeText(c, getString(R.string.toast_acquiringsignal), Toast.LENGTH_SHORT).show();
						//TODO: This causes a crash!!!
						onClick(view0); //recursive call if location cannot be found
					} else {
						geo.setLatitude(location.getLatitude());
						geo.setLongitude(location.getLongitude());
					}
				} else if (updateTag && chkOrigLocation.isChecked()) {
					geo = tag.getLocation();
				}
				
				tag.setLocation(geo);
				
				//attempt to add tag to db
				startAddingTag();
				
				if (intentFlags == 2) {
					adventure.addStoreTagList(tag);
				}
			}
		});
		
		app = (GeotaggerApplication)getApplication();
		rspHandler = new DbHandlerResponse(TAG, this, this);
		app.addResponseHandler(rspHandler);
		
		LinearLayout origLayout = (LinearLayout)findViewById(R.id.origlocationlayout);
		LinearLayout origBorder = (LinearLayout)findViewById(R.id.origlocationborder);
		if (updateTag) {
			btnOk.setEnabled(true);

			getTagInitial = app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_TAG, tag);
			
			origLayout.setVisibility(View.VISIBLE);
			origBorder.setVisibility(View.VISIBLE);
			
			chkOrigLocation.setChecked(true);
			chkOrigLocation.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean state) {
					// Cannot have both checked
					if (chkGPS.isChecked())
						chkGPS.setChecked(false);
				}
			} );
			
			chkGPS.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean state) {
					// Cannot have both checked
					if (chkOrigLocation.isChecked())
						chkOrigLocation.setChecked(false);
				}
			} );
		} else {
			btnOk.setEnabled(false);
			origLayout.setVisibility(View.GONE);
			origBorder.setVisibility(View.GONE);
		}
	}
	
	private TextWatcher setButtonStateWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			if (txtName.getText().length() > 0 && txtDesc.getText().length() > 0) {
				btnOk.setEnabled(true);
			} else {
				btnOk.setEnabled(false);
			}
		}
		public void beforeTextChanged(CharSequence s, int start, int count, int after){}
		public void onTextChanged(CharSequence s, int start, int before, int count){}
	}; 
	
	private void updateContents() {
		Bitmap bitmap;
		bitmap = tag.getBitmap();
		if (bitmap != null) {
			imageFragment.setImage(bitmap);
		} else {
			imageFragment.clearImage();
		}
		
		//text fields
		txtName.setText(tag.getName());
		txtDesc.setText(tag.getDescription());
		txtLoc.setText(tag.getLocationString());

		GeoLocation geo = tag.getLocation();
		if (geo.getLatitude() == 0.0 && geo.getLongitude() == 0.0) {
			if (updateTag) {
				chkOrigLocation.setChecked(false);
				chkOrigLocation.setEnabled(false);
			}

			txtOrigLoc.setText(this.getString(R.string.add_tag_no_orig_location));
		} else {
			if (updateTag) {
				chkOrigLocation.setChecked(true);
				chkOrigLocation.setEnabled(true);
			}
			
			String locationText = lldf.format(geo.getLatitude()) + ", " + lldf.format(geo.getLongitude());
			txtOrigLoc.setText(locationText);
		}
		chkGPS.setEnabled(false);
	}
	
	/**
	 * This method will call the appropriate database action,
	 * Add to create a new Tag and Update to edit an existing
	 * tag
	 */
	private void startAddingTag() {
		setupProgress(c.getResources().getString(R.string.progress_add_tag));
		showProgress();

		// Set the image associated with the tag, if there is one
		if (this.updateTag) {
			// TODO: add update to the Image, if possible
			app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_UPDATE, tag);
		} else {
			tag.setImageUploadFile(imageFragment.getCurrentImage());
			if (imageFragment.getCurrentImage() != null) {
				app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_UPLOAD_IMAGE, imageFragment.getCurrentImage());
			} else {
				app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_ADD, tag);
			}
		}
	}
	

	
	
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		Log.d(TAG, "Entered DBGetCallback");
		String msg;
		
		if (success) {
			switch (action) {
			case DbHandlerConstants.DBMSG_UPDATE:
				finishAddingTag();
				break;
			case DbHandlerConstants.DBMSG_UPLOAD_IMAGE:
				Long imageID = (Long)response;
				tag.setImageId(imageID);
				app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_ADD, tag);
				break;
			case DbHandlerConstants.DBMSG_ADD:
				if (response instanceof Tag) {
					tag = (Tag)response;
					// If adding the Tag to and adventure then create the relationship
					if (intentFlags == 1 && adventure != null) {
						AdventureTags advTag = new AdventureTags(adventure.getId(), tag.getId());
						app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_ADD, advTag);
					} else {
						getTagAfterAdd = app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_TAG, tag);
					}
				} else if (response instanceof AdventureTags) {
					getTagAfterAdd = app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_TAG, tag);
				}
				break;
			case DbHandlerConstants.DBMSG_GET_TAG:
				if (msgID == getTagAfterAdd) {
					finishAddingTag();
				} else {
					tag = (Tag)response;
					String url = tag.getImageUrl();
					if (url != null && url.length() > 0) {
						DbHandlerScaledImageReq gsi = new DbHandlerScaledImageReq();
						gsi.width = (int) (getResources().getDimension(R.dimen.tag_image_max_size));
						gsi.height = (int) (getResources().getDimension(R.dimen.tag_image_max_size));
						gsi.urls = new String[1];
						gsi.urls[0] = url;
	
						getScaledImageID = app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_SCALED_IMAGES, gsi);
					} else {
						updateContents();
						stopProgress();
					}
				}
				break;
			case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
				if (msgID == getScaledImageID) {
					if (success) {
						DbHandlerScaledImageRsp sir = (DbHandlerScaledImageRsp)response;
						tag.setBitmap(sir.bitmap);
					}
				}
				updateContents();
				stopProgress();
				break;
			}
		} else {
			stopProgress();
			
			switch (action) {
			case DbHandlerConstants.DBMSG_UPLOAD_IMAGE:
				msg = this.getResources().getString(R.string.toast_uploadimage_failure);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				break;
			case DbHandlerConstants.DBMSG_ADD:
				if (response instanceof AdventureTags) {
					msg = this.getResources().getString(R.string.toast_addadventuretag_failure);
				} else {
					msg = this.getResources().getString(R.string.toast_addtag_failure);
				}
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				break;
			case DbHandlerConstants.DBMSG_GET_TAG:
			case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
				updateContents();
				break;
			}
		}
	}

	private void finishAddingTag() {
		String msg;
		if (updateTag)
			msg = getString(R.string.toast_edittag_success);
		else
			msg = getString(R.string.toast_addtag_success);
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		stopProgress();
		setResult(RESULT_OK);
		finish();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    
	    File curImage = imageFragment.getCurrentImage();
	    if (curImage != null)
	    	outState.putString("imageUri", curImage.toString());
	}
	
	/**
	 * Defines the context menu for when an image view is long pressed
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		if (v.getId() == R.id.addtag_imgView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle("Tag Image");
			menu.add(Menu.NONE, CONTEXT_DELETE_ID, Menu.NONE, "Clear");
		}
	}
	
	/**
	 * Context handler for deleting an image on long press
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			//if the user deletes the image, set the flag to false,
			//reset the imageview size and image to default
			case CONTEXT_DELETE_ID:
				if (imageFragment.getHasImage()) {
					imageFragment.clearImage();
				}	
				break;
		}	
		return true;	
	}
	
}