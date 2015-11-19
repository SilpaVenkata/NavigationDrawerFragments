package com.hci.geotagger.objects;

import java.io.Serializable;
import java.util.ArrayList;

import com.hci.geotagger.connectors.GeotaggerHandler;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

public class GeotaggerObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	protected long id; 

	public boolean selected;
	protected int checkBoxID = 0;
	
	public GeotaggerObject() {
		selected = false;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
    public boolean isSelected() {
    	return selected;
    }
   
    public void setSelected(boolean selected) {
    	this.selected = selected;
    }
	
	/**
	 * Compares the fields that should identify this GeotaggerObject objects uniquely.
	 * @param that
	 * @return true is returned if they are equal, false otherwise
	 */
	public boolean equals(GeotaggerObject that) {
		if (this.id == that.id)
			return true;
		return false;
	}
	
	/**
	 * Override this method to return an instance of the handler to use for this type
	 * of object.
	 * @return The type of handler associated with the object type
	 */
	public GeotaggerHandler getHandler(Context context) {
		return null;
	}

	
	public int getListLayoutID() {
		return -1;
	}
	
	public View updateListView(View view) {
		return view;
	}
	
	public int getCheckableListLayoutID() {
		return -1;
	}
	
	public int getCheckableListCheckBoxID() {
		return checkBoxID;
	}

	public View updateCheckableView(View view) {
		CheckBox checkBox = (CheckBox) view.findViewById(checkBoxID);
		checkBox.setChecked(selected);
		return view;
	}
	
	public boolean inList(ArrayList<?> objList) {
		return false;
	}
}
