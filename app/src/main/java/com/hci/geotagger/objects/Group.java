package com.hci.geotagger.objects;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hci.geotagger.R;
import com.hci.geotagger.common.Constants;

public class Group extends GeotaggerObject implements Serializable {

	private String name, description, imageUrl, ownerName;
	private int visibility;
	private long ownerId;
	private UserAccount owner;
	
	private Date createdDateTime;
	
	//TODO: These lists can be made generic in some way
	private ArrayList<Tag> tagArray;
	private ArrayList<UserAccount> peopleArray;
	private ArrayList<Adventure> adventureArray;	
	private ArrayList<Tag> storeAddTagList, storeRemoveTagList;
	private ArrayList<UserAccount> storeAddUserList, storeRemoveUserList;
	private ArrayList<Adventure> storeAddAdventureList, storeRemoveAdventureList;

	//constructor for if a User Account is available to associate with the tag
	//this is also used when a tag is first created locally and the id is not available
	public Group(String name, String desc, String url, UserAccount own) {
		init();
		this.setName(name);
		this.setDescription(desc);
		this.setImageUrl(url);
		this.setOwner(own);
	}
	
	//use this when getting tag object from db and ID is accessible. Also uses values for
	//owner id and name so a user account object does not need to be created at this time.
	public Group(long gID, int oId, String name, String desc, String url, Date ts) {
		init();
		this.setId(gID);
		this.setName(name);
		this.setDescription(desc);
		this.setImageUrl(url);
		this.setOwnerId(oId);		
		
		this.createdDateTime = ts;
	}
	
	public Group(long gID, long oId, String name, String desc, Date ts) {
		init();
		this.setId(gID);
		this.setName(name);
		this.setDescription(desc);
		this.setOwnerId(oId);		
		this.createdDateTime = ts;
	}
	
	private void init() {
		tagArray = new ArrayList<Tag>();
		peopleArray = new ArrayList<UserAccount>();
		adventureArray = new ArrayList<Adventure>();
		storeAddTagList = new ArrayList<Tag>();
		storeRemoveTagList = new ArrayList<Tag>();
		storeAddUserList = new ArrayList<UserAccount>();
		storeRemoveUserList = new ArrayList<UserAccount>();
		storeAddAdventureList = new ArrayList<Adventure>();
		storeRemoveAdventureList = new ArrayList<Adventure>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}


	public UserAccount getOwner() {
		return owner;
	}

	public void setOwner(UserAccount owner) {
		this.owner = owner;
	}


	public int getVisibility() {
		return visibility;
	}

	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public ArrayList<Tag> getTags() {
		return tagArray;
	}
	
	public void setTags(ArrayList<Tag> tags) {
		tagArray = tags;
	}
	
	public boolean addTag(Tag t) {
		if (t != null) {
			tagArray.add(t);
			return true;
		} else {
			return false;
		}
	}
	
	public void removeTag(long tagID) {
		for(int i = 0; i < tagArray.size(); i++)
		{
			long ID = tagArray.get(i).getId();
			if(ID == tagID)
			{
				int index = tagArray.indexOf(ID);
				tagArray.remove(index);
			}
		}		
	}

	public ArrayList<UserAccount> getUsers() {
		return peopleArray;
	}
	
	public void setUsers(ArrayList<UserAccount> people) {
		peopleArray = people;
	}

	/**
	 * These methods deal with adding and removing people from the adventure.	 
	 * NOTE: removePerson requires the prior call of getId on the person (UserAccount) to be removed.
	 */
	
	public boolean addPerson(UserAccount u)
	{
		if(u != null)
		{
			peopleArray.add(u);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void removePerson(long userID)
	{
		for(int i = 0; i < peopleArray.size(); i++)
		{
			long ID = peopleArray.get(i).getId();
			if(ID == userID)
			{
				int index = peopleArray.indexOf(ID);
				peopleArray.remove(index);
			}
		}		
	}
	
	public ArrayList<Adventure> getAdventures() {
		return adventureArray;
	}
	
	public void setAdventures(ArrayList<Adventure> adventure) {
		adventureArray = adventure;
	}

	public boolean addAdventure(Adventure u) {
		if(u != null) {
			adventureArray.add(u);
			return true;
		} else {
			return false;
		}
	}
	
	public void removeAdventure(long adventureID) {
		for (int i = 0; i < adventureArray.size(); i++) {
			long ID = adventureArray.get(i).getId();
			if (ID == adventureID) {
				int index = adventureArray.indexOf(ID);
				adventureArray.remove(index);
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
	
	public ArrayList<Tag> getStoreAddTagList()
	{
		return storeAddTagList;
	}
	
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
	
	public ArrayList<Tag> getStoreRemoveTagList()
	{
		return storeRemoveTagList;
	}
	
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
	
	public ArrayList<UserAccount> getStoreAddUserList()
	{
		return storeAddUserList;
	}
	
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
	
	public ArrayList<UserAccount> getStoreRemoveUserList()
	{
		return storeRemoveUserList;
	}
	
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
		// If there are currently no Tag associated with this Adventure then just add them all
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

	private boolean containsAdventure(long id) {
		for (Adventure adventure : adventureArray) {
			if (adventure.getId() == id)
				return true;
		}
		return false;
	}

	public void mergeAdventuresList(ArrayList<Adventure> list) {
		// If there are currently no Adventures associated with this Group then just add them all
		if (tagArray.size() == 0) {
			// there are no Adventures currently on the list, so add all of the input Adventures
			storeAddAdventureList = list;
			storeRemoveAdventureList = new ArrayList<Adventure>();
		} else if (list.size() == 0) {
			// no Adventures are selected so remove all that are currently on the list
			storeAddAdventureList = new ArrayList<Adventure>();
			storeRemoveAdventureList = adventureArray;
		} else {
			// Determine the list of Adventures to be added
			storeAddAdventureList = new ArrayList<Adventure>();
			for (Adventure adventure : list) {
				if (! containsAdventure(adventure.getId()))
					storeAddAdventureList.add(adventure);
			}
			
			// Determine the list of tags to be removed
			storeRemoveAdventureList = new ArrayList<Adventure>();
			for (Adventure adventure : adventureArray) {
				boolean found = false;
				for (Adventure newAdventure : list) { 
					if (adventure.getId() == newAdventure.getId()) {
						found = true;
						break;
					}
				}
				if (!found)
					storeRemoveAdventureList.add(adventure);
			}
		}
	}
	

	
	
	
	
	
	
	
	// define static values to instantiated object not needed
	public static final int LIST_LAYOUT_ID = R.layout.row;
	public static final int CHECK_LAYOUT_ID = R.layout.row_with_checkbox;
	public static final int CHECKBOX_ID = R.id.row_checkbox;

	@Override
	public int getListLayoutID() {
		return LIST_LAYOUT_ID;
	}
	
	@Override
	public View updateListView(View view) {
		TextView nameTxt = (TextView) view.findViewById(R.id.row_txtName);
		TextView descTxt = (TextView) view.findViewById(R.id.row_txtdesc);
		TextView timeTxt = (TextView) view.findViewById(R.id.row_txtTime);
		
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
		return super.updateListView(view);
	}
	
}
