package com.hci.geotagger.activities;

import java.io.File;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.hci.geotagger.objects.Comment;

/**
 * This activity is used to add or edit a comment. If a comment is to be edited then the comment ID
 * value is identified by the Constants.EXTRA_COMMENTID value. The presence of this value is used to
 * identify this as an edit, versus and add of a new comment. The tag ID that the new or current
 * comment is associated with is identified by the Constants.EXTRA_TAGID value.  The tag ID is
 * required, if it is not present then the activity will finish.
 * 
 * This activity uses the DbHandler capability.
 * 
 * @author Paul Cushman
 *
 */
public class AddCommentActivity extends BaseActivity implements DbMessageResponseInterface {
	private String TAG = "AddCommentActivity";
	
	private final int CONTEXT_DELETE_ID = 1;
	final Context c = AddCommentActivity.this;
	
	AddImageFragment imageFragment;
	
	Button btnOk;
	EditText txtComment;
	
	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication app;
	
	private long tagID;
	private Comment comment;
	
	private boolean updateComment = false;
	private int getScaledImageID;
	
	/**
	 * Intializes GPS location listener in case user uses gps coordinates for
	 * added tag, a new ImageHandler which will retrieve image from mediastore(using generic method regardless of device type), scale it
	 * to avoid using too much memory(which may cause crash on older phone) and upload it to server, and of initialize ui elements.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_comment);
		ActionBar ab = getActionBar();
		
		//buttons
		btnOk = (Button) findViewById(R.id.addcomment_btnOk);
		btnOk.setEnabled(true);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		
		if (bundle.containsKey(Constants.EXTRA_TAGID)) {
			tagID = bundle.getLong(Constants.EXTRA_TAGID);
		} else {
			Log.e(TAG, "NO TAGID IDENTIFIED");
			setResult(RESULT_CANCELED);
			finish();
		}
		
		if (bundle.containsKey(Constants.EXTRA_COMMENTID)) {
			ab.setTitle(R.string.edit_comment);
			btnOk.setText(R.string.save);
			updateComment = true;
			setupProgress(getString(R.string.progress_loading));
			showProgress();
			comment = new Comment(bundle.getLong(Constants.EXTRA_COMMENTID));
		} else {
			ab.setTitle(R.string.new_comment);
			btnOk.setText(R.string.add);
		}

		FragmentManager fm = getFragmentManager();
		imageFragment = (AddImageFragment)fm.findFragmentById(R.id.addcomment_image_fragment);
		
		//text fields
		txtComment = (EditText) findViewById(R.id.addcomment_comment);
		
		//If the orientation was changed, reload the image
		if(savedInstanceState != null)
	    {
			String savedUri = (savedInstanceState.getString("imageUri"));
			if (savedUri != null) {
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
				String url = "";

				//create a new comment from the given info
				String commentString = txtComment.getText().toString();
				if (!commentString.isEmpty()) {
					comment = new Comment(tagID, commentString, url);
					startAddingComment();
			
					//will only reach here if something goes wrong adding tag
					//if so, re enable button
					btnOk.setEnabled(true);
				} else {
					Toast t = Toast.makeText(c, getString(R.string.toast_problem_commentneedscomment), Toast.LENGTH_SHORT);
					t.show();
					btnOk.setEnabled(true);
				}
			}
		});
		
		app = (GeotaggerApplication)getApplication();
		rspHandler = new DbHandlerResponse(TAG, this, this);
		app.addResponseHandler(rspHandler);
		
		if (updateComment)
			app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_COMMENT, comment);
	}
	
	/**
	 * This method will update the picture rotate button.  If there is an image associated with 
	 * the comment then the rotate button will be made visible, otherwise it will be hidden.
	 */
	private void updateContents() {
		Bitmap bitmap;
		bitmap = comment.getBitmap();
		if (bitmap != null) {
			imageFragment.setImage(bitmap);
		} else {
			imageFragment.clearImage();
		}
		
		//text fields
		txtComment.setText(comment.getText());
	}
	
	/**
	 * This method will begin the process of adding the comment to the database. If there is an
	 * image associated with the comment then the image will be uploaded. If there is an image
	 * we will wait for the image to be uploaded before starting to add the comment, so that the
	 * image URL can be added to the comment. If there is NO image we will add the comment here.
	 */
	private void startAddingComment() {
		setupProgress(c.getResources().getString(R.string.progress_adding_comment));
		showProgress();

		comment.setImageUploadFile(imageFragment.getCurrentImage());
		if (imageFragment.getCurrentImage() != null) {
			app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_UPLOAD_IMAGE, imageFragment.getCurrentImage());
		} else {
			app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_ADD, comment);
		}
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
				comment.setImageId(imageID);
				app.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_ADD, comment);
				break;
			case DbHandlerConstants.DBMSG_ADD:
				comment = (Comment)response;
				finishAddingComment();
				break;
			case DbHandlerConstants.DBMSG_GET_COMMENT:
				comment = (Comment)response;
				String url = comment.getImageURL();
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
				break;
			case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
				if (msgID == getScaledImageID) {
					if (success) {
						DbHandlerScaledImageRsp sir = (DbHandlerScaledImageRsp)response;
						comment.setBitmap(sir.bitmap);
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
				msg = this.getResources().getString(R.string.toast_addcomment_failure);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				break;
			case DbHandlerConstants.DBMSG_GET_COMMENT:
			case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
				updateContents();
				break;
			}
		}
	}

	/**
	 * This method is called when the activity has finished adding the comment. A Toast message
	 * will be displayed to identify that the comment was added successfully and then the
	 * activity will be finished. An activity result code of RESULT_OK will be returned.
	 */
	private void finishAddingComment() {
		String msg = getString(R.string.toast_addcomment_success);
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.addimage_imgView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle("Tag Image");
			menu.add(Menu.NONE, CONTEXT_DELETE_ID, Menu.NONE, "Clear");
		}
	}
	
	/**
	 * Context handler for deleting an image on long press
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
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