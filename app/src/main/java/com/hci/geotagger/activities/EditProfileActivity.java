package com.hci.geotagger.activities;

import java.io.File;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.common.BaseActivity;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.AccountHandler;
import com.hci.geotagger.connectors.ImageHandler;
import com.hci.geotagger.connectors.ReturnInfo;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageReq;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageRsp;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.objects.Comment;
import com.hci.geotagger.objects.UserAccount;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

/**
 * This class allows a user to modify their Profile information. This includes location, a small "About Me" blurb
 * their favorite quote, and an email address.
 * 
 *
 */
public class EditProfileActivity extends BaseActivity implements DbMessageResponseInterface {
	private TextView profileTxt;
	private EditText locTxt, descTxt, quoteTxt, emailTxt;
	private Button saveBtn;
	
	private AccountHandler accountHandler;
	
	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication app;
	
	AddImageFragment imageFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);
		
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.edit_profile);
		
		accountHandler = new AccountHandler(this);
		
		profileTxt = (TextView) findViewById(R.id.editprofile_username);
		locTxt = (EditText) findViewById(R.id.editprofile_location);
		descTxt = (EditText) findViewById(R.id.editprofile_desc);
		quoteTxt = (EditText) findViewById(R.id.editprofile_quote);
		emailTxt = (EditText) findViewById(R.id.editprofile_email);
		
		saveBtn = (Button) findViewById(R.id.editprofile_saveBtn);

		FragmentManager fm = getFragmentManager();
		imageFragment = (AddImageFragment)fm.findFragmentById(R.id.editprofile_image_fragment);
		
		if (savedInstanceState != null) {
			String savedUri = (savedInstanceState.getString("imageUri"));
			if (savedUri != null) {
				//Set the layout of the image view to allow the image to expand up to max size
	        	LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	        	layout.gravity = Gravity.CENTER_HORIZONTAL;
	        	layout.leftMargin = R.dimen.Scrollview_Leftmargin;
	        	layout.rightMargin = R.dimen.Scrollview_Leftmargin;
	        	
				Uri imgUri = Uri.parse(savedUri);
				if (! imageFragment.setImage(imgUri))
					Toast.makeText(this, getString(R.string.toast_problem_loadingimage), Toast.LENGTH_SHORT).show();
			} else {
				imageFragment.clearImage();
			}
	    }
		
		loadCurrentProfile();
		
		// go to add tags menu when add button is clicked
		saveBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View view0) {
				startSavingProfile();
			}
		});
		
		app = (GeotaggerApplication)getApplication();
		rspHandler = new DbHandlerResponse(TAG, this, this);
		app.addResponseHandler(rspHandler);
	}
	
	/**
	 * This method is a callback method that is called when a DbHandler response is received from
	 * the DbHandler.
	 */
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		Log.d(TAG, "Entered DBGetCallback");
		String msg;
		
		if (success) {
			switch (action) {
			case DbHandlerConstants.DBMSG_UPLOAD_IMAGE:
				Long imageID = (Long)response;
//				comment.setImageId(imageID);
//				app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_ADD, comment);
				break;
			case DbHandlerConstants.DBMSG_UPDATE:
				stopProgress();
				msg = this.getResources().getString(R.string.toast_editprofile_success);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				finish();
				break;
			case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
/*				if (msgID == getScaledImageID) {
					if (success) {
						DbHandlerScaledImageRsp sir = (DbHandlerScaledImageRsp)response;
						comment.setBitmap(sir.bitmap);
					}
				}
				updateContents();
				*/
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
			case DbHandlerConstants.DBMSG_UPDATE:
				msg = this.getResources().getString(R.string.toast_editprofile_failure);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				break;
			case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
//				updateContents();
				break;
			}
		}
	}

	
	// define context menu for when image view is long-pressed
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.addimage_imgView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle("Profile Picture");
			menu.add(1, 1, 1, "Clear");
		}
	}
	
	//Context handler for deleting image on long press
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		//if the user deletes the image, set the flag to false,
		//reset the imageview size and image to default
		case 1:
			if (imageFragment.getHasImage()) {
				imageFragment.clearImage();
			}	
			break;
		}	
		return true;	
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	    File curImage = imageFragment.getCurrentImage();
	    if (curImage != null)
	    	outState.putString("imageUri", curImage.toString());
	}

	/*
	 * Functions
	 */
	/**
	 * Loads the current profile data into editable fields.
	 */
	private void loadCurrentProfile() {
		//if the user has a profile image, load it to the image view
		String imageUrl = UserSession.CURRENT_USER.getImage();
		if ((!imageUrl.equalsIgnoreCase("null")) && (!imageUrl.isEmpty())) {
			loadImage(imageUrl);
		}
		String str_default = "";
		// set strings to default value if they are not set (to prevent
		// displaying 'null')
		String profile = (UserSession.CURRENT_USER.getuName() == null || 
				UserSession.CURRENT_USER.getuName().equalsIgnoreCase("null")) ?
						str_default : UserSession.CURRENT_USER.getuName();
		if (profile.length() > 0)
			profile = profile.substring(0, 1).toUpperCase() + profile.substring(1);
		String quote = (UserSession.CURRENT_USER.getQuote()
				.equalsIgnoreCase("null")) ? str_default
				: UserSession.CURRENT_USER.getQuote();
		String loc = (UserSession.CURRENT_USER.getLocation()
				.equalsIgnoreCase("null")) ? str_default
				: UserSession.CURRENT_USER.getLocation();
		String desc = (UserSession.CURRENT_USER.getDescription()
				.equalsIgnoreCase("null")) ? str_default
				: UserSession.CURRENT_USER.getDescription();
		String email = (UserSession.CURRENT_USER.getEmail());
	
		profileTxt.setText(profile);
		locTxt.setText(loc);
		descTxt.setText(desc);
		quoteTxt.setText(quote);
		emailTxt.setText(email);
	}
	
	/**
	 *  load the tag's image from the url into the image view
	 * @param imgUrl The uri of the image associated with the users profile
	 */
	private void loadImage(String imgUrl) {
		final String url = imgUrl;
		// retrieve the image in separate thread
		Runnable loadImage = new Runnable() {
			@Override
			public void run() {
				ImageHandler handler = new ImageHandler(EditProfileActivity.this);
				// get a scaled version of the image so we don't load the full
				// size unnecessarily
				final Bitmap b = handler.getScaledBitmapFromUrl(url,
						R.dimen.image_width, R.dimen.image_height);
				// if the image gets returned, set it in the image view
				if (b != null) {
					runOnUiThread(new Runnable() {
						public void run() {
							imageFragment.setImage(b);
						}
					});
				}
			}
		};
		Thread thread = new Thread(null, loadImage, "LoadImageThread");
		thread.start();
	}
	
	private void startSavingProfile() {
		setupProgress(getResources().getString(R.string.progress_saving_changes));
		showProgress();

		
		UserSession.CURRENT_USER.setDescription(descTxt.getText().toString());
		UserSession.CURRENT_USER.setLocation(locTxt.getText().toString());
		UserSession.CURRENT_USER.setQuote(quoteTxt.getText().toString());
		UserSession.CURRENT_USER.setEmail(emailTxt.getText().toString());

		app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_UPDATE, UserSession.CURRENT_USER);

/*		comment.setImageUploadFile(imageFragment.getCurrentImage());
		if (imageFragment.getCurrentImage() != null) {
			app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_UPLOAD_IMAGE, imageFragment.getCurrentImage());
		} else {
			app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_ADD, comment);
		}
		*/
	}

	/**
	 * Saves users edited profile information to server. This includes the image the user selected,
	 * favorite quote text, location text, and description text. This is done with AccountHandler class.
	 */
	private void saveChanges() {
		// editProfile(int uId, String imgUrl, String description, String
		// location, String quote)
		Runnable SaveChanges = new Runnable() {
			@Override
			public void run() {
				String url = UserSession.CURRENT_USER.getImage();
				//only upload a new image if the image was changed
				if (imageFragment.getCurrentImage() != null && imageFragment.hasImageChanged()) {
					url = uploadImage(imageFragment.getCurrentImage());
				} else if(imageFragment.getCurrentImage() == null) {
					url = "";
				}
				
// TODO: Need to create a dbhandler to do this
				boolean success = accountHandler.editProfile(UserSession.CURRENTUSER_ID, 
						"NAME", url, 
						descTxt.getText().toString(), locTxt.getText().toString(), 
						quoteTxt.getText().toString(), "EMAIL");
				if (success)	{
					//if edit successful, update the currentuser object
					UserAccount account = accountHandler.getUser(UserSession.CURRENT_USER.getuName());
					UserSession.CURRENT_USER = account;
				}
				runOnUiThread(new Runnable() {
					public void run() {
						stopProgress();
						Intent returnIntent = getIntent();
						setResult(RESULT_OK, returnIntent);        
						finish();
					}
				});
			}
		};
		Thread thread = new Thread(null, SaveChanges, "SaveChangesThread");
		thread.start();
		setupProgress(getString(R.string.progress_saving_changes));
		showProgress();
	}

	/**
	 * Uploads a scaled image to the server and sets the url. Take care of the url returned, and whether it points 
	 * to production server or development server
	 * @param f
	 * @return
	 */
	private String uploadImage(File f) {
		//first check the size of the image file without getting pixels
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
		
		int height = options.outHeight;
		int width = options.outWidth;
		Log.d("Image Size", "H, W = " + height + ", " + width);
		//resize image if it is very large to avoid out of memory exception
		if (height > 2048 || width > 2048)
			options.inSampleSize = 4;
		else if(height > 1024 || width > 1024)
			options.inSampleSize = 2;
		
		
		//get bitmap pixels
		options.inJustDecodeBounds = false;
		b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
		height = b.getHeight();
		width = b.getWidth();
		Log.d("New Image Size", "H, W = " + height + ", " + width);
		if (height > 0 && width > 0) {
//TODO fix this
/*			ReturnInfo response = imageHandler.uploadImageToServer(b);
			b.recycle();
			response.print("EditProfile uploadImage");
			return response.url;
			*/
		}
		return null;
	}

}
