package com.hci.geotagger.objects;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hci.geotagger.R;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.connectors.AdventureHandler;

/**
 * This class represents the Adventure object. An Adventure object is essentially a collection tags grouped together by a user, and
 * also has information regarding user accounts associated with the adventure, a date and time the adventure was created
 * the user id of the person who created the adventure, and a description and name for the adventure.
 *
 */
public class Adventure extends GeotaggerObject implements Serializable 
{		
	private static final long serialVersionUID = 3L;
	private long oID;
	private int visibility;
	private String name, description; //, creatorName;
	private UserAccount creatorAccount;
	private Date createdDateTime, modified_at;
	private ArrayList<Tag> tagArray;
	private ArrayList<UserAccount> peopleArray;	
	private ArrayList<Tag> storeAddTagList, storeRemoveTagList;
	private ArrayList<UserAccount> storeAddUserList, storeRemoveUserList;
	
	private Bitmap bitmap = null;
	
	/*
	 * Initializes all fields except for creatorAccount.
	 * Used when creating a brand new adventure and id is not available.
	 * 
	 * @param:	All params are fields.
	 */
	public Adventure(long oId, String name, String desc)
	{
		this.setoID(oId);
		this.setName(name);
		this.setDescription(desc);
		this.tagArray = new ArrayList<Tag>();
		this.peopleArray = new ArrayList<UserAccount>();
		this.storeAddTagList = new ArrayList<Tag>();
		this.storeRemoveTagList = new ArrayList<Tag>();
		this.storeAddUserList = new ArrayList<UserAccount>();
		this.storeRemoveUserList = new ArrayList<UserAccount>();
	}
	
	public Adventure(long id, long oID, String name, 
			String newDescription, Date cTime, Date modi)
	{
		this.setId(id);
		this.setoID(oID);
		this.setName(name);
		this.setDescription(newDescription);
		//this.setCreatorName(cName);
		this.createdDateTime = cTime;
		this.modified_at = modi;
		this.tagArray = new ArrayList<Tag>();
		this.peopleArray = new ArrayList<UserAccount>();
		this.storeAddTagList = new ArrayList<Tag>();
		this.storeRemoveTagList = new ArrayList<Tag>();
		this.storeAddUserList = new ArrayList<UserAccount>();
		this.storeRemoveUserList = new ArrayList<UserAccount>();
	}

	public Adventure(int vis, long cID, String newName, 
			String newDescription, /*String cName,*/ Date cTime)
	{
		this.setVisibility(vis);
		this.setCreatorID(cID);	
		this.setName(newName);
		this.setDescription(newDescription);
		//this.setCreatorName(cName);
		this.createdDateTime = cTime;
		this.tagArray = new ArrayList<Tag>();
		this.peopleArray = new ArrayList<UserAccount>();
		this.storeAddTagList = new ArrayList<Tag>();
		this.storeRemoveTagList = new ArrayList<Tag>();
		this.storeAddUserList = new ArrayList<UserAccount>();
		this.storeRemoveUserList = new ArrayList<UserAccount>();
	}
	
	/**
	 * Adventure constructor that does not require a creatorAccount.
	 * 
	 * @param:	All params are fields.
	 */
	public Adventure(long nID, int vis, long cID, String newName, 
			String desc, /*String cName,*/ Date cTime)
	{
		this.setId(nID);
		this.setVisibility(vis);
		this.setCreatorID(cID);
		this.setName(newName);
		this.setDescription(desc);
		//this.setCreatorName(cName);		
		this.createdDateTime = cTime;		
		this.tagArray = new ArrayList<Tag>();
		this.peopleArray = new ArrayList<UserAccount>();
		this.storeAddTagList = new ArrayList<Tag>();
		this.storeRemoveTagList = new ArrayList<Tag>();
		this.storeAddUserList = new ArrayList<UserAccount>();
		this.storeRemoveUserList = new ArrayList<UserAccount>();
	}
	
	@Override
	public AdventureHandler getHandler(Context context) {
		return new AdventureHandler(context);
	}

	//All get and set methods are self-explanatory.
	
	public long getoID() {
		return oID;
	}

	public void setoID(long oID) {
		this.oID = oID;
	}

	public Date getModified_at() {
		return modified_at;
	}

	public void setModified_at(Date modified_at) {
		this.modified_at = modified_at;
	}

	public void setVisibility(int vis)
	{
		this.visibility = vis;
	}
	
	public int getVisibility()
	{
		return this.visibility;
	}
	
	public void setCreatorID(long cID)
	{
		this.oID = cID;
	}
	
	public long getCreatorID()
	{
		return this.oID;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setDescription(String desc)
	{
		this.description = desc;
	}
	
	public String getDescription()
	{
		return this.description;
	}	
	
	public void setCreatorAccount(UserAccount cAccount)
	{
		this.creatorAccount = cAccount;
	}
	
	public UserAccount getCreatorAccount()
	{
		return this.creatorAccount;
	}
	
	public void setCreatedDateTime(Date cTime)
	{
		this.createdDateTime = cTime;
	}
	
	public Date getCreatedDateTime()
	{
		return this.createdDateTime;
	}
	
	public ArrayList<Tag> getTags() {
		return tagArray;
	}
	
	public void setTags(ArrayList<Tag> tags) {
		tagArray = tags;
	}
	
	public ArrayList<UserAccount> getUsers() {
		return peopleArray;
	}
	
	public void setUsers(ArrayList<UserAccount> people) {
		peopleArray = people;
	}

	/**
	 * This method adds a tag to the adventure.	 
	 * @param T Tag object to add
	 */
	
	public boolean addTag(Tag t) {
		if (t != null) {
			tagArray.add(t);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * This method removes a tag from the adventure
	 * NOTE: removeTag requires the prior call of getId on the tag to be removed.
	 * @param tagID The tag object to remove from the adventure
	 */
	public void removeTag(long tagID) {
		for (int i = 0; i < tagArray.size(); i++) {
			long ID = tagArray.get(i).getId();
			if (ID == tagID) {
				int index = tagArray.indexOf(ID);
				tagArray.remove(index);
			}
		}		
	}
	
	/**
	 * This method adds a person to the adventure.	 
	 * @param u Useraccount to add to the adventure
	 */
	
	public boolean addPerson(UserAccount u) {
		if (u != null) {
			peopleArray.add(u);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * This method removes a person from the adventure.
	 * 	 * NOTE: removePerson requires the prior call of getId on the person (UserAccount) to be removed.
	 * @param userID userid of the account to be removed from the adventure
	 */
	public void removePerson(long userID) {
		for (int i = 0; i < peopleArray.size(); i++) {
			long ID = peopleArray.get(i).getId();
			if (ID == userID) {
				int index = peopleArray.indexOf(ID);
				peopleArray.remove(index);
			}
		}		
	}
	
	/**
	 * These methods deal with storing tags and users that are to be added and/or removed from the adventure.
	 * These methods are used by activities called from AdvEditTagTabActivity, AdvEditPeopleTabActivity and the 
	 * EditAdventureActivity since we want to ensure that all changes made to the Adventure occur only when the user
	 * presses the save or cancel buttons. Otherwise adding and removing tags and users would occur instantaneously 
	 * and we do not want that.  
	 */
	
	/**
	 * Stores a Tag to be added to the adventure after the user saves his changes.
	 * @param t The tag to be added to the Adventure
	 * @return boolean indicating whether the passed in tag was null or not
	 */
	
	public boolean addStoreTagList(Tag t)
	{
		if(t != null)
		{
			storeAddTagList.add(t);
			return true;
		}
		else
		{
			return false;
		}
	}
	/**
	 * Returns the currently stored taglist
	 * @return An ArrayList of Tags that were stored.
	 */
	public ArrayList<Tag> getStoreAddTagList()
	{
		return storeAddTagList;
	}
	
	/**
	 * Adds a tag to the removeTagStoreList for the tag to be removed after the user hits the save button
	 * @param t Tag to be stored to the removeStoreTagList
	 * @return boolean value indicating if tag was null or not.
	 */
	public boolean removeStoreTagList(Tag t)
	{
		if(t != null)
		{
			storeRemoveTagList.add(t);
			return true;
		}
		else
		{
			return false;
		}
	}
	/**
	 * Retrieves the removeStore tagList
	 * @return ArrayList of tags added to the StoreRemoveTagList 
	 */
	
	public ArrayList<Tag> getStoreRemoveTagList()
	{
		return storeRemoveTagList;
	}
	
	/**
	 * Saves a user account to the storeUserList for the account to be added after the user hits the save button.
	 * @param u
	 * @return boolean indicating whether the user account was null or not
	 */
	public boolean addStoreUserList(UserAccount u)
	{
		if(u != null)
		{
			storeAddUserList.add(u);
			return true;
		}
		else
		{
			return false;
		}
	}
	/**
	 * Returns the current storeAddUserList
	 * @return ArrayList of addUserList
	 */
	
	public ArrayList<UserAccount> getStoreAddUserList()
	{
		return storeAddUserList;
	}
	/**
	 * Adds the UserAccount to the removeStoreUserList to remove the user after the user hits the save button
	 * @param u the UserAccount to add to the removeStoreUserList
	 * @return boolean value indicating whether the UserAccount was null or not
	 */
	public boolean removeStoreUserList(UserAccount u)
	{
		if(u != null)
		{
			storeRemoveUserList.add(u);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Retrieves the storeRemoveUserList
	 * @return the ArrayList of StoreRemoveUserList
	 */
	public ArrayList<UserAccount> getStoreRemoveUserList()
	{
		return storeRemoveUserList;
	}
	/**
	 * Deletes all tags in the storeTagList arrayList
	 * @param list The arrayList of tags to delete
	 */
	
	public void emptyStoreTagList(ArrayList<Tag> list)
	{
		for(int i = 0; i < list.size(); i++)
		{
			if(list.size() > 0)
			{
				Tag t = list.get(i);
				t = null;
			}
		}
	}
	
	private boolean containsTag(long id) {
		for (Tag tag : tagArray) {
			if (tag.getId() == id)
				return true;
		}
		return false;
	}

	/**
	 * The input list will be the updated list of Tags for this Adventure. This function will
	 * create the Add and Remove lists based on what is currently in the tagArray and the input
	 * list.
	 * @param list this is what the tagArray should end up looking like
	 */
	public void mergeTagsList(ArrayList<Tag> list) {
		// If there are currently no Tag associated with this Adventure then just add them all
		if (tagArray.size() == 0) {
			// there are no tags currently on the list, so add all of the input tags
			storeAddTagList = list;
			storeRemoveTagList = new ArrayList<Tag>();
		} else if (list.size() == 0) {
			// no tags are selected so remove all that are currently on the list
			storeAddTagList = new ArrayList<Tag>();
			storeRemoveTagList = tagArray;
		} else {
			// Determine the list of tags to be added
			storeAddTagList = new ArrayList<Tag>();
			for (Tag tag : list) {
				if (! containsTag(tag.getId()))
					storeAddTagList.add(tag);
			}
			
			// Determine the list of tags to be removed
			storeRemoveTagList = new ArrayList<Tag>();
			for (Tag tag : tagArray) {
				boolean found = false;
				for (Tag newTag : list) { 
					if (tag.getId() == newTag.getId()) {
						found = true;
						break;
					}
				}
				if (!found)
					storeRemoveTagList.add(tag);
			}
		}
	}
	
	
	/**
	 * Deletes all users in the storeUserList arraylist
	 * @param list The arrayList of Users to delete
	 */
	public void emptyStoreUserList(ArrayList<UserAccount> list)
	{
		for(int i = 0; i < list.size(); i++)
		{
			if(list.size() > 0)
			{
				UserAccount u = list.get(i);
				u = null;
			}
		}
	}
	
	/**
	 * The input list will be the updated list of UserAccounts for this Adventure. This function will
	 * create the Add and Remove lists based on what is currently in the peopleArray and the input
	 * list.
	 * @param list this is what the peopleArray should end up looking like
	 */
	public void mergeUserList(ArrayList<UserAccount> list) {
		// If there are urrently no Tag associated with this Adventure then just add them all
		if (peopleArray.size() == 0) {
			// there are no tags currently on the list, so add all of the input tags
			storeAddUserList = list;
			storeRemoveUserList = new ArrayList<UserAccount>();
		} else if (list.size() == 0) {
			// no tags are selected so remove all that are currently on the list
			storeAddUserList = new ArrayList<UserAccount>();
			storeRemoveUserList = peopleArray;
		} else {
			// Put people to be added on the add list
			storeAddUserList = new ArrayList<UserAccount>();
			for (UserAccount newPerson : list) {
				boolean found = false;
				for (UserAccount person : peopleArray) { 
					if (person.getId() == newPerson.getId()) {
						found = true;
						break;
					}
				}
				if (!found)
					storeAddUserList.add(newPerson);
			}
			
			// Put people to be removed on the remove list
			storeRemoveUserList = new ArrayList<UserAccount>();
			for (UserAccount person : peopleArray) {
				boolean found = false;
				for (UserAccount newPerson : list) { 
					if (person.getId() == newPerson.getId()) {
						found = true;
						break;
					}
				}
				if (!found)
					storeRemoveUserList.add(person);
			}
		}
	}

	public Bitmap getBitmap() {
		return bitmap;
	}
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	// define static values to instantiated object not needed
	public static final int LIST_LAYOUT_ID = R.layout.row;

	@Override
	public int getListLayoutID() {
		return LIST_LAYOUT_ID;
	}
	
	@Override
	public View updateListView(View view) {
		TextView nameTxt = (TextView) view.findViewById(R.id.row_txtName);
		TextView descTxt = (TextView) view.findViewById(R.id.row_txtdesc);
		TextView timeTxt = (TextView) view.findViewById(R.id.row_txtTime);
		ImageView imgView = (ImageView) view.findViewById(R.id.row_thumbnail);
		
		if (nameTxt != null)
			nameTxt.setText(getName());
		if (descTxt != null)
			descTxt.setText(getDescription());
		if (timeTxt != null) {
			Date date = getCreatedDateTime();
			SimpleDateFormat df = new SimpleDateFormat(
					Constants.DATETIME_FORMAT);
			String formatted = df.format(date);
			timeTxt.setText(formatted);
		}
		// Set thumbnail of tag image to imageview
		if (imgView != null) {
			if (bitmap != null) {
				imgView.setImageBitmap(bitmap);
			} else {
				Bitmap default_bitmap = BitmapFactory.decodeResource(
						view.getResources(), R.drawable.icon);
				imgView.setImageBitmap(default_bitmap);
			}
		}
		return super.updateListView(view);
	}
	
	@Override
	public boolean inList(ArrayList<?> objList) {
		ArrayList<Adventure> list = (ArrayList<Adventure>)objList;
		for (Adventure adventure : list) {
			if (id == adventure.getId())
				return true;
		}
		return false;
	}
	
	
}