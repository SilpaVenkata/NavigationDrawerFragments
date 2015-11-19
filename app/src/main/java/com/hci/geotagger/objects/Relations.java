package com.hci.geotagger.objects;

import java.io.Serializable;

public class Relations implements Serializable {		
	private static final long serialVersionUID = 2L;
	private long parentID;
	private long childID;
	
	public Relations(long parentID, long childID) {
		this.parentID = parentID;
		this.childID = childID;
	}

	//All get and set methods are self-explanatory.
	
	public void setParentID(long id) {
		parentID = id;
	}
	
	public long getParentID() {
		return parentID;
	}
	
	public void setChildID(long id) {
		childID = id;
	}
	
	public long getChildID() {
		return childID;
	}
}