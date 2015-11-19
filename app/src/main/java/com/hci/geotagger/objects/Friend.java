package com.hci.geotagger.objects;

import java.io.Serializable;

/**
 * This class represents the Friend object.
 * TODO: DOCUMENT ME
 *
 */
public class Friend implements Serializable 
{		
	private static final long serialVersionUID = 2L;
	private long userID;
	private long friendID;
	
	public Friend(long userID, long friendID) {
		this.userID = userID;
		this.friendID = friendID;
	}

	//All get and set methods are self-explanatory.
	
	public long getUserId() {
		return userID;
	}
	
	public long getFriendId() {
		return friendID;
	}
}