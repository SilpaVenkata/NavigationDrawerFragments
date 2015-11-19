package com.hci.geotagger.activities;

import java.util.ArrayList;

import com.hci.geotagger.R;
import com.hci.geotagger.activities.common.BaseFragment;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.gui.GenericEntryListAdapter;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.Tag;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * Taglist activity displays a scrollable list of tags for 
 * a given user account and gives the user the ability
 * to click on them to view the tag. 
 */

public class AdvViewTagFragment extends BaseFragment {
	private static final String TAG = "AdvViewTagFragment";
	
	private ArrayList<Tag> tags = new ArrayList<Tag>();
	
	private GenericEntryListAdapter listAdapter = null;
	
	private Adventure adventure;
	private int userID;
	private Button addTag;

	static final int ADDTAG_ACTIVITY = 1;
	static final private int VIEWTAG_ACTIVITY = 2;	

	ListView tagsList;
	
	private View fragmentView; //view that is returned for onCreateView

	public interface UpdateDatabaseListener {
	    public void onDatabaseChanged();
	}
	
	public AdvViewTagFragment() {
	}
	
	public void setTags(ArrayList<Tag> tags) {
		this.tags = tags;
		
		if (listAdapter != null) {
			listAdapter.clear();
			listAdapter.notifyDataSetChanged();
			for (int i = 0; i < tags.size(); i++)
				listAdapter.add(tags.get(i));
			listAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_adv_view_tag_tab, container, false);
		
		Intent intent = this.getActivity().getIntent();
		Bundle bundle = intent.getExtras();
		adventure = (Adventure) bundle.getSerializable("adventure");
		
		Log.d(TAG, "onCreateView: adventure: id="+adventure.getId()+",name="+adventure.getName());
		
		addTag = (Button)fragmentView.findViewById(R.id.advViewTagTab_btnNewTag);		
		addTag.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(v.getContext(), AddTagActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("adventure", adventure);
				intent.putExtras(bundle);
				intent.setFlags(1);
				startActivityForResult(intent, ADDTAG_ACTIVITY);
			}
		});
		
		tagsList = (ListView)fragmentView.findViewById(R.id.tag_list);
		listAdapter = new GenericEntryListAdapter(this.getActivity(), Tag.LIST_LAYOUT_ID, tags);

		tagsList.setAdapter(listAdapter);
		tagsList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Tag t = (Tag) parent.getItemAtPosition(position);
				Intent i = new Intent(parent.getContext(), TagViewActivity.class);
				i.putExtra(TagViewActivity.EXTRA_TAGID, t.getId());
				
				// if user is viewing their own tag list, open the tag view
				// expecting a result incase
				// the user deletes or updates the tag from the tagview
				if (userID == UserSession.CURRENTUSER_ID)
					startActivityForResult(i, VIEWTAG_ACTIVITY);
				else
					startActivity(i);
			}
		});
		
		if (!Constants.ADVENTURE_ADDTAG_CAPABLE)
			addTag.setVisibility(Button.GONE);

		return fragmentView;
	}
	
	UpdateDatabaseListener mCallback = null;
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);

	    // This makes sure that the container activity has implemented
	    // the callback interface. If not, it throws an exception
	    try {
	        mCallback = (UpdateDatabaseListener) activity;
	    } catch (ClassCastException e) {
	        throw new ClassCastException(activity.toString()
	                + " must implement UpdateDatabaseListener");
	    }
	}
	
	/**
	 * When the image is selected in the gallery, this method shows it in ImageView
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
        	switch(requestCode) {
    		//if image is selected from gallery, show it in the image view
    		case ADDTAG_ACTIVITY:
    			mCallback.onDatabaseChanged();
    			break;
    		case VIEWTAG_ACTIVITY:
    			if (data.hasExtra(TagViewActivity.TAG_DELETED) ||
    				data.hasExtra(TagViewActivity.TAG_UPDATED)) {
    				mCallback.onDatabaseChanged();
    			}
    			break;
            }
        }
    }
	
}
