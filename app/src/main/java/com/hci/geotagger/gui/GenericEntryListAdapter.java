package com.hci.geotagger.gui;

import java.util.ArrayList;

import com.hci.geotagger.objects.GeotaggerObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * This class implements a generic list adapter. Objects that extend the GeotaggerObject
 * class can be used by this list adapter.
 * 
 * @author Paul Cushman
 */
public class GenericEntryListAdapter extends ArrayAdapter<GeotaggerObject> {
	//TODO: modify this so that the internal data is used not this array
	private ArrayList<GeotaggerObject> entries = new ArrayList<GeotaggerObject>();
	Context c;
	private int entryViewId = 0;

	/**
	 * Constructor for this class. initializes the entries associated with the list, the
	 * context needed for the operations and a Entry View ID. The entryViewId is used
	 * to identify how the entries of the list are displayed.
	 * @param context The context used for to perform context operations
	 * @param entryViewId Resource ID used to display the entries of the list
	 * @param entries The entries associated with this list
	 */
	@SuppressWarnings("unchecked")
	public GenericEntryListAdapter(Context context, int entryViewId, ArrayList<?> entries) {
		super(context, entryViewId, (ArrayList<GeotaggerObject>)entries);
		this.entries = (ArrayList<GeotaggerObject>)entries;
		this.c = context;
		this.entryViewId = entryViewId;
	}
	
	/**
	 * This method is called to update the entries of this list.
	 * @param entryViewId Update the entry view resource ID.
	 * @param list Update the list of entries.
	 */
	@SuppressWarnings("unchecked")
	public void updateList(int entryViewId, ArrayList<?> list) {
		this.entryViewId = entryViewId;
		entries = (ArrayList<GeotaggerObject>)list;
		clear();
		addAll(entries);
		notifyDataSetChanged();
	}
	
	/**
	 * This method will update the list of entries.
	 * @param list The new list of entries to use.
	 */
	public void updateList(ArrayList<?> list) {
		updateList(entryViewId, list);
	}

	/**
	 * This method will return the list of GeotaggerObjects
	 * @return The current list of entries is returned.
	 */
	public ArrayList<GeotaggerObject> getItems() {
		return entries;
	}

	/**
	 * Overridden method to return the item ID of the input position within the
	 * list of entries.
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Overriden function that sets up the view for a specific entry position within 
	 * the list. This method will call methods associated with the GeotaggerObject
	 * class object to perform this method.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// This should NOT happen
		if (entries.size() == 0)
			return null;
		
		GeotaggerObject go = entries.get(position);

		View row = convertView;
		if (row == null) {
			if (go != null)
				entryViewId = go.getListLayoutID();
			
			LayoutInflater vi = (LayoutInflater) c
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = vi.inflate(entryViewId, null);
		}
		if (go != null) {
			go.updateListView(row);
		}

		return row;
	}

}
