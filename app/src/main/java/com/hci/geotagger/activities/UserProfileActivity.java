package com.hci.geotagger.activities;


import com.hci.geotagger.R;
import com.hci.geotagger.activities.common.BaseActivity;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.ImageHandler;
import com.hci.geotagger.objects.UserAccount;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class takes a user to either the EditProfile activity or view tag activity. 
 */
public class UserProfileActivity extends BaseActivity {

	private UserAccount currentUserAccount;
	private TextView uNameTxt, locTxt, descTxt, quoteTxt, emailTxt;
	private ImageView imgView;
	private Button btnTags;
	private Button btnEdit;

	/**
	 * Overidded onCreate method that retrieves the current Useraccont via UserSession class and
	 * populates ui elements with retrieved information.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);

		//init ui elements
		uNameTxt = (TextView) findViewById(R.id.profileview_username);
		locTxt = (TextView) findViewById(R.id.profileview_locationtxt);
		descTxt = (TextView) findViewById(R.id.profileview_aboutme);
		quoteTxt= (TextView) findViewById(R.id.profileview_quote);
		emailTxt = (TextView) findViewById(R.id.profileview_email);

		imgView = (ImageView) findViewById(R.id.profile_photo);
		btnTags = (Button) findViewById(R.id.profileview_tagsBtn);
		btnEdit = (Button) findViewById(R.id.profileview_editBtn);

		Intent i = getIntent();

		if(i != null && i.hasExtra("LoggedInUser"))
		{
			//get current users profile if they are viewing their own
			if(i.getBooleanExtra("LoggedInUser", false))
			{
				currentUserAccount = UserSession.CURRENT_USER;
			}
		}
		else if(i != null && i.hasExtra("account")){
			try
			{
				//if it was any other user, retrieve their account that was passed to the activity
				currentUserAccount = (UserAccount) i.getSerializableExtra("account");
			}
			catch(ClassCastException ex)
			{
				Log.d("User Profile OnCreate", "Unable to deserialize user account");
				ex.printStackTrace();
			}
		}
		//after user account has been set, start showing its data
		if(currentUserAccount != null)
		{
			String profile = currentUserAccount.getuName();
			if (profile.length() == 0) {
				profile = getResources().getString(R.string.profile_title);
			} else {
				profile = profile.substring(0, 1).toUpperCase() + profile.substring(1);
			}

			uNameTxt.setText(profile);
			//if the user is viewing their own profile, show the edit button
			if (currentUserAccount.getId() == UserSession.CURRENTUSER_ID)
			{
				btnEdit.setVisibility(Button.VISIBLE);
			}
			setUIFields();
		}

		/*
		 * Button Handlers
		 */
		// go to add tags menu when add button is clicked
		btnEdit.setOnClickListener(new OnClickListener() {
			public void onClick(View view0) {
				// open current user's profile
				Intent i = new Intent(UserProfileActivity.this, EditProfileActivity.class);
				startActivityForResult(i,1);
			}
		});
		//view the user's taglist when the tags button is clicked
		btnTags.setOnClickListener(new OnClickListener() {
			public void onClick(View view0) {
				// create link to tags
				Intent i = new Intent(getBaseContext(), TagListActivity.class);
				//pass the ID of the current user to ViewTag activity to load their tags 
				i.putExtra("id", currentUserAccount.getId());
				startActivity(i);
			}
		});


	}
/**
 * Overidded onActiviityResult that returns after user edits profile information and populates the view with the edited information.
 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1)
		{
			if (resultCode == RESULT_OK)
			{
				//reload the current user account after the profile is edited and reset fields
				currentUserAccount = UserSession.CURRENT_USER;
				setUIFields();
			}
		}
	}

	/**
	 * Displays the User's profile data on the screen
	 */
	private void setUIFields() 
	{
		String imageUrl = currentUserAccount.getImage();
		if((!imageUrl.equalsIgnoreCase("null")) && (!imageUrl.isEmpty()))
		{
			loadImage(imageUrl);
		}
		//String name, loc, desc, quote;
		String str_default = "Not Yet Set";
		//set strings to default value if they are not set (to prevent displaying 'null')
		String quote = (currentUserAccount.getQuote().equalsIgnoreCase("null")) ? str_default : currentUserAccount.getQuote();
		String loc = (currentUserAccount.getLocation().equalsIgnoreCase("null"))? str_default : currentUserAccount.getLocation();
		String desc = (currentUserAccount.getDescription().equalsIgnoreCase("null")) ? str_default : currentUserAccount.getDescription();
		String email = (currentUserAccount.getEmail().equalsIgnoreCase("null")) ? str_default : currentUserAccount.getEmail(); 

		locTxt.setText(loc);
		descTxt.setText(desc );
		quoteTxt.setText(quote);
		emailTxt.setText(email);
	}

	/**
	 *  load the user's profile image from the specified url into the image view
	 * @param imgUrl the uri of the image to load
	 */
	private void loadImage(String imgUrl) {
		final String url = imgUrl;
		// retrieve the image in separate thread
		Runnable loadImage = new Runnable() {
			@Override
			public void run() {
				ImageHandler handler = new ImageHandler(UserProfileActivity.this);
				// get a scaled version of the image so we don't load the full
				// size unnecessarily
				final Bitmap b = handler.getScaledBitmapFromUrl(url,
						R.dimen.image_width, R.dimen.image_height);
				// if the image gets returned, set it in the image view
				if (b != null) {
					runOnUiThread(new Runnable() {
						public void run() {
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