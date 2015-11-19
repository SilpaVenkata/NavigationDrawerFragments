package com.hci.geotagger.objects;

import java.io.Serializable;

/**
 * This class represents the Adventure object. An Adventure object is essentially a collection tags grouped together by a user, and
 * also has information regarding user accounts associated with the adventure, a date and time the adventure was created
 * the user id of the person who created the adventure, and a description and name for the adventure.
 *
 */
public class AdventureMembers implements Serializable {		
	private static final long serialVersionUID = 2L;
	private long advID;
	private long memberID;
	
	public AdventureMembers(long advID, long memberID) {
		this.advID = advID;
		this.memberID = memberID;
	}

	//All get and set methods are self-explanatory.
	
	public void setAdvId(long id) {
		advID = id;
	}
	
	public long getAdvId() {
		return advID;
	}
	
	public void setMemberId(long id) {
		memberID = id;
	}
	
	public long getMemberId() {
		return memberID;
	}
}