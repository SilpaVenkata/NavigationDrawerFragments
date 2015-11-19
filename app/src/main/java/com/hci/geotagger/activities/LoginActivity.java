/* 
 * LoginActivity class contains the code for the login screen
 * This validates a user's login credentials and directs them
 * to the home screen, or links new users to the Registration
 * page. 
 * 
 * Chris Loeschorn
 * Spring 2013
 */
package com.hci.geotagger.activities;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.common.BaseActivity;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.NetworkUtils;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.ReturnInfo;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.objects.Login;
import com.hci.geotagger.objects.MyUserAccount;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the first activity the user comes in contact with. The user enters in a username and password to login.
 * Although it is removed for now, new users can also register.
 */
public class LoginActivity extends BaseActivity implements DbMessageResponseInterface {
	private String TAG = "LoginActivity";
	
	private Button loginBtn; //log in button
	private TextView title; //the title of the application
	private EditText unameTxt; //input for user's username
	private EditText pwTxt; //input for user's password
	private AlphaAnimation titleFadeIn; //animation for the title's fade in
	private AlphaAnimation itemsFadeIn; //animation for the widgets' fade in

	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication mApp = null;
	
	/**
	 * Initializes ui elements and listeners for user to login. Login is done asynchronously. If user has logged in
	 * before and session is still open(meaning user has not logged out and this is not new application install)
	 * ,then login is automatically done from information stored persistently in shared preferences.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		//keyboard is hidden upon starting the application
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		//find the widgets
		title = (TextView) findViewById(R.id.application_title);
		unameTxt = (EditText) findViewById(R.id.login_unameTxt);
		pwTxt = (EditText) findViewById(R.id.login_pwTxt);
		loginBtn = (Button) findViewById(R.id.login_btnLogin);
		
		//animation for the title's fade in
		titleFadeIn = new AlphaAnimation(0.0f, 1.0f); 
		titleFadeIn.setDuration(1200);
		title.startAnimation(titleFadeIn);
		
		//animation for the EditTexts' and button's fade in
		itemsFadeIn = new AlphaAnimation(0.0f, 1.0f);
		itemsFadeIn.setDuration(2400);
		unameTxt.startAnimation(itemsFadeIn);
		pwTxt.startAnimation(itemsFadeIn);
		loginBtn.startAnimation(itemsFadeIn);
		
		//get form controls
		//unameTxt = (EditText) findViewById(R.id.login_unameTxt);
		//pwTxt = (EditText) findViewById(R.id.login_pwTxt);
		//loginBtn = (Button) findViewById(R.id.login_btnLogin);
		//Set onClick action for login button
		loginBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				// Network must be up to attempt the login
				if(NetworkUtils.isNetworkUp(LoginActivity.this)) {
					String uName = unameTxt.getText().toString();
					String pw = pwTxt.getText().toString();
					if (! uName.isEmpty() && ! pw.isEmpty()) {
						Login login = new Login(uName, pw);
				
						setupProgress(getString(R.string.progress_loggingin));
						showProgress();
						mApp.sendMsgToDbHandler(rspHandler, LoginActivity.this, DbHandlerConstants.DBMSG_LOGIN_BYNAME, login);
					}
				}
			}
		});

		mApp = (GeotaggerApplication)getApplication();
		if (mApp != null) {
			rspHandler = new DbHandlerResponse(TAG, this, this);
			mApp.addResponseHandler(rspHandler);
			
			// Lets check if there was a working access token
			UserSession.retrieveTokens(this);
			
			// For login we will use the Refresh Token
			if (UserSession.refreshTokenExists()) {
				setupProgress(getString(R.string.progress_loggingin));
				showProgress();
				mApp.sendMsgToDbHandler(rspHandler, LoginActivity.this, DbHandlerConstants.DBMSG_LOGIN_REFRESHTOKEN);
			}
		}
	}
	
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		stopProgress();
		if (action == DbHandlerConstants.DBMSG_LOGIN_BYNAME) {
			if (success) {
				ReturnInfo returnInfo = (ReturnInfo)response;
				if (returnInfo.object != null && returnInfo.object instanceof MyUserAccount) {
					MyUserAccount loginAccount = (MyUserAccount)returnInfo.object;
					
					// Save the access_token and the refresh_token, for later usage like re-login
					UserSession.saveTokens(this);
					
					// Logged in so lets go to the HomeActivity
					loggedIn(loginAccount);
				} else {
					String msg2 = getString(R.string.invalid_login);
					Toast.makeText(getApplicationContext(), msg2, Toast.LENGTH_SHORT).show();
				}
			} else {
				String msg2 = getString(R.string.invalid_login);
				Toast.makeText(getApplicationContext(), msg2, Toast.LENGTH_SHORT).show();
			}
		} else if (action == DbHandlerConstants.DBMSG_LOGIN_VALIDATETOKEN) {
			if (success) {
				if (response != null && response instanceof MyUserAccount) {
					MyUserAccount loginAccount = (MyUserAccount)response;
					// Logged in so lets go to the HomeActivity
					loggedIn(loginAccount);
				}
			} else {
				if (UserSession.refreshTokenExists()) {
					setupProgress(getString(R.string.progress_loggingin));
					showProgress();
					mApp.sendMsgToDbHandler(rspHandler, LoginActivity.this, DbHandlerConstants.DBMSG_LOGIN_REFRESHTOKEN);
				}
			}
		} else if (action == DbHandlerConstants.DBMSG_LOGIN_REFRESHTOKEN) {
			if (success) {
				// Save the access_token and the refresh_token, for later usage like re-login
				UserSession.saveTokens(this);

				if (response != null && response instanceof MyUserAccount) {
					MyUserAccount loginAccount = (MyUserAccount)response;
					// Logged in so lets go to the HomeActivity
					loggedIn(loginAccount);
				}
			} else {
				String msg2 = getString(R.string.invalid_login);
				Toast.makeText(getApplicationContext(), msg2, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * This method is called when the logging in has been successful. The HomeActivity will
	 * be started. The Login activity will be finished.
	 * @param loginAccount
	 */
	private void loggedIn(MyUserAccount loginAccount) {
		UserSession.login(LoginActivity.this.getApplicationContext(), loginAccount);

		// create link to home screen
		Intent i = new Intent(getBaseContext(), HomeActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		stopProgress();
		finish();
	}

}//end LoginActivity
