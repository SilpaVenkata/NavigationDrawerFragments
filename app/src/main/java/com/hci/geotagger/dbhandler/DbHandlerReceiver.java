package com.hci.geotagger.dbhandler;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONObject;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.AccountHandler;
import com.hci.geotagger.connectors.AdventureHandler;
import com.hci.geotagger.connectors.CommentHandler;
import com.hci.geotagger.connectors.GeotaggerHandler;
import com.hci.geotagger.connectors.GroupHandler;
import com.hci.geotagger.connectors.HandlerConstants;
import com.hci.geotagger.connectors.ImageHandler;
import com.hci.geotagger.connectors.ReturnInfo;
import com.hci.geotagger.connectors.TagHandler;
import com.hci.geotagger.connectors.UserHandler;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.AdventureTags;
import com.hci.geotagger.objects.Comment;
import com.hci.geotagger.objects.GeotaggerObject;
import com.hci.geotagger.objects.Group;
import com.hci.geotagger.objects.Login;
import com.hci.geotagger.objects.Tag;
import com.hci.geotagger.objects.UserAccount;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * This class implements the DbHandler Receiver functionality. This class extends the Thread class
 * 
 * @author Paul Cushman
 *
 */
public class DbHandlerReceiver extends Thread {
	private static final String TAG = "DbHandlerReceiver";
	
	private GeotaggerApplication mApp = null;
    public Handler mHandler;

    // Values for the currently handled message
    MessageLayout msgDetails;
	
    /**
     * Constructor for this class. Save the input application class to be used by the methods
     * of this class.
     * @param app The GeotaggerApplication class
     */
	public DbHandlerReceiver(GeotaggerApplication app) {
		mApp = app;
	}
	
	/**
	 * This method will start the thread associated with this DbHandler Receiver. This thread
	 * will process the messages sent to the associated message handler.
	 */
	public void run() {
		Looper.prepare();
		/**
		 * This is the message handler for the Database Handler
		 */
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.obj != null) {
					msgDetails = (MessageLayout)msg.obj;
				} else {
					msgDetails = null;
				}
				
				switch (msg.what) {
				case DbHandlerConstants.DBMSG_ADD:
					doAddRecord();
					break;
				case DbHandlerConstants.DBMSG_UPDATE:
					doUpdateRecord();
					break;
				case DbHandlerConstants.DBMSG_DELETE:
					doDeleteRecord();
					break;
					
				case DbHandlerConstants.DBMSG_GET_ALL_TAGS:
					doGetTags();
					break;
				case DbHandlerConstants.DBMSG_GET_TAG:
					doGetTag();
					break;
				case DbHandlerConstants.DBMSG_GET_ADVENTURE_TAGS:
					doGetAdventureTags();
					break;
				case DbHandlerConstants.DBMSG_GET_ADVENTURE_USERS:
					doGetAdventureUsers();
					break;
				case DbHandlerConstants.DBMSG_GET_USERS:
					doGetUsers();
//					doGetAllUsers();
					break;
				case DbHandlerConstants.DBMSG_GET_GROUPS:
					doGetGroups();
					break;
				case DbHandlerConstants.DBMSG_GET_GROUPS_OWNER:
					doGetGroupsOwnerOf();
					break;
				case DbHandlerConstants.DBMSG_GET_GROUPS_MEMBER:
					doGetGroupsMemberOf();
					break;
				case DbHandlerConstants.DBMSG_GET_GROUP_TAGS:
					doGetGroupTags();
					break;
				case DbHandlerConstants.DBMSG_GET_GROUP_MEMBERS:
					doGetGroupMembers();
					break;
				case DbHandlerConstants.DBMSG_GET_GROUP_ADVENTURES:
					doGetGroupAdventures();
					break;
					
				case DbHandlerConstants.DBMSG_GET_ALL_ADVENTURES:
					doGetAllAdventures();
					break;
				case DbHandlerConstants.DBMSG_GET_ADVENTURES_MEMBEROF:
					doGetAdventuresMemberOf();
					break;
				case DbHandlerConstants.DBMSG_GET_ADVENTURES_OWNEROF:
					doGetAdventuresOwnerOf();
					break;

				case DbHandlerConstants.DBMSG_GET_TAG_COMMENTS:
					doGetTagComments();
					break;
					
				case DbHandlerConstants.DBMSG_UPLOAD_IMAGE:
					doUploadImage();
					break;
				case DbHandlerConstants.DBMSG_GET_SCALED_IMAGES:
					getScaledImage();
					break;

				case DbHandlerConstants.DBMSG_LOGIN_BYNAME:
					loginByName();
					break;
				case DbHandlerConstants.DBMSG_LOGIN_VALIDATETOKEN:
					loginValidateToken();
					break;
				case DbHandlerConstants.DBMSG_LOGIN_REFRESHTOKEN:
					loginRefreshToken();
					break;

				}
			}
		};
		Looper.loop();
	}

	/**
	 * This method handle the Add Record action. The input object identifies the specific Add
	 * Record that will be called.
	 * NOTE: New record types will need to have an entry added to this method
	 */
	private void doAddRecord() {
		if (msgDetails.obj instanceof Adventure) {
			doAddAdventureRecord((Adventure)msgDetails.obj);
		} else if (msgDetails.obj instanceof Tag) {
			doAddTagRecord((Tag)msgDetails.obj);
		} else if (msgDetails.obj instanceof Comment) {
			doAddCommentRecord((Comment)msgDetails.obj);
		} else if (msgDetails.obj instanceof AdventureTags) {
			doAddAdventureTag((AdventureTags)msgDetails.obj);
		}
	}
	
	/**
	 * This method will add the input adventure record.
	 * @param adventure The adventure to add.
	 */
	private void doAddAdventureRecord(Adventure adventure) {
		AdventureHandler advHandler = null;
		advHandler = new AdventureHandler(msgDetails.context);
		ReturnInfo result = advHandler.addAdventure(adventure);
		if (!result.success) {
			msgDetails.obj = adventure;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_ADD, 0, 1, msgDetails);
			return;
		}
		
		Adventure newAdv = (Adventure)result.object;
		adventure.setId(newAdv.getId());
		
		// TODO: Need to handle any failures
		
		// Add the Tags to the AdventureTags
		for (Tag tag : adventure.getTags())
			advHandler.addTagToAdventure(tag.getId(), adventure.getId());
		
		// Add the users to the AdventureMembers
		for (UserAccount user : adventure.getUsers())
			advHandler.addUserToAdventureById(user.getId(), adventure.getId());
		msgDetails.obj = adventure;
		mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_ADD, 1, 1, msgDetails);
	}
	
	/**
	 * This method will add the input Tag record.
	 * @param tag The tag to add.
	 */
	private void doAddTagRecord(Tag tag) {
/*
		File tagImage;
		
		// Handle the Tag image
		tagImage = tag.getImageUploadFile();
		if (tagImage != null) {
			ImageHandler imageHandler = new ImageHandler(msgDetails.context);
			tag.setImageData(imageHandler.encodeImage(tagImage));
		} else {
			tag.setImageData("");
		}
	*/	
		// attempt to add tag
		TagHandler handler = new TagHandler(msgDetails.context);
		ReturnInfo response = null;

		//add tag to db
		response = handler.AddTag(tag);
		response.print("AddTagTask");
		
		// This is where the Tag should be associated with the Adventure, if intentFlags is 1
		if (response.success) { 
			msgDetails.obj = response.object;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_ADD, 1, 1, msgDetails);
		} else {
			msgDetails.obj = tag;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_ADD, 0, 1, msgDetails);
		}
	}

	/**
	 * This method will add the input Tag Comment record.
	 * @param comment The Tag Comment to add
	 */
	private void doAddCommentRecord(Comment comment) {
		CommentHandler handler = new CommentHandler(msgDetails.context);
		ReturnInfo response = null;

		response = handler.addTagComment(comment);
		
		if (response.success) {
			msgDetails.obj = response.object;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_ADD, 1, 1, msgDetails);
		} else {
			msgDetails.obj = comment;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_ADD, 0, 1, msgDetails);
		}
	}

	/**
	 * This method will add the input Adventure Tags record.
	 * @param advTag The AdventureTags record to add.
	 */
	private void doAddAdventureTag(AdventureTags advTag) {
		AdventureHandler advHandler = new AdventureHandler(msgDetails.context);

		if (advHandler.addTagToAdventure(advTag.getTagId(), advTag.getAdvId()))
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_ADD, 1, 1, msgDetails);
		else
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_ADD, 0, 1, msgDetails);
	}

	/**
	 * This method will update a record. The type of records is identified by the
	 * object type. 
	 * WARNING: Not all record types support the update action yet.
	 */
	private void doUpdateRecord() {
		if (msgDetails.obj instanceof Adventure) {
			Adventure adventure = (Adventure)msgDetails.obj;
			AdventureHandler advHandler = null;
			advHandler = new AdventureHandler(msgDetails.context);
			ReturnInfo result = advHandler.editAdventure(adventure);
			if (!result.success) {
				mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_UPDATE, 0, 1, msgDetails);
				return;
			}
			
			// TODO: Need to handle any failures

			// Add the Tags to the AdventureTags
			for (Tag tag : adventure.getStoreAddTagList())
				advHandler.addTagToAdventure(tag.getId(), adventure.getId());
			// Remove the Tags to the AdventureTags
			for (Tag tag : adventure.getStoreRemoveTagList())
				advHandler.removeTagFromAdventure(tag.getId(), adventure.getId());
			
			// Add the users to the AdventureMembers
			for (UserAccount user : adventure.getStoreAddUserList())
				advHandler.addUserToAdventureById(user.getId(), adventure.getId());
			// Remove the users to the AdventureMembers
			for (UserAccount user : adventure.getStoreRemoveUserList())
				advHandler.removeUserFromAdventure(user.getId(), adventure.getId());
		} else if (msgDetails.obj instanceof Tag) {
			Tag tag = (Tag)msgDetails.obj;
			TagHandler tagHandler = new TagHandler(msgDetails.context);
			ReturnInfo result = tagHandler.editTag(tag);
			if (!result.success) {
				mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_UPDATE, 0, 1, msgDetails);
				return;
			}
		} else if (msgDetails.obj instanceof UserAccount) {
			UserAccount user = (UserAccount)msgDetails.obj;
			UserHandler userHandler = new UserHandler(msgDetails.context);
			ReturnInfo result = userHandler.editUser(user);
			if (!result.success) {
				mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_UPDATE, 0, 1, msgDetails);
				return;
			}
		} else if (msgDetails.obj instanceof Group) {
			Group group = (Group)msgDetails.obj;
			GroupHandler groupHandler = null;
			groupHandler = new GroupHandler(msgDetails.context);
			ReturnInfo result = groupHandler.editGroup(group);
			if (!result.success) {
				mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_UPDATE, 0, 1, msgDetails);
				return;
			}
			
			// TODO: Need to handle any failures

			// Add the Tags to the GroupTags
			for (Tag tag : group.getStoreAddTagList())
				groupHandler.addTagToGroup(tag.getId(), group.getId());
			// Remove the Tags to the GroupTags
			for (Tag tag : group.getStoreRemoveTagList())
				groupHandler.removeTagFromGroup(tag.getId(), group.getId());
			
			// Add the Members to the GroupMembers
			for (UserAccount member : group.getStoreAddUserList())
				groupHandler.addMemberToGroup(member.getId(), group.getId());
			// Remove the Members to the GroupMembers
			for (UserAccount member : group.getStoreRemoveUserList())
				groupHandler.removeMemberFromGroup(member.getId(), group.getId());

			// Add the Adventures to the GroupMembers
			// TODO: IMPLEMENT THE Group Adventures
		}
		mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_UPDATE, 1, 1, msgDetails);
	}
	
	/**
	 * This method is used to delete a record. The type of record is identified by the
	 * object type of the input record. This is a generic method, supported by setting the
	 * appropriate methods in the associated Object (GeotaggerObject based) and the 
	 * Handler (GeotaggerHandler based).
	 * WARNING: Not all record types support the delete action.
	 */
	private void doDeleteRecord() {
		if (msgDetails.obj instanceof GeotaggerObject) {
			GeotaggerObject object = (GeotaggerObject)msgDetails.obj;
			GeotaggerHandler handler = object.getHandler(msgDetails.context);
			long id = object.getId();
			ReturnInfo response = handler.delete(id);
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_DELETE, response.success?1:0, 1, msgDetails);
		} else {
			// Return failed, not supported
			// TODO: add not supported message
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_DELETE, 0, 1, msgDetails);
		}
	}

	/**
	 * This method will get tags using the TagHandler class.
	 */
	private void doGetTags() {
		TagHandler tagHandler = null;
		tagHandler = new TagHandler(msgDetails.context);
		ArrayList<Tag> tags = tagHandler.getTagsById(UserSession.CURRENTUSER_ID);
		msgDetails.obj = tags;
		mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_ALL_TAGS, 1, 1, msgDetails);
	}
	
	/**
	 * This method get a specific Tag
	 */
	private void doGetTag() {
		TagHandler tagHandler = null;
		tagHandler = new TagHandler(msgDetails.context);
		Tag tag = (Tag)msgDetails.obj;
		tag = tagHandler.getTag(tag.getId());
		msgDetails.obj = tag;
		mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_TAG, 1, 1, msgDetails);
	}
	
	private void doGetUsers() {
		AccountHandler accountHandler = null;
		accountHandler = new AccountHandler(msgDetails.context);
		ArrayList<UserAccount> friends = accountHandler.getFriends(UserSession.CURRENTUSER_ID);
		msgDetails.obj = friends;
		mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_USERS, 1, 1, msgDetails);
	}
	
	private void doGetAllUsers() {
		UserHandler userHandler = null;
		userHandler = new UserHandler(msgDetails.context);
		ArrayList<UserAccount> users = userHandler.getUsers();
		msgDetails.obj = users;
		mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_USERS, 1, 1, msgDetails);
	}
	
	private void doGetGroups() {
		GroupHandler groupHandler = null;
		groupHandler = new GroupHandler(msgDetails.context);
		ArrayList<Group> groups = groupHandler.getAllGroups();
		msgDetails.obj = groups;
		mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_GROUPS, 1, 1, msgDetails);
	}

	private void doGetGroupsOwnerOf() {
		GroupHandler groupHandler = null;
		groupHandler = new GroupHandler(msgDetails.context);
		ArrayList<Group> groups = groupHandler.getAllMyGroups();
		msgDetails.obj = groups;
		mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_GROUPS_OWNER, 1, 1, msgDetails);
	}

	private void doGetGroupsMemberOf() {
		GroupHandler groupHandler = null;
		groupHandler = new GroupHandler(msgDetails.context);
		ArrayList<Group> groups = groupHandler.getMyMemberGroups(Constants.LOGIN_BYID);
		msgDetails.obj = groups;
		mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_GROUPS_MEMBER, 1, 1, msgDetails);
	}

	private void doGetGroupTags() {
		if (msgDetails.obj instanceof Group) {
			Group group = (Group)msgDetails.obj;
			GroupHandler groupHandler = null;
			groupHandler = new GroupHandler(msgDetails.context);
			ArrayList<Tag> tags = groupHandler.getAllGroupTags(group.getId());
			msgDetails.obj = tags;
			mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_GROUP_TAGS, 1, 1, msgDetails);
		} else {
			mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_GROUP_TAGS, 0, 1, msgDetails);
		}
	}

	private void doGetGroupMembers() {
		if (msgDetails.obj instanceof Group) {
			Group group = (Group)msgDetails.obj;
			GroupHandler groupHandler = null;
			groupHandler = new GroupHandler(msgDetails.context);
			ArrayList<UserAccount> members = groupHandler.getGroupMembers(group.getId());
			msgDetails.obj = members;
			mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_GROUP_MEMBERS, 1, 1, msgDetails);
		} else {
			mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_GROUP_MEMBERS, 0, 1, msgDetails);
		}
	}

	private void doGetGroupAdventures() {
		if (msgDetails.obj instanceof Group) {
			Group group = (Group)msgDetails.obj;
			GroupHandler groupHandler = null;
			groupHandler = new GroupHandler(msgDetails.context);
			ArrayList<Adventure> adventures = groupHandler.getGroupAdventures(group.getId());
			msgDetails.obj = adventures;
			mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_GROUP_ADVENTURES, 1, 1, msgDetails);
		} else {
			mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_GROUP_ADVENTURES, 0, 1, msgDetails);
		}
	}

	
	
	
	/**
	 * This method will compare two ArrayLists of GeotaggerObject types. If the lists are equal
	 * then a true value will be returned.  If there is a difference then false is returned.
	 * @param cache
	 * @param server
	 * @return true if the lists are equal, false if they are different
	 */
	private boolean compareGeotaggerArrays(ArrayList<GeotaggerObject> cache, ArrayList<GeotaggerObject> server) {
		if (server.size() != cache.size())
			return false;
		
		boolean diff = false;
		for (int i=0; i<server.size(); i++) {
			GeotaggerObject serverRec = server.get(i);
			boolean found = false;
			for (int j=0; j<cache.size(); j++) {
				GeotaggerObject cacheRec = cache.get(j);
				if (serverRec.equals(cacheRec)) {
					found = true;
					break;
				}
			}
			if (!found) {
				diff = true;
				break;
			}
		}
		if (diff) {
			return false;
		}
		return true;
	}
	
	private void doGetAllAdventures() {
		AdventureHandler adventureHandler;
		adventureHandler = new AdventureHandler(msgDetails.context);
		
		Object serverObject;
		// Get the cached adventures first and if some are found then return them
		serverObject = adventureHandler.getAllAdventures(HandlerConstants.FLAG_SERVER | HandlerConstants.FLAG_CACHE);
		msgDetails.obj = serverObject;

		// TODO: Add caching retrieval
		mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_ALL_ADVENTURES, 1, 1, msgDetails);
	}
	
	private void doGetAdventuresOwnerOf() {
		AdventureHandler adventureHandler;
		adventureHandler = new AdventureHandler(msgDetails.context);
		
		Object serverObject;
		// Get the cached adventures first and if some are found then return them
		serverObject = adventureHandler.getAdventuresOwnerOf(HandlerConstants.FLAG_SERVER | HandlerConstants.FLAG_CACHE);
		msgDetails.obj = serverObject;

		// TODO: Add caching retrieval
		mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_ADVENTURES_OWNEROF, 1, 1, msgDetails);
	}

	private void doGetAdventuresMemberOf() {
		AdventureHandler adventureHandler;
		adventureHandler = new AdventureHandler(msgDetails.context);
		
		Object serverObject;
		// Get the cached adventures first and if some are found then return them
		serverObject = adventureHandler.getAdventuresMemberOf(HandlerConstants.FLAG_SERVER | HandlerConstants.FLAG_CACHE);
		msgDetails.obj = serverObject;

		// TODO: Add caching retrieval
		mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_ADVENTURES_MEMBEROF, 1, 1, msgDetails);
	}
	
/*
	private void doGetAdventuresByUserID() {
		Log.d(TAG, "Starting doGetAdventuresByUserID");
		boolean sent = false;
		long userID = 0;
		AdventureHandler adventureHandler;
		adventureHandler = new AdventureHandler(msgDetails.context);
		if (msgDetails.obj instanceof Long)
			userID = (Long)msgDetails.obj;
		else if (msgDetails.obj instanceof UserAccount)
			userID = ((UserAccount)msgDetails.obj).getId();
		else {
			mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_ADVENTURES_BYUSER, 0, 1, msgDetails);
			return;
		}
		
		Object cacheObject;
		ArrayList<Adventure> cacheAdventures;
		// Get the cached adventures first and if some are found then return them
		cacheAdventures = adventureHandler.getAllAdventuresUserPartOf(userID, HandlerConstants.FLAG_CACHE);
		cacheObject = cacheAdventures;
		
		if (cacheAdventures != null && cacheAdventures.size() > 0) {
			Log.d(TAG, "doGetAdventuresByUserID: send cache retrieved records");
			msgDetails.obj = cacheAdventures;
			msgDetails.flags = DbHandlerConstants.FLAG_CACHE;
			mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_ADVENTURES_BYUSER, 1, 1, msgDetails);
			sent = true;
		}

		Object serverObject;
		// Get the cached adventures first and if some are found then return them
		serverObject = adventureHandler.getAllAdventuresUserPartOf(userID, HandlerConstants.FLAG_SERVER);

		// TODO: Need a better way to identify if the list of records is different between the server and cache
		// If there are adventures and the server and cache have same number the compare
		if (serverObject == null ||  
				!compareGeotaggerArrays((ArrayList<GeotaggerObject>)cacheObject, (ArrayList<GeotaggerObject>)serverObject)) {
			// Send the server retrieved records
			Log.d(TAG, "doGetAdventuresByUserID: send server retrieved records");
			msgDetails.obj = serverObject;
			msgDetails.flags = DbHandlerConstants.FLAG_SERVER;
			mApp.sendResponse(msgDetails.key,  DbHandlerConstants.DBMSG_GET_ADVENTURES_BYUSER, 1, 1, msgDetails);
		}
		Log.d(TAG, "Leaving doGetAdventuresByUserID");		
	}
	*/
	
	private void doGetAdventureTags() {
		AdventureHandler ah = new AdventureHandler(msgDetails.context);
		long advID = 0;
		if (msgDetails.obj instanceof Long)
			advID = (Long)msgDetails.obj;
		else if (msgDetails.obj instanceof Adventure)
			advID = ((Adventure)msgDetails.obj).getId();
		else {
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_ADVENTURE_TAGS, 0, 1, msgDetails);
			return;
		}
		
		ArrayList<Tag> tags = ah.getAllAdventureTags(advID, msgDetails.flags == DbHandlerConstants.FLAG_CACHE);
		if (tags == null) {
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_ADVENTURE_TAGS, 0, 1, msgDetails);
		} else {
			msgDetails.obj = tags;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_ADVENTURE_TAGS, 1, 1, msgDetails);
		}
	}

	private void doGetAdventureUsers() {
		AdventureHandler ah = new AdventureHandler(msgDetails.context);
		long advID = 0;
		if (msgDetails.obj instanceof Long)
			advID = (Long)msgDetails.obj;
		else if (msgDetails.obj instanceof Adventure)
			advID = ((Adventure)msgDetails.obj).getId();
		else {
			msgDetails.obj = null;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_ADVENTURE_USERS, 0, 1, msgDetails);
		}
		
		ArrayList<UserAccount> users = ah.getPeopleInAdventure(advID);
		if (users == null) {
			msgDetails.obj = null;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_ADVENTURE_USERS, 0, 1, msgDetails);
		} else {
			msgDetails.obj = users;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_ADVENTURE_USERS, 1, 1, msgDetails);
		}
	}

	private void doGetTagComments() {
		long tagID = 0;
		if (msgDetails.obj instanceof Long)
			tagID = (Long)msgDetails.obj;
		else if (msgDetails.obj instanceof Tag)
			tagID = ((Tag)msgDetails.obj).getId();
		else {
			msgDetails.obj = null;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_TAG_COMMENTS, 0, 1, msgDetails);
		}

		CommentHandler commentHandler = new CommentHandler(msgDetails.context);
		ArrayList<Comment> comments = commentHandler.getTagComments(tagID);
		if (comments == null) {
			msgDetails.obj = null;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_TAG_COMMENTS, 0, 1, msgDetails);
		} else {
			msgDetails.obj = comments;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_TAG_COMMENTS, 1, 1, msgDetails);
		}
	}

	/**************************************************************************************
	 * Begin image associated methods
	 *************************************************************************************/
	
	private void doUploadImage() {
		if (msgDetails.obj instanceof File) {
			File CurrentImage = (File) msgDetails.obj;
			Long imageID = uploadImage(msgDetails.context, CurrentImage);
			Log.d("AddTagActivity", "TAG Image ID: " + imageID);
			
			if (imageID != -1L) {
				msgDetails.obj = imageID;
				mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_UPLOAD_IMAGE, 1, 1, msgDetails);
				return;
			}
		}
		mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_UPLOAD_IMAGE, 0, 1, msgDetails);
	}
	
	private void getScaledImage() {
		ImageHandler imageHandler = new ImageHandler(msgDetails.context);
		
		if (msgDetails.obj instanceof DbHandlerScaledImageReq) {
			DbHandlerScaledImageReq gsi = (DbHandlerScaledImageReq)msgDetails.obj;
			
			for (int i=0; i<gsi.urls.length; i++) {
				Bitmap b = imageHandler.getScaledBitmapFromUrl(gsi.urls[i], gsi.width, gsi.height);
				int done = i+1 == gsi.urls.length ? 1 : 0;
				DbHandlerScaledImageRsp response = new DbHandlerScaledImageRsp(gsi.urls[i], b);
				msgDetails.obj = response;
				if (b != null) {
					mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_SCALED_IMAGES, 1, done, msgDetails);
				} else {
					mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_GET_SCALED_IMAGES, 0, done, msgDetails);
				}
			}
		}	
	}
	
	/**
	 * Upload an image to the server and set the URL.
	 * Currently both the geotaggerdev and geotagger production server url
	 * returns a url that points to the geotagger production server, which
	 * is in turn stored persistently in the geotagger application. Therefore,
	 * a temporary fix for demo purposes is to replace "geotagger" string with
	 * "geotaggerdev" string to store the currently used development environment. Once 
	 * this application points to the production environment, return original "response.url"
	 */
	private long uploadImage(Context context, File f) {
		//first check the size of the image file without getting pixels
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
		
		int height = options.outHeight;
		int width = options.outWidth;
		Log.d("Image Size", "H, W = " + height + ", " + width);
		//resize image if it is very large to avoid out of memory exception
		if (height > 2048 || width > 2048)
			options.inSampleSize = 4;
		else if(height > 1024 || width > 1024)
			options.inSampleSize = 2;
		
		//get bitmap pixels
		options.inJustDecodeBounds = false;
		b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
		height = b.getHeight();
		width = b.getWidth();
		Log.d("New Image Size", "H, W = " + height + ", " + width);
		if (height > 0 && width > 0) {
			
			
			// TODO: Need to handle the network down case
			
			ImageHandler imageHandler = new ImageHandler(context);

			ReturnInfo response = imageHandler.uploadImageToServer(b);
			b.recycle();
			response.print("AddImageTask");
			//temp fix until server returns correct url
//PWC			String tempUrl = response.url.replaceAll("geotagger", "geotaggerdev");
//			return tempUrl;
			
			return (Long)(response.object);
		} else {
			return -1L;
		}	
	}	

	/**************************************************************************************
	 * End image associated methods
	 *************************************************************************************/

	/**************************************************************************************
	 * Begin login associated methods
	 *************************************************************************************/

	/**
	 * Attempt to login using the username and password
	 */
	private void loginByName() {
		AccountHandler handler = new AccountHandler(msgDetails.context);

		if (msgDetails.obj instanceof Login) {
			Login login = (Login)msgDetails.obj;
			ReturnInfo response = handler.login(login.username, login.password);
			if (response != null) {
				response.print("LoginTask");
				msgDetails.obj = response;
				mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_LOGIN_BYNAME, 1, 1, msgDetails);
			} else {
				msgDetails.obj = null;
				mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_LOGIN_BYNAME, 0, 1, msgDetails);
			}
		}
	}

	/**
	 * This method will attempt to validate the input access_token. This is done by 
	 * performing a Get Profile Info request, using the input access token.
	 */
	private void loginValidateToken() {
		UserAccount ua = null;
		String accessToken = null;
		boolean success = false;
		AccountHandler handler = new AccountHandler(msgDetails.context);

		if (msgDetails.obj instanceof String) {
			accessToken = (String)msgDetails.obj;
			JSONObject response = handler.getProfileInfo();
			if (response != null) {
				ua = AccountHandler.createAccountFromJSON(response);
				if (ua != null) {
					success = true;
				} else {
					success = false;
				}
			} else {
				success = false;
			}
		}
		if (success) {
			msgDetails.obj = ua;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_LOGIN_VALIDATETOKEN, 1, 1, msgDetails);
		} else {
			msgDetails.obj = null;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_LOGIN_VALIDATETOKEN, 0, 1, msgDetails);
		}
	}
	
	/**
	 * This method will try to perform a refresh token login
	 */
	private void loginRefreshToken() {
		UserAccount ua = null;
		boolean success = false;
		AccountHandler handler = new AccountHandler(msgDetails.context);

		if (handler.refreshToken()) {
			JSONObject response = handler.getProfileInfo();
			if (response != null) {
				ua = AccountHandler.createAccountFromJSON(response);
				if (ua != null) {
					success = true;
				}
			}
		}
		
		if (success) {
			msgDetails.obj = ua;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_LOGIN_REFRESHTOKEN, 1, 1, msgDetails);
		} else {
			msgDetails.obj = null;
			mApp.sendResponse(msgDetails.key, DbHandlerConstants.DBMSG_LOGIN_REFRESHTOKEN, 0, 1, msgDetails);
		}
	}
	
	/**************************************************************************************
	 * End login associated methods
	 *************************************************************************************/
	
	/*
	 * The following are overloaded sendMessage methods. These methods will send a DbHandler
	 * action message to the Message Handler associated with this DbHandler Receiver instance.
	 */
	
	/**
	 * Send a message to this DbHandler Receiver's Message Handler
	 * @param key The Activity's key, needed to respond back to the Activity.
	 * @param id An ID used by the Activity . Probably to identify the specific request
	 * @param context The Activity's context, needed to perform some DB or Network actions
	 * @param action The DbHandler action to be performed. See the DbHandlerConstants.
	 * @param obj The object associated with the action
	 * @return Returns true if the message was sent, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action, Object obj) {
		return sendMessage(key, id, context, action, obj, DbHandlerConstants.FLAG_DEFAULT);
	}
	
	/**
	 * Send a message to this DbHandler Receiver's Message Handler
	 * @param key The Activity's key, needed to respond back to the Activity.
	 * @param id An ID used by the Activity . Probably to identify the specific request
	 * @param context The Activity's context, needed to perform some DB or Network actions
	 * @param action The DbHandler action to be performed. See the DbHandlerConstants.
	 * @param obj The object associated with the action
	 * @param flags Flags used by the action
	 * @return Returns true if the message was sent, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action, Object obj, int flags) {
		MessageLayout msgDetails = new MessageLayout(key, id, context, obj, flags);
		
		Message msg;
		msg = mHandler.obtainMessage(action, 0, 0, msgDetails);
		msg.sendToTarget();
		return true;
	}
	
	/**
	 * Send a message to this DbHandler Receiver's Message Handler
	 * @param key The Activity's key, needed to respond back to the Activity.
	 * @param id An ID used by the Activity . Probably to identify the specific request
	 * @param context The Activity's context, needed to perform some DB or Network actions
	 * @param action The DbHandler action to be performed. See the DbHandlerConstants.
	 * @param index An index value used by the action
	 * @param obj The object associated with the action
	 * @return Returns true if the message was sent, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action, int index, Object obj) {
		return sendMessage(key, id, context, action, index, obj, DbHandlerConstants.FLAG_DEFAULT);
	}
	
	/**
	 * Send a message to this DbHandler Receiver's Message Handler
	 * @param key The Activity's key, needed to respond back to the Activity.
	 * @param id An ID used by the Activity . Probably to identify the specific request
	 * @param context The Activity's context, needed to perform some DB or Network actions
	 * @param action The DbHandler action to be performed. See the DbHandlerConstants.
	 * @param index An index value used by the action
	 * @param obj The object associated with the action
	 * @param flags Flags used by the action
	 * @return Returns true if the message was sent, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action, int index, Object obj, int flags) {
		MessageLayout msgDetails = new MessageLayout(key, id, context, obj, flags);
		
		Message msg;
		msg = mHandler.obtainMessage(action, index, 0, msgDetails);
		msg.sendToTarget();
		return true;
	}

	/**
	 * Send a message to this DbHandler Receiver's Message Handler
	 * @param key The Activity's key, needed to respond back to the Activity.
	 * @param id An ID used by the Activity . Probably to identify the specific request
	 * @param context The Activity's context, needed to perform some DB or Network actions
	 * @param action The DbHandler action to be performed. See the DbHandlerConstants.
	 * @return Returns true if the message was sent, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action) {
		return sendMessage(key, id, context, action, DbHandlerConstants.FLAG_DEFAULT);
	}
	/**
	 * Send a message to this DbHandler Receiver's Message Handler
	 * @param key The Activity's key, needed to respond back to the Activity.
	 * @param id An ID used by the Activity . Probably to identify the specific request
	 * @param context The Activity's context, needed to perform some DB or Network actions
	 * @param action The DbHandler action to be performed. See the DbHandlerConstants.
	 * @param flags Flags used by the action
	 * @return Returns true if the message was sent, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action, int flags) {
		MessageLayout msgDetails = new MessageLayout(key, id, flags);
		msgDetails.context = context;

		Message msg;
		msg = mHandler.obtainMessage(action, msgDetails);
		msg.sendToTarget();
		return true;
	}
	
	/**
	 * This class is used to send arguments associated with a DbHandler action
	 * @author Paul Cushman
	 */
	public class MessageLayout {
		String key;			// Unique identifier for the Activity sending requests
		String command;		// Not used
		Context context;	// Activity's context used by the DbHandler receiver
		Object obj;			// Object to perform the action on or returned object
		int messageID;		// Identifier used by the Activity, no meaning to the DbHandler
		int flags;			// Flags used by the action
		
		public MessageLayout(String key, int id) {
			this(key, id, DbHandlerConstants.FLAG_DEFAULT);
		}
		public MessageLayout(String key, int id, int flags) {
			this.key = key;
			this.messageID = id;
			command = null;
			context = null;
			obj = null;
			this.flags = flags;
		}
		public MessageLayout(String key, int id, Context context, Object obj) {
			this(key, id, context, obj, DbHandlerConstants.FLAG_DEFAULT);
		}
		public MessageLayout(String key, int id, Context context, Object obj, int flags) {
			this.key = key;
			this.messageID = id;
			command = null;
			this.context = context;
			this.obj = obj;
			this.flags = flags;
		}
	}
	
}
