package com.hci.geotagger.activities.unused;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.hci.geotagger.R;
import com.hci.geotagger.activities.AddTagActivity;
import com.hci.geotagger.activities.TagViewActivity;
import com.hci.geotagger.activities.common.BaseListActivity;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.ImageHandler;
import com.hci.geotagger.connectors.AdventureHandler;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.Tag;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Taglist activity displays a scrollable list of tags for 
 * a given user account and gives the user the ability
 * to click on them to view the tag. 
 */

public class AdvViewTagTabActivity extends BaseListActivity 
{
	private ArrayList<Tag> tags = null;
	private TagAdapter TA;
	private AdventureHandler advHandler;
	private Adventure adventure;
	private Runnable viewTags;
	private int userID;
	private int CONTEXT_DELETE_ID = 1;	
	private ImageHandler imageHandler;
	private Button addTag;

	HashMap<String, Bitmap> thumbCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adv_view_tag_tab);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		adventure = (Adventure) bundle.getSerializable("adventure");
		
		addTag = (Button)findViewById(R.id.advViewTagTab_btnNewTag);		
		addTag.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(v.getContext(), AddTagActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("adventure", adventure);
				intent.putExtras(bundle);
				intent.setFlags(1);
				startActivity(intent);				
			}
		});		

		// initialize objects
		imageHandler = new ImageHandler(this);
		thumbCache = new HashMap<String, Bitmap>();
		tags = new ArrayList<Tag>();
		this.TA = new TagAdapter(this, R.layout.row, tags);
		setListAdapter(this.TA);
		registerForContextMenu(getListView());

		advHandler = new AdventureHandler(this);				
		// action when a list item is clicked
		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// Open tag view, pass current position in taglist along with
				// whole taglist
				// this can be used to implement swype through tags without
				// downloading each time
				
				Intent i = new Intent(parent.getContext(), TagViewActivity.class);

				i.putExtra("startPos", position);
				i.putExtra("tagList", tags);
				// if user is viewing their own tag list, open the tag view
				// expecting a result incase
				// the user deletes the tag from the tagview
				if (userID == UserSession.CURRENTUSER_ID)
					startActivityForResult(i, CONTEXT_DELETE_ID);
				else
					startActivity(i);
			}
		});
		
		if (!Constants.ADVENTURE_EDITABLE)
			addTag.setVisibility(Button.INVISIBLE);

		// get the UserID that was passed to this activity to determine whose
		// tags to load
//		Intent i = getIntent();
//		int id = i.getIntExtra("id", -1);
//		if (id != -1) {
//			this.userID = id;
//			retrieveTags();
//		}
		retrieveTags();
	}

	// setup separate thread to retrieve tags
	private void retrieveTags() {
		// retrieve the tags in separate thread
		viewTags = new Runnable() {
			@Override
			public void run() {								
				// get the user's adventure tags and display
				// them in the list
				GetTags();
				// after getting tags, download the images to the cache and
				// update the ui
				loadImagesToCache();
			}
		};
		Thread thread = new Thread(null, viewTags, "GetTagThread");
		thread.start();
		setupProgress(getString(R.string.progress_retrieving_tags));
		showProgress();
	}

	/*
	 * Functions
	 */	

	// get the tags from the database, then create tag objects for them and add
	// them to the array list
	private void GetTags()  
	{
		tags = advHandler.getAllAdventureTags(adventure.getId(), false);
	}

	private void loadImagesToCache() {
		// retrieve the tags in separate thread
		Runnable loadImages = new Runnable() {
			@Override
			public void run() {
				int width = (int)(getResources().getDimension(R.dimen.thumbnail_width));
				int height = (int)(getResources().getDimension(R.dimen.thumbnail_height));

				// loop through tags and cache their images if they have them
				for (Tag t : tags) {
					if(t != null)
					{
						String url = t.getImageUrl();
						// if tag has image url, download image and cache it
						if (!url.isEmpty()) {
							final Bitmap b = imageHandler.getScaledBitmapFromUrl(url, width, height);
							if (b != null)
								thumbCache.put(url, b);
						}
					}	
				}
				// update array adapter on ui thread
				runOnUiThread(returnRes);
			}
		};
		Thread thread = new Thread(null, loadImages, "LoadImageThread");
		thread.start();

	}

	/*
	 * Bindings
	 */
	// update list with the results of getTag
	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if (tags != null && tags.size() > 0) {
				TA.notifyDataSetChanged();
				for (int i = 0; i < tags.size(); i++)
					TA.add(tags.get(i));
			}
			stopProgress();
			TA.notifyDataSetChanged();
		}
	};

	// arrayadapter to bind tags to list view
	private class TagAdapter extends ArrayAdapter<Tag> {

		private ArrayList<Tag> tags;
		Context c;

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
				TextView nameTxt = (TextView) row
						.findViewById(R.id.row_txtName);
				TextView descTxt = (TextView) row
						.findViewById(R.id.row_txtdesc);
				TextView timeTxt = (TextView) row
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
