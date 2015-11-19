package com.hci.geotagger.activities;

import java.util.ArrayList;

import com.hci.geotagger.R;
import com.hci.geotagger.activities.common.BaseListActivity;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.objects.GeotaggerObject;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * This Activity is designed to support the display of selectable lists of
 * GeotaggerObject based records. These GeotaggerObject based records must 
 * implement the Checkable methods.
 * 
 * @author Paul Cushman
 *
 */
public class SelectFromListActivity extends BaseListActivity {
	private String TAG = "SelectFromListActivity";
	
	public static final String EXTRA_LIST = "recordList";
	public static final String EXTRA_TITLE = "title";

	private EntryAdapter listAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_record);
		ActionBar actionBar = getActionBar();
		optionsMenuID = R.menu.select_list;
		
		Intent i = getIntent();
		ArrayList<GeotaggerObject> entries = (ArrayList<GeotaggerObject>) i.getSerializableExtra(EXTRA_LIST);
		
		int titleID;
		titleID = i.getIntExtra(EXTRA_TITLE, R.string.selectRecords_title_default);
		actionBar.setTitle(titleID);

		listAdapter = new EntryAdapter(this, entries.get(0).getCheckableListLayoutID(), entries);
		setListAdapter(listAdapter);

		// action when a list item is clicked
		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			}
		});
		
		Button saveButton = (Button)findViewById(R.id.save_button);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<GeotaggerObject> resultList = new ArrayList<GeotaggerObject>();
				Intent returnIntent = getIntent();
				if (listAdapter.itemsAreSelected()) {
					ArrayList<GeotaggerObject> adapterList = listAdapter.getItems();
					for (int i=0; i<adapterList.size(); i++) {
						GeotaggerObject cur = adapterList.get(i);
						if (cur.selected)
							resultList.add(cur);
					}
				}
				returnIntent.putExtra(EXTRA_LIST, resultList);
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_select_all:
			for (GeotaggerObject entry : listAdapter.entries) {
				entry.selected = true;
			}
			listAdapter.notifyDataSetChanged();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private class EntryAdapter extends ArrayAdapter<GeotaggerObject> {
		private ArrayList<GeotaggerObject> entries;
		Context c;
		private int entryViewId;

		public EntryAdapter(Context context, int entryViewId, ArrayList<GeotaggerObject> entries) {
			super(context, entryViewId, entries);
			this.entries = entries;
			this.c = context;
			this.entryViewId = entryViewId;
		}
		
		/**
		 * This method will identify if there are any items in the list of entries
		 * that have been checked.
		 * @return true if any items are selected, false if none are selected
		 */
		public boolean itemsAreSelected() {
			for (int i=0; i<entries.size(); i++)
				if (entries.get(i).selected)
					return true;
			return false;
		}
		
		/**
		 * This method will return the list of GeotaggerObjects\
		 * @return
		 */
		public ArrayList<GeotaggerObject> getItems() {
			return entries;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// This should NOT happen
			if (entries.size() == 0)
				return null;
			
			View row = convertView;
			if (row == null) {
				LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = vi.inflate(entryViewId, null);

				CheckBox checkBox = (CheckBox) row.findViewById(entries.get(0).getCheckableListCheckBoxID());
				checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Integer p = (Integer)buttonView.getTag();
						entries.get(p).selected = isChecked;
					}
				});
			}
			GeotaggerObject go = entries.get(position);
			if (go != null) {
				CheckBox checkBox = (CheckBox) row.findViewById(go.getCheckableListCheckBoxID());
				
				Integer p = position;
				checkBox.setTag(p);
				
				go.updateCheckableView(row);
			}// end if t
			
			
			return row;
		}// end getView

	}// end tagadapter
	
}
