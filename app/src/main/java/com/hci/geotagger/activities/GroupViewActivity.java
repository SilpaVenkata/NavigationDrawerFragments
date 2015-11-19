package com.hci.geotagger.activities;

import java.util.ArrayList;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.GroupViewTagFragment.UpdateDatabaseListener;
import com.hci.geotagger.activities.common.BaseActivity;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageReq;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageRsp;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.Group;
import com.hci.geotagger.objects.Tag;
import com.hci.geotagger.objects.UserAccount;

import android.os.Bundle;
import android.app.ActionBar;
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
 * Groups.
 */
public class GroupViewActivity extends BaseActivity 
implements UpdateDatabaseListener, DbMessageResponseInterface {
	private String TAG = "GroupViewActivity";
	
	private TextView descriptionTextView;
	private Group group;
	private ActionBar actionBar;
	
	private static final int EDIT_ACTIVITY = 1;
	
	private GeotaggerApplication mApp = null;
	private static DbHandlerResponse rspHandler;
	
	ArrayList<Tag> tags = new ArrayList<Tag>();
	ArrayList<Adventure> adventures = new ArrayList<Adventure>();
	ArrayList<UserAccount> members = new ArrayList<UserAccount>();
	
	private int actionOutstanding = 0;
	private final static int tagsDbRequest = 0x01;
	private final static int membersDbRequest = 0x02;
	private final static int adventuresDbRequest = 0x04;
	private final static int imagesDbRequest = 0x08;
	
	/**
	 * Populates view with Group's list of tags
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_view);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		group = (Group) bundle.getSerializable("group");
		
		// TODO: If there are actions specific to owned groups, then add them
		if (group.getOwnerId() == UserSession.CURRENTUSER_ID)
			optionsMenuID = R.menu.group_view;
		else
			optionsMenuID = R.menu.basic_options;

		Log.d(TAG, "onCreate: group: id="+group.getId()+",name="+group.getName());

		mApp = (GeotaggerApplication)getApplication();
		if (mApp != null) {
			rspHandler = new DbHandlerResponse(TAG, this, this);
			mApp.addResponseHandler(rspHandler);
		}

		initializeUIComponents();

		actionBar = getActionBar();
		descriptionTextView = (TextView) findViewById(R.id.groupView_desc);
		
		updateGuiData();
	}
	
	private void updateGuiData() {
		actionBar.setTitle(group.getName());
		descriptionTextView.setText(group.getDescription());		
	}
	
	private void updateTags(ArrayList<Tag> tags) {
		tagsViewFragment.setList(tags);
	}
	private void updateMembers(ArrayList<UserAccount> members) {
		membersViewFragment.setList(members);
	}
	private void updateAdventures(ArrayList<Adventure> adventures) {
		adventuresViewFragment.setList(adventures);
	}
	
	@Override
	public void onDatabaseChanged() {
		setupProgress(getString(R.string.progress_loading));
		showProgress();
		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_GROUP_TAGS, group, DbHandlerConstants.FLAG_CACHE);
		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_GROUP_MEMBERS, group, DbHandlerConstants.FLAG_CACHE);
		mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_GROUP_ADVENTURES, group, DbHandlerConstants.FLAG_CACHE);
		actionOutstanding |= tagsDbRequest | adventuresDbRequest | membersDbRequest;
	}

	private int tagImageGetID = 0;
	
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		switch (action) {
		case DbHandlerConstants.DBMSG_GET_GROUP_TAGS:
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
						actionOutstanding |= imagesDbRequest;
						if (done)
							actionOutstanding &= ~tagsDbRequest;
						return;
					}
				}
				updateTags(tags);
			}
			if (done)
				actionOutstanding &= ~tagsDbRequest;
			break;
		case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
			if (msgID == tagImageGetID) {
				if (success) {
					DbHandlerScaledImageRsp sir = (DbHandlerScaledImageRsp)response;
					setTagThumbnail(sir.url, sir.bitmap);
				}
				if (done) {
					updateTags(tags);
					actionOutstanding &= ~imagesDbRequest;
				}
			}
			break;
		case DbHandlerConstants.DBMSG_GET_GROUP_ADVENTURES:
			if (success) {
				adventures = (ArrayList<Adventure>)response;
				updateAdventures(adventures);
			}
			actionOutstanding &= ~adventuresDbRequest;
			break;
		case DbHandlerConstants.DBMSG_GET_GROUP_MEMBERS:
			if (success) {
				members = (ArrayList<UserAccount>)response;
				updateMembers(members);
			}
			actionOutstanding &= ~membersDbRequest;
			break;
		}
		
		if (actionOutstanding == 0) {
			stopProgress();
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
	private FrameLayout adventuresViewContent;
	private FrameLayout membersViewContent;
	
	private GroupViewTagFragment tagsViewFragment = null;
	private GroupViewTagFragment adventuresViewFragment = null;
	private GroupViewTagFragment membersViewFragment = null;

	private FragmentManager fm;
	private FragmentTransaction transaction;

	private LinearLayout tagsTabBtn;			// press to view list of tags
	private LinearLayout membersTabBtn;			// press to view list of members
	private LinearLayout adventuresTabBtn;		// press to view list of adventures


	private void initializeUIComponents() {
		
		tagsTabBtn = (LinearLayout) findViewById(R.id.tags_button);
		adventuresTabBtn = (LinearLayout) findViewById(R.id.adventures_button);
		membersTabBtn = (LinearLayout) findViewById(R.id.members_button);

		tagsViewContent = (FrameLayout) findViewById(R.id.tagsViewContent);
		adventuresViewContent = (FrameLayout) findViewById(R.id.adventuresViewContent);
		membersViewContent = (FrameLayout) findViewById(R.id.membersViewContent);
		
		// Start with the Tags List view visible and the Map gone
		tagsViewContent.setVisibility(View.VISIBLE);
		adventuresViewContent.setVisibility(View.GONE);
		membersViewContent.setVisibility(View.GONE);

		setupProgress(getString(R.string.progress_loading));
		showProgress();
		mApp.sendMsgToDbHandler(rspHandler, GroupViewActivity.this, DbHandlerConstants.DBMSG_GET_GROUP_TAGS, group);
		mApp.sendMsgToDbHandler(rspHandler, GroupViewActivity.this, DbHandlerConstants.DBMSG_GET_GROUP_ADVENTURES, group);
		mApp.sendMsgToDbHandler(rspHandler, GroupViewActivity.this, DbHandlerConstants.DBMSG_GET_GROUP_MEMBERS, group);
		actionOutstanding |= tagsDbRequest | adventuresDbRequest | membersDbRequest;

		tagsViewFragment = new GroupViewTagFragment();
		membersViewFragment = new GroupViewTagFragment();
		adventuresViewFragment = new GroupViewTagFragment();

		fm = getFragmentManager();

		// show description tab by default
		transaction = fm.beginTransaction();
		transaction.replace(R.id.tagsViewContent, tagsViewFragment);
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
					adventuresViewContent.setVisibility(View.GONE);
					membersViewContent.setVisibility(View.GONE);
					tagsViewContent.setVisibility(View.VISIBLE);
				}

				// disable the description button, re-enable the others
				tagsTabBtn.setEnabled(false);
				membersTabBtn.setEnabled(true);
				adventuresTabBtn.setEnabled(true);
			}
		});

		adventuresTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!adventuresViewFragment.isAdded()) {

					// show the description fragment
					transaction = fm.beginTransaction();
					transaction.replace(R.id.adventuresViewContent, adventuresViewFragment);

					transaction.commit();
				} else {
					transaction.show(adventuresViewFragment);
				}

				if (adventuresViewContent.getVisibility() != View.VISIBLE) {
					adventuresViewContent.setVisibility(View.VISIBLE);
					membersViewContent.setVisibility(View.GONE);
					tagsViewContent.setVisibility(View.GONE);
				}

				// disable the description button, re-enable the others
				tagsTabBtn.setEnabled(true);
				membersTabBtn.setEnabled(true);
				adventuresTabBtn.setEnabled(false);
			}
		});

		membersTabBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!membersViewFragment.isAdded()) {

					// show the description fragment
					transaction = fm.beginTransaction();
					transaction.replace(R.id.membersViewContent, membersViewFragment);

					transaction.commit();
				} else {
					transaction.show(membersViewFragment);
				}

				if (membersViewContent.getVisibility() != View.VISIBLE) {
					adventuresViewContent.setVisibility(View.GONE);
					membersViewContent.setVisibility(View.VISIBLE);
					tagsViewContent.setVisibility(View.GONE);
				}

				// disable the description button, re-enable the others
				tagsTabBtn.setEnabled(true);
				membersTabBtn.setEnabled(false);
				adventuresTabBtn.setEnabled(true);
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
			intent = new Intent(getBaseContext(), EditGroupActivity.class);
			Bundle bundle1 = new Bundle();
			bundle1.putSerializable("group", group);
			intent.putExtras(bundle1);
			
			startActivityForResult(intent, EDIT_ACTIVITY);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
        	switch(requestCode) {
        	case EDIT_ACTIVITY:
        		Bundle bundle = data.getExtras();		
        		group = (Group) bundle.getSerializable("group");
        		updateGuiData();
        		
				ArrayList<Tag> tags = group.getTags();
				updateTags(tags);
				ArrayList<UserAccount> members = group.getUsers();
				updateMembers(members);
				ArrayList<Adventure> adventures = group.getAdventures();
				updateAdventures(adventures);
        		break;
            }
        } else {
        	
        }
    }

	
}
