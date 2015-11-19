package com.hci.geotagger.activities.unused;

import com.hci.geotagger.R;
import com.hci.geotagger.activities.LoginActivity;
import com.hci.geotagger.activities.common.BaseActivity;
import com.hci.geotagger.connectors.AccountHandler;
import com.hci.geotagger.connectors.ReturnInfo;
import com.hci.geotagger.gui.AlertHandler;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This class handles new user registration, using business rules 
 * concerning password and username format regarding legnth and characters used. 
 */
public class RegisterActivity extends BaseActivity {
	//initialize controls
	Button regButton, cancelButton;
	EditText userTxt;
	EditText pwTxt;
	EditText confirmTxt;
	
	/**
	 * Overidded onCreate method that sets up editable textfields for username and password,
	 * as well as listeners for registering or canceling registration.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		setTitle(getString(R.string.title_activity_register));
		final Context c = RegisterActivity.this;
		
		//get form controls
		userTxt = (EditText) findViewById(R.id.reg_unameTxt);
		pwTxt = (EditText) findViewById(R.id.reg_pwTxt);
		confirmTxt = (EditText) findViewById(R.id.reg_confirmTxt);
		regButton = (Button) findViewById(R.id.reg_submitBtn);
		cancelButton = (Button) findViewById(R.id.reg_btnCancel);
		//Set onClick action for login button
		
		//If the cancel button is clicked, return to login screen
		cancelButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				// return to login screen after registration
				Intent i = new Intent(c, LoginActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();			
			}
		});
		//if the register button is clicked, check the fields and attempt to register.
		regButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				//attempt to log in user
				String uName = userTxt.getText().toString();
				String pw = pwTxt.getText().toString();
				String pw2 = confirmTxt.getText().toString();
				AlertHandler alert = new AlertHandler();
				if (!uName.isEmpty() && !pw.isEmpty() && !pw2.isEmpty())
				{
					//make sure passwords match
					if (pw.equals(pw2))
					{
						if (UsernameFollowsRules(uName))
						{
							if(PasswordFollowsRules(pw))
							{
								//attempt to register in an async task
								new RegisterTask(c).execute(uName, pw); 
							}
							else
							{
								//alert.showAlert(c, null, "Password must be 6-20 characters in length.");
								String msg = "Password must be 6-20 characters in length.";
								Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
							}
						}
						else
						{
							/*alert.showAlert(c, null, "Username must start with a letter," +
									" contain only letters and numbers, and have a length of" +
									" 4-15 characters.");*/
							String msg = "Username must start with a letter," +
									" contain only letters and numbers, and have a length of" +
									" 4-15 characters.";
							Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
						}
					}
					else
					{
						//alert.showAlert(c, null, "The two passwords do not match!");
						String msg = "The two passwords do not match!";
						Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
					}	
				}
				else
				{
					String msg = "Please fill out all fields to register.";
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
					//alert.showAlert(c, null, "Please fill out all fields to register.");
					Log.d("LoginOnClick","Username or password fields cant be empty.");
				}
			}
		});
	}//end oncreate
	
	//enforce username rules
	private boolean UsernameFollowsRules(String username)
	{
		//Username can only contain letters and numbers
		//must start with letter
		//Must be 4-15 chars long
		return username.matches("^[A-Za-z][A-Za-z0-9]{3,14}");
	}
	//enforce password rules
	private boolean PasswordFollowsRules(String password)
	{
		//Password must be 6-20 characters
		return (password.length() >= 6 && password.length() <= 20);
	}

	/*
	 * Register task: Asynchronous task to make the web request and get the 
	 * attempt to register the user account
	 */
	class RegisterTask extends AsyncTask<String, Void, ReturnInfo> 
	{
		Context c;
		//get context from the parent activity for opening dialogs
		public RegisterTask(Context context)
		{
			this.c = context;		
		}
		
		 //Setup progress dialog before execution
		@Override
		public void onPreExecute() 
		{
			setupProgress(getString(R.string.progress_registration));
			showProgress();
		}
		
		/*
		 * After login task is finished, get response and
		 * determine if the login was successful. If so, close dialog 
		 * and move to next activity, if not show error. 
		 */
		@Override
		protected void onPostExecute(ReturnInfo response) {
			stopProgress();
			if(response != null)
			{
				//if the registration was success, 
				//set the loggedin flag to true, and open the Home page
				if (response.success)
				{
					Log.d("RegisterPostExecute", "RegistrationSuccess");
					//AlertHandler alert = new AlertHandler();
					//alert.showAlert(c, null, "Registration success!");
					String msg = "Registration success! Please log in to start.";
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
					// return to login screen after registration
					Intent i = new Intent(c, LoginActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
					finish();				
				}
				else
				{	
					response.print("RegisterPostExecute");
					AlertHandler alert = new AlertHandler();
					alert.showAlert(c, null, response.getMessage());
				}	
				
			}		
		}//end onPostExecute
		
		/*
		 * Create an account handler and attempt to log in with 
		 * the provided credentials. 
		 */
		@Override
		protected ReturnInfo doInBackground(String... loginParams) {
			String uName = loginParams[0];
			String pw = loginParams[1];

			// attempt login
			AccountHandler handler = new AccountHandler(c);
			ReturnInfo response;

			response = handler.registerUser(uName, pw);
			response.print("RegisterTask");
			return response;
		}// end doInBackground
	}//end RegisterTask
}
