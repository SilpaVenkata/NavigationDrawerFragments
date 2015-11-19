package com.hci.geotagger.activities;

import java.util.ArrayList;

import com.hci.geotagger.R;
import com.hci.geotagger.activities.common.BaseFragment;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.gui.GenericEntryListAdapter;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.GeotaggerObject;
import com.hci.geotagger.objects.Group;
import com.hci.geotagger.objects.Tag;
import com.hci.geotagger.objects.UserAccount;

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

public class GroupViewTagFragment extends BaseFragment {
	private static final String TAG = "GroupViewTagFragment";
	
	private ArrayList<GeotaggerObject> geoObjects = new ArrayList<GeotaggerObject>();
	
	private GenericEntryListAdapter listAdapter = null;
	
	private Group group;
	private int userID;
	private int CONTEXT_DELETE_ID = 1;	
	private Button addTag;

	static final int ADDTAG_ACTIVITY = 1;

	ListView objectList;
	
	private View fragmentView; //view that is returned for onCreateView

	public interface UpdateDatabaseListener {
	    public void onDatabaseChanged();
	}
	
	public GroupViewTagFragment() {
	}
	
	public void setList(Object objects) {
		ArrayList<GeotaggerObject> list;
		
		list = (ArrayList<GeotaggerObject>)objects;
		this.geoObjects = list;
		
		if (listAdapter != null) {
			listAdapter.clear();
			listAdapter.notifyDataSetChanged();
			for (int i = 0; i < list.size(); i++)
				listAdapter.add(list.get(i));
			listAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_adv_view_tag_tab, container, false);
        
		Intent intent = this.getActivity().getIntent();
		Bundle bundle = intent.getExtras();
		group = (Group) bundle.getSerializable("group");
		
		Log.d(TAG, "onCreateView: group: id="+group.getId()+",name="+group.getName());
		
        // Hide the add button for now
		addTag = (Button)fragmentView.findViewById(R.id.advViewTagTab_btnNewTag);
		addTag.setVisibility(View.GONE);
		/*
		addTag.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(v.getContext(), AddTagActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("group", group);
				intent.putExtras(bundle);
				intent.setFlags(1);
				startActivityForResult(intent, ADDTAG_ACTIVITY);
			}
		});
		*/
		
		objectList = (ListView)fragmentView.findViewById(R.id.tag_list);
		
		listAdapter = new GenericEntryListAdapter(this.getActivity(), Tag.LIST_LAYOUT_ID, geoObjects);

		objectList.setAdapter(listAdapter);
		objectList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent i;
				
				GeotaggerObject obj = (GeotaggerObject)parent.getItemAtPosition(position);

				if (obj instanceof Tag) {
					Tag tag = (Tag)obj;
					i = new Intent(parent.getContext(), TagViewActivity.class);
					i.putExtra(TagViewActivity.EXTRA_TAGID, tag.getId());
				} else if (obj instanceof Adventure) {
					Adventure a = (Adventure)obj;

					// Open adventure view, pass current position in adventurelist along with
					// whole adventurelist
					// this can be used to implement swipe through adventures without
					// downloading each time
					i = new Intent(parent.getContext(), AdventureViewActivity.class);
					i.putExtra("startPos", position);
					i.putExtra("adventureList", geoObjects);
					Bundle bundle = new Bundle();
					bundle.putSerializable("adventure", a);
					i.putExtras(bundle);
				} else if (obj instanceof UserAccount) {
					return;
				} else {
					return;
				}
				
				// if user is viewing their own tag list, open the tag view
				// expecting a result incase
				// the user deletes the tag from the tagview
				if (userID == UserSession.CURRENTUSER_ID)
					startActivityForResult(i, CONTEXT_DELETE_ID);
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
        	switch(requestCode) {
    		//if image is selected from gallery, show it in the image view
    		case ADDTAG_ACTIVITY:
    			mCallback.onDatabaseChanged();
    			break;
            }
        } else {
        }
    }
	
}
