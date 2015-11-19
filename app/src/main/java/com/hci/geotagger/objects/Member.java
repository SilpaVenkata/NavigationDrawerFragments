/***
 * @author Syed M Shah
 */
package com.hci.geotagger.objects;

import java.io.Serializable;
import java.util.Date;

public class Member implements Serializable {

	private long id;
	
	private int mType, ownerId;
	private UserAccount owner;
	private Group group;
	private Date createdDateTime;
	
	//constructor for if a User Account is available to associate with the tag
	//this is also used when a tag is first created locally and the id is not available
	public Member(long gID, Group group,UserAccount own, int mType, Date jd)
	{
       
		this.setOwner(own);
		this.setGroup(group);
		this.createdDateTime = jd;
		this.setId(group.getId());
		this.setmType(mType);
		
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public UserAccount getOwner() {
		return owner;
	}

	public void setOwner(UserAccount owner) {
		this.owner = owner;
	}


	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}
	public int getmType() {
		return mType;
	}

	public void setmType(int mType) {
		this.mType = mType;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

}
