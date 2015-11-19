package com.hci.geotagger.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.hci.geotagger.cache.CacheDatabase.CacheRef;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.NetworkUtils;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.GeotaggerHandler;
import com.hci.geotagger.connectors.JSONParser;
import com.hci.geotagger.connectors.ReturnInfo;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.AdventureMembers;
import com.hci.geotagger.objects.AdventureTags;
import com.hci.geotagger.objects.Comment;
import com.hci.geotagger.objects.Friend;
import com.hci.geotagger.objects.Group;
import com.hci.geotagger.objects.MyUserAccount;
import com.hci.geotagger.objects.Tag;
import com.hci.geotagger.objects.UserAccount;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * The CacheHandler class is the main class used to handle the Geotagger Cache operations.
 * Each activity that needs to cache records should have an instance of this class.
 * 
 * @author Paul Cushman
 *
 */
public class CacheHandler {
	private final String TAG = "CacheHandler";
	private Context c;
	private CacheDatabase geoTaggerData = null;		// Database used to maintain keys and file references
	private Random r;

	/*
	 * Action cache handling definitions
	 */
	
	/**
	 * Action cache post operation type ID for operation type/value pair.  The value IDs of this
	 * type/value pair is identified by the ACTION_<action>_POSTOP definitions.
	 */
	public static final String ACTION_OP_POSTID = "operation";
	
	/**
	 * Action cache post operation value ID for "update" operation of operation type/value pair.
	 * The typeID for this type/value pair is the ACTION_OP_POSTID type ID.
	 */
	public static final String ACTION_UPDATE_POSTOP = "update";

	/**
	 * Action cache post operation type ID for tagID type/value pair.  The value of this
	 * type/value pair is a Tag ID value (from the cache add Tag action).
	 */
	public static final String ACTION_TAGID_POSTID = "tagID";
	public static final String ACTION_TAGID2_POSTID = "tagId";
	
	/**
	 * Action cache post operation type ID for AccountID/MemberID type/value pair.  The value of this
	 * type/value pair is a Account ID value (from the cache add Account action).
	 */
	public static final String ACTION_ACCOUNTID_POSTID = "AccountID";
	
	/**
	 * Action cache post operation type ID for adventureID type/value pair.  The value of this
	 * type/value pair is a Adventure ID value (from the cache add Adventure action).
	 */
	public static final String ACTION_ADVENTUREID_POSTID = "adventureID";
	public static final String ACTION_ADVID_POSTID = "advId";
	
	/**
	 * Action cache post operation type ID for commentID type/value pair.  The value of this
	 * type/value pair is a Comment ID value (from the cache add comment action).
	 */
	public static final String ACTION_COMMENTID_POSTID = "commentID";
	
	/**
	 * Action cache post operation type ID for URL type/value pair.  The value of this type/value
	 * pair is a URL value (from the cache add image action).
	 */
	public static final String ACTION_IMAGEURL_POSTID = "imgUrl";

	/**
	 * Constructor to create a CacheHandler class object.
	 * @param context Context of the activity
	 */
	public CacheHandler(Context context)
	{
		this.c = context;
		geoTaggerData = new CacheDatabase(context);
		r = new Random();
	}

	/**
	 * This method should be called periodically to cleanup the cache.  NO other database
	 * operations should take place during this.
	 * TODO: Implement me
	 */
	public void cleanup() {
		logCacheStatus();
	}
	
	/****************************************************************************************
	 * Image Caching Methods 
	 ****************************************************************************************/

	/**
	 * This method will check if an image with the input key already exists in the image cache.
	 * @param key The key (URL) associated with the image, when it was cached.
	 * @return Returns true if the image is in the cache, false if not found.
	 */
	public boolean imageExists(String key) {
		String fileName = geoTaggerData.getCacheReferenceFileName(key);
		if (fileName == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * This method will add the input image bitmap to the image cache.
	 * @param key The key (URL) to associated with the cached image.
	 * @param bm The bitmap to save in the image cache
	 * @return Returns true if successfully cached, false if not
	 */
	public boolean add2Cache(String key, Bitmap bm) {
		String fileName;
		Log.d(TAG, "entering add2Cache");
		
		fileName = Constants.CACHE_FILEPREFIX + r.nextInt();
		Log.d(TAG, "add2Cache: fileName="+fileName);

		try {
			FileOutputStream fOut = c.openFileOutput(fileName, 0);
//			File file = new File(fileName);
//	        FileOutputStream fOut = new FileOutputStream(file);
	        bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
	        fOut.flush();
	        fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Save file error!");
			return false;
		}
		
		// Add a record to the Cache database
		geoTaggerData.addCacheReference(key, fileName);

		Log.d(TAG, "Added cache for "+key);

		Log.d(TAG, "leaving add2Cache");
		return true;
	}

	/**
	 * This method will add the input image bitmap to the image cache and create
	 * a key to be used temporarily.  This method is used when an image cannot be
	 * uploaded to the server.
	 * @param bm The bitmap to save in the image cache
	 * @return Returns the temporary key associated with the cached image
	 */
	public String add2Cache(Bitmap bm) {
		String fileName;
		Log.d(TAG, "entering add2Cache");
		
		fileName = Constants.CACHE_FILEPREFIX + r.nextInt();
		Log.d(TAG, "add2Cache: fileName="+fileName);

		try {
			FileOutputStream fOut = c.openFileOutput(fileName, 0);
//			File file = new File(fileName);
//	        FileOutputStream fOut = new FileOutputStream(file);
	        bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
	        fOut.flush();
	        fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Save file error!");
			return null;
		}
		
		URL url;
		try {
			url = new URL("file", "", fileName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		
		// Add a record to the Cache database
		geoTaggerData.addCacheReference(url.toString(), fileName);

		Log.d(TAG, "Added temporary cache for "+fileName);

		Log.d(TAG, "leaving add2Cache");
		return url.toString();
	}
	
	/**
	 * This method will decode a cached (image) bitmap based on the input
	 * BitmapFactory options.  The input key identifies the cached image.
	 * @param key The key associated with the cached image.
	 * @param o The bitmap options used to decode the image
	 * @return The decoded bitmap
	 */
	public Bitmap decodeCacheBitmap(String key, BitmapFactory.Options o) {
		String fileName;
		Log.d(TAG, "entering decodeCacheBitmap");
		
		fileName = geoTaggerData.getCacheReferenceFileName(key);
		if (fileName == null) {
			Log.d(TAG, "decodeCacheBitmap: No cache for "+key);
			return null;
		}
		Log.d(TAG, "decodeCacheBitmap: fileName="+fileName);

		InputStream inFile;
		try {
			inFile = c.openFileInput(fileName);
		} catch (FileNotFoundException ex) {
			Log.e(TAG, "cannot open file "+fileName);
			return null;
		}

		Bitmap bitmap = null;
		while (o.inSampleSize < 16) {
			try {
				bitmap = BitmapFactory.decodeStream(inFile, null, o);
				break;
			} catch (OutOfMemoryError e) {
				o.inSampleSize *= 2;
				Log.w(TAG, "Received OutOfMemoryError, increasing sample size");
			}
			try {
				inFile.reset();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		if (bitmap == null)
			Log.e(TAG, "could not decode bitmap stream!");
		
		try {
			inFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.d(TAG, "leaving decodeCacheBitmap");
		return bitmap;
	}
	
	/**
	 * This method will decode the specific bitmap and returns the decoded bitmap
	 * @param key The key that identifies the cached bitmap
	 * @return The bitmap associated with the decoded bitmap image.
	 */
	public Bitmap decodeCacheBitmap(String key) {
		String fileName;
		Log.d(TAG, "entering getCacheBitmap");
		
		fileName = geoTaggerData.getCacheReferenceFileName(key);
		if (fileName == null) {
			Log.d(TAG, "getCacheBitmap: No cache for "+key);
			return null;
		}
		Log.d(TAG, "getCacheBitmap: fileName="+fileName);

		InputStream inFile;
		try {
			inFile = c.openFileInput(fileName);
		} catch (FileNotFoundException ex) {
			Log.e(TAG, "cannot open file "+fileName);
			return null;
		}

		Bitmap bitmap = BitmapFactory.decodeStream(inFile);
		
		Log.d(TAG, "leaving getCacheBitmap");
		return bitmap;
	}

/*
	public InputStream add2Cache(String key, InputStream in) {
		OutputStream out;
		String fileName;
		boolean failure = false;
		
		fileName = Constants.CACHE_FILEPREFIX + r.nextInt();
		
		try {
			out = c.openFileOutput (fileName, 0);
		} catch (FileNotFoundException ex) {
			Log.d(TAG, "cannot open file "+fileName);
			return null;
		}
		
		// copy the input file to the new file
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = in.read(buffer)) != -1) {
			    out.write(buffer, 0, len);
			}
		} catch (IOException ex) {
			Log.d(TAG, "cannot copy to file "+fileName);
			failure = true;
		}
		
		try {
			out.close();
		} catch (Exception ex) {
			Log.d(TAG, "cannot copy to file "+fileName);
		}

		if (failure)
			return null;
		
		// Add a record to the Cache database
		geoTaggerData.addCacheReference(key, fileName);

		Log.d(TAG, "Added cache for "+key);

		return getCacheInputStream(key);
	}
	
	public InputStream getCacheInputStream(String key) {
		String fileName;
		fileName = geoTaggerData.getCacheReferenceFileName(key);
		if (fileName == null) {
			Log.d(TAG, "No cache for "+key);
			return null;
		}
		
		InputStream inFile;
		try {
			inFile = c.openFileInput(fileName);
		} catch (FileNotFoundException ex) {
			Log.e(TAG, "cannot open file "+fileName);
			return null;
		}
		
		Log.d(TAG, "Found cache for "+key);
		return inFile;
	}

*/
	
	/****************************************************************************************
	 * Tags Methods 
	 ****************************************************************************************/

	/**
	 * This method will add a Tag class record into the Geotagger cache
	 * @param tag The Tag class record to add to the cache
	 * @return Returns true if successfully added to the cache.
	 */
	public boolean addTag(Tag tag) {
		return geoTaggerData.addTag(tag);
	}
	
	public boolean editTag(Tag tag) {
		return geoTaggerData.editTag(tag);
	}
	
	public Tag getTag(long tagID) {
		return geoTaggerData.getTag(tagID);
	}
	
	/**
	 * This method will retrieve the list of Tag records associated with a
	 * specific owner.
	 * @param ownerID ID of the owner
	 * @return Returns an ArrayList of Tag class objects
	 */
	public ArrayList<Tag> getTags(long ownerID) {
		Tag [] tags;
		tags = geoTaggerData.getTags(ownerID);
		
		ArrayList<Tag> tagsArray = new ArrayList<Tag>();
		if (tags != null && tags.length > 0) {
			for (Tag tag : tags) {
				tagsArray.add(tag);
			}
		}
		return tagsArray;
	}

	/**
	 * This method will retrieve the list of Tag records associated with a
	 * specific adventure ID
	 * @param ownerID ID of the adventure
	 * @return Returns an ArrayList of Tag class objects
	 */
	public ArrayList<Tag> getAdventureTags(long advID) {
		Tag [] tags;
		tags = geoTaggerData.getAdventureTags(advID);
		
		ArrayList<Tag> tagsArray = new ArrayList<Tag>();
		if (tags != null && tags.length > 0) {
			for (Tag tag : tags) {
				tagsArray.add(tag);
			}
		}
		return tagsArray;
	}
	
	/**
	 * This method will delete the cache record with the input Tag ID
	 * @param tagID ID of the tag to delete
	 * @return Returns true if successfully deleted.
	 */
	public boolean deleteTag(long tagID) {
		return geoTaggerData.deleteTag(tagID);
	}

	/**
	 * This method will return the next available Tag cache ID value.  The cache
	 * ID values are used for when records cannot be added to the server database and
	 * need to be cached with a temporary ID value, until the record can be added. At
	 * that point the cache ID should be replaced with the ID from the database.
	 * @return A long integer cache ID is returned
	 */
	public long getnextTagCacheID() {
		return geoTaggerData.getnextTagCacheID();
	}

	/**
	 * This method will add the input Comment class record into the Geotagger cache.
	 * @param comment The Comment class record to add
	 * @return Returns true if successfully added to the cache
	 */
	public boolean addTagComment(Comment comment) {
		return geoTaggerData.addTagComment(comment);
	}
	
	/**
	 * This method will remove the Comment record from the Geotagger cache with the
	 * input comment ID
	 * @param commentId The ID of the record to delete
	 * @return Returns true if successfully removed
	 */
	public boolean deleteTagComment(long commentId) {
		return geoTaggerData.deleteTagComment(commentId);
	}
	
	/**
	 * This method will retrieve the list of Comment records that is associated with the
	 * input Tag ID.
	 * @param tagID The ID of the Tag
	 * @return Returns an ArrayList of the retrieved Comment class records
	 */
	public ArrayList<Comment> getTagComments(long tagID) {
		Comment [] comments;
		comments = geoTaggerData.getTagComments(tagID);
		
		ArrayList<Comment> commentArray = new ArrayList<Comment>();
		if (comments != null && comments.length > 0) {
			for (Comment comment : comments) {
				commentArray.add(comment);
			}
		}
		return commentArray;
	}
	
	/**
	 * This method will return the next available Comment cache ID value.  The cache
	 * ID values are used for when records cannot be added to the server database and
	 * need to be cached with a temporary ID value, until the record can be added. At
	 * that point the cache ID should be replaced with the ID from the database.
	 * @return A long integer cache ID is returned
	 */
	public long getnextTagCommentCacheID() {
		return geoTaggerData.getnextTagCommentCacheID();
	}

	
	/****************************************************************************************
	 * User Account Methods 
	 ****************************************************************************************/

	/**
	 * This method will check if the cache database contains a user account record for the
	 * user with the specified name and password.  If found in the cache database then a
	 * true value is returned, other wise false is returned.
	 * @param name The user name of the person logging in
	 * @param password The password of the person logging in
	 * @return True if the record is found, false otherwise
	 */
	public MyUserAccount loginByName(String name, String password) {
		MyUserAccount mua = geoTaggerData.getMyUserAccount(name, password);
		return mua;
	}
	
	/**
	 * This method will check if the cache database contains a user account record for the
	 * user with the specified ID and password.  If found in the cache database then a
	 * true value is returned, other wise false is returned.
	 * @param id The ID of the person logging in
	 * @param password The password of the person logging in
	 * @return True if the record is found, false otherwise
	 */
	public MyUserAccount loginById(int id, String password) {
		MyUserAccount mua = geoTaggerData.getMyUserAccount(id, password);
		return mua;
	}
	
	/**
	 * Add or update a UserAccount class record to the Geotagger cache
	 * @param ua The UserAccount record to add/update to the cache
	 * @return Returns true if successfull added/updated
	 */
	public boolean addAccount(UserAccount ua) {
		return geoTaggerData.addAccount(ua);
	}
	
	/**
	 * Get a UserAccount records from the Geotagger cache with the input user name
	 * @param username The user name string
	 * @return Returns the retrieved UserAccount record, or null
	 */
	public UserAccount getAccount(String username) {
		return geoTaggerData.getAccount(username);
	}
	
	/**
	 * Get a UserAccount record from the Geotagger cache with the input Account ID value
	 * @param id The ID of the record to retrieve
	 * @return Returns the retrieved UserAccount record, or null
	 */
	public UserAccount getAccount(int id) {
		return geoTaggerData.getAccount(id);
	}
	
	/**
	 * Gets the user name of the UserAccount cache record with the input Account ID value
	 * @param id The ID of the record to retrieve
	 * @return Returns the retrieved UserAccount record, or null
	 */
	public String getUsernameFromAccount(int id) {
		return geoTaggerData.getUsernameFromAccount(id);
	}
	
	/**
	 * This method will retrieve the list of Member records associated with a
	 * specific adventure ID
	 * @param ownerID ID of the adventure
	 * @return Returns an ArrayList of Tag class objects
	 */
	public ArrayList<UserAccount> getAdventureMembers(long advID) {
		UserAccount [] members;
		members = geoTaggerData.getAdventureMembers(advID);
		
		ArrayList<UserAccount> membersArray = new ArrayList<UserAccount>();
		if (members != null && members.length > 0) {
			for (UserAccount member : members) {
				membersArray.add(member);
			}
		}
		return membersArray;
	}
	

	/****************************************************************************************
	 * Friend Caching Methods 
	 ****************************************************************************************/

	public ArrayList<UserAccount> getFriends(long uID) {
		UserAccount [] friends;
		friends = geoTaggerData.getFriends(uID);
			
		ArrayList<UserAccount> friendArray = new ArrayList<UserAccount>();
		if (friends != null) {
			for (UserAccount friend : friends) {
				friendArray.add(friend);
			}
		}
		return friendArray;
	}

	/**
	 * This method will add a friend relationship to the cache.
	 * 
	 * TODO: Implement me
	 * @param uID The ID of the users account record
	 * @param fID The ID of the friend's account record
	 * @return Return true if the record is added to the cache
	 */
	public boolean addFriend(long uID, long fID) {
		return geoTaggerData.addFriend(uID, fID);
	}
	
	/**
	 * Delete a Friend record/association from the cache
	 * 
	 * TODO: Implement
	 * @param uID The ID of the user's account record
	 * @param fID The ID of the friend's Account record
	 * @return Returns true if the record is removed from the cache
	 */
	public boolean deleteFriend(long uID, long fID) {
		//TODO: Implement
		return true;
	}
	
	/**
	 * This method will add a friend association.  The input friend name
	 * will be converted to a account ID for the associated friend records.
	 * @param uID The account ID for the specific user.
	 * @param friendName The user name of the friend to add
	 * @return Returns true if successfully added, otherwise false.
	 */
	public boolean addFriend(long uID, String friendName) {
		long fID = geoTaggerData.getAccountID(friendName);
		
		if (fID >= 0) {
			return geoTaggerData.addFriend((long)uID, fID);
		}
		return false;
	}
	
	/****************************************************************************************
	 * AdventureTags Methods 
	 ****************************************************************************************/
	
	public boolean addAdventureTag(long adventureId, long tagId) {
		return geoTaggerData.addAdventureTag(adventureId, tagId);
	}

	public boolean deleteAdventureTag(long adventureID, long tagID) {
		return geoTaggerData.deleteAdventureTag(adventureID, tagID);
	}

	public boolean deleteAdventureTags(long adventureID) {
		return geoTaggerData.deleteAdventureTags(adventureID);
	}
	
	/****************************************************************************************
	 * AdventureMembers Methods 
	 ****************************************************************************************/
	
	public boolean addAdventureMember(long adventureId, long memberId) {
		return geoTaggerData.addAdventureMember(adventureId, memberId);
	}
	
	public boolean deleteAdventureMember(long adventureID, long memberID) {
		return geoTaggerData.deleteAdventureMember(adventureID, memberID);
	}

	public boolean deleteAdventureMembers(long adventureID) {
		return geoTaggerData.deleteAdventureMembers(adventureID);
	}
	
	/****************************************************************************************
	 * Adventure Caching Methods 
	 ****************************************************************************************/

	public long getnextAdventureCacheID() {
		return geoTaggerData.getnextAdventureCacheID();
	}

	/**
	 * This method will add an Adventure to the Geotagger cache
	 * 
	 * 	TODO: Implement me
	 * @param a The Adventure record to add to the cache
	 * @return Returns true if added to the cache
	 */
	public boolean addAdventure(Adventure a) {
		return geoTaggerData.addAdventure(a);
	}
	
	public boolean editAdventure(Adventure a) {
		return geoTaggerData.editAdventure(a);
	}
	
	/**
	 * This method will delete an Adventure record from the Geotagger cache
	 * 
	 * TODO: Implement me
	 * @param id ID of the Adventure record to delete
	 * @return Returns true if successfully removed from the cache
	 */
	public boolean deleteAdventure(long id) {
		return geoTaggerData.deleteAdventure(id);
	}
	
	/**
	 * This method will retrieve an Adventure record from the Geotagger cache
	 * 
	 * TODO: Implement me
	 * @param id ID of the Adventure to retrieve
	 * @return The Adventure record is returned, or null
	 */
	public Adventure getAdventure(int id) {
		return geoTaggerData.getAdventure(id);
	}

	/*
	 * Server implements the following query:
	 * SELECT * FROM adventures WHERE ID IN (SELECT DISTINCT AdvID FROM adventuremembers WHERE uID=".$id." UNION SELECT ID AS AdvID FROM adventures WHERE OwnerID=".$id.
	 * Server returns the following, array of:
	 * 'ID'=>$row['ID'],'OwnerID'=>$row['OwnerID'], 'Name'=>$row['Name'], 'Description'=>$row['Description'], 'Visibility'=>$row['Visibility'], 'CreatedDateTime'=>$row['CreatedDateTime']
	 */
	
	/**
	 * This method will retrieve the list of Adventure associated with a specific user.
	 * @param uID ID of the user
	 * @return Returns an ArrayList of Adventure records
	 */
	public ArrayList<Adventure> getAllUserAdventures(long uID) {
		Adventure [] adventures;
//		adventures = geoTaggerData.getAllUserAdventures(uID);
		adventures = geoTaggerData.getAdventures(uID);
		
		ArrayList<Adventure> adventureArray = new ArrayList<Adventure>();
		if (adventures != null) {
			for (Adventure adventure : adventures) {
				adventureArray.add(adventure);
			}
		}
		return adventureArray;
	}

	public ArrayList<Adventure> getAllAdventures() {
		Adventure [] adventures;
		adventures = geoTaggerData.getAdventures();
		
		ArrayList<Adventure> adventureArray = new ArrayList<Adventure>();
		if (adventures != null) {
			for (Adventure adventure : adventures) {
				adventureArray.add(adventure);
			}
		}
		return adventureArray;
	}
	
	/**
	 * This method will retrieve a list of adventures that the current
	 * user is a member of.
	 * @return The list of retrieved Adventures
	 */
	public ArrayList<Adventure> getAdventuresMemberOf() {
		Log.d(TAG, "getAdventuresMemberOf() Implement ME");
		
		Adventure [] adventures;
		adventures = geoTaggerData.getMemberAdventures(UserSession.CURRENTUSER_ID);
		
		ArrayList<Adventure> adventureArray = new ArrayList<Adventure>();
		if (adventures != null) {
			for (Adventure adventure : adventures) {
				adventureArray.add(adventure);
			}
		}
		return adventureArray;
	}
	

	
	
	/****************************************************************************************
	 * GroupTags Methods 
	 ****************************************************************************************/
	
	public boolean addGroupTag(long groupID, long tagID) {
		return geoTaggerData.addGroupTag(groupID, tagID);
	}
	
	public boolean deleteGroupTag(long groupID, long tagID) {
		return geoTaggerData.deleteGroupTag(groupID, tagID);
	}

	public boolean deleteGroupTags(long groupID) {
		return geoTaggerData.deleteGroupTags(groupID);
	}
	
	public ArrayList<Tag> getGroupTags(long groupID) {
		Tag [] tags;
		tags = geoTaggerData.getGroupTags(groupID);
		
		ArrayList<Tag> tagsArray = new ArrayList<Tag>();
		if (tags != null && tags.length > 0) {
			for (Tag member : tags) {
				tagsArray.add(member);
			}
		}
		return tagsArray;
	}


	/****************************************************************************************
	 * GroupMembers Methods 
	 ****************************************************************************************/
	
	public boolean addGroupMember(long groupID, long memberId) {
		return geoTaggerData.addGroupMember(groupID, memberId);
	}
	
	public boolean deleteGroupMember(long groupID, long memberID) {
		return geoTaggerData.deleteGroupMember(groupID, memberID);
	}

	public boolean deleteGroupMembers(long groupID) {
		return geoTaggerData.deleteGroupTags(groupID);
	}

	public ArrayList<UserAccount> getGroupMembers(long groupID) {
		UserAccount [] members;
		members = geoTaggerData.getGroupMembers(groupID);
		
		ArrayList<UserAccount> tagsArray = new ArrayList<UserAccount>();
		if (members != null && members.length > 0) {
			for (UserAccount member : members) {
				tagsArray.add(member);
			}
		}
		return tagsArray;
	}


	/****************************************************************************************
	 * Group Caching Methods 
	 ****************************************************************************************/

	public long getnextGroupCacheID() {
		return geoTaggerData.getnextGroupCacheID();
	}

	public boolean addGroup(Group g) {
		return geoTaggerData.addGroup(g);
	}

	public boolean deleteGroup(long id) {
		return geoTaggerData.deleteGroup(id);
	}
	
	public ArrayList<Group> getAllGroups() {
		Group [] groups;
		groups = geoTaggerData.getGroups();
		
		ArrayList<Group> groupArray = new ArrayList<Group>();
		if (groups != null) {
			for (Group group : groups) {
				groupArray.add(group);
			}
		}
		return groupArray;
	}
	
	public ArrayList<Group> getAllUserGroups(long uID) {
		Group [] groups;
		groups = geoTaggerData.getGroups(uID);
		
		ArrayList<Group> groupArray = new ArrayList<Group>();
		if (groups != null) {
			for (Group group : groups) {
				groupArray.add(group);
			}
		}
		return groupArray;
	}

	
	
	
	/****************************************************************************************
	 * General and Misc. Caching Methods 
	 ****************************************************************************************/

	/**
	 * This method will log status information about the cache.  Information about the size of
	 * the cache database, the image files in the cache, and the size of the cached files.
	 */
	public void logCacheStatus() {
		long fileSize = 0;
		long dbSize = geoTaggerData.getSize();
		
		Log.d(TAG, "Database size="+dbSize);
		
		// get information about the cached image files
		CacheRef [] imageCaches;
		imageCaches = geoTaggerData.getAllCacheReferenceRecords();
		
		if (imageCaches != null) {
			for (CacheRef imageCache : imageCaches) {
				File file = c.getFileStreamPath(imageCache.filename);
	//			File file = new File(imageCache.filename);
				long length = file.length();
				fileSize += length;
				
				Log.d(TAG, "Image: "+imageCache.filename+", Updated="+imageCache.updateTime+", Length="+length);
			}
			
			Log.d(TAG, "Cached filesize="+fileSize);
		} else {
			Log.d(TAG, "No imageCache records returned");
		}
	}

	/****************************************************************************************
	 * Action Caching Definitions and Methods 
	 ****************************************************************************************/
    
	private static final Object actionCacheLock = new Object();
	
	/**
	 * This method will add the input action to the Action Cache.
	 * @param action The string representing the database action.
	 * @return Return true if the action was added, otherwise false
	 */
	public boolean cacheAction(String handlerName, String action) {
		long recID;
		synchronized (actionCacheLock) {
			recID = geoTaggerData.addActionCache(handlerName, action);
		}
		if (recID >= 0)
			return true;
		return false;
	}
	
	/**
	 * This method will add the input action, represented by the list of
	 * NameValuePair objects, to the Action Cache.  
	 * @param params The list of NameValuePair object representing the db operation.
	 * @return Return true if the action was added, otherwise false
	 */
	public boolean cacheAction(String handlerName, String operation, JSONObject params) {
		CacheAction ca = new CacheAction(handlerName, operation, params);
		long recID;
		synchronized (actionCacheLock) {
			recID = geoTaggerData.addActionCache(ca);
		}
		if (recID >= 0)
			return true;
		return false;
	}

	/**
	 * This method will add the input action, represented by the list of
	 * NameValuePair objects, to the Action Cache.  
	 * @param params The list of NameValuePair object representing the db operation.
	 * @return Return true if the action was added, otherwise false
	 */
	public boolean cacheActionWithID(String handlerName, String operation, JSONObject params, long id) {
		CacheAction ca = new CacheAction(handlerName, operation, params);
		ca.addKey(CacheAction.CA_KEY_ID, id);
		long recID;
		synchronized (actionCacheLock) {
			recID = geoTaggerData.addActionCache(ca);
		}
		if (recID >= 0)
			return true;
		return false;
	}

	/**
	 * This method will put the Action in the action cache, including the post operations.
	 * @param url The URL associated with the database operation
	 * @param params The params used to perform the database operation
	 * @param postParams The params used to perform the post db operation
	 * @return Returns true if the action was cached successfully
	 */
	public boolean cacheAction(String handlerName, String operation, JSONObject params, List<CachePostAction> postParams) {
		CacheAction ca = new CacheAction(handlerName, operation, params);
		String postAction = CachePostAction.toString(postParams);
		if (postAction == null)
			return false;
		ca.postActions = postAction;
		
		long recID;
		synchronized (actionCacheLock) {
			recID = geoTaggerData.addActionCache(ca);
		}
		if (recID >= 0)
			return true;
		return false;
	}
	

	/**
	 * This is a diagnostic method to print out the list of currently cached
	 * action records.
	 */
	public void printCachedActions() {
		CacheAction [] cachedActions;
		
		// Get all of the cached actions
		cachedActions = geoTaggerData.getAllActionCache();
		
		// If there are any cached actions then perform them
		if (cachedActions != null && cachedActions.length > 0) {
			Log.d(TAG, "There are "+cachedActions.length+" Actions cached");
			for (CacheAction action : cachedActions) {
				Log.d(TAG, "Cached Action: "+action.actionParams);
				if (action.postActions != null && action.postActions.length() > 0)
					Log.d(TAG, "Post Action: "+action.postActions);
			}
		} else {
			Log.d(TAG, "There are NO Actions cached");
		}
	}
	public void printCacheDatabase() {
		if (! Constants.SHOW_DB_DUMPS) 
			return;
		
		Log.d(TAG, "Dumping the Database:");
		
		// Print the contents of the ACTION_TABLE_NAME
		printCachedActions();
		// Print the contents of the CACHEREF_TABLE_NAME
		// Print the contents of the ACCOUNT_TABLE_NAME
		// Print the contents of the FRIEND_TABLE_NAME
		// Print the contents of the CACHEREF_TABLE_NAME
		// Print the contents of the TAGS_TABLE_NAME
		Tag [] tags = geoTaggerData.getTags();
		if (tags != null) {
			Log.d(TAG, "There are "+tags.length+" Tag records");
			for (int i=0; i<tags.length; i++) {
				Log.d(TAG, i+": ID="+tags[i].getId()+", Name="+tags[i].getName());
			}
		}
		// Print the contents of the ADVENTURES_TABLE_NAME
		Adventure [] adventures = geoTaggerData.getAdventures();
		if (adventures != null) {
			Log.d(TAG, "There are "+adventures.length+" Adventure records");
			for (int i=0; i<adventures.length; i++) {
				Log.d(TAG, i+": ID="+adventures[i].getId()+", Name="+adventures[i].getName());
			}
		}
		// Print the contents of the ADVENTURETAGS_TABLE_NAME
		AdventureTags [] adventureTags = geoTaggerData.getAdventureTags();
		if (adventureTags != null) {
			Log.d(TAG, "There are "+adventureTags.length+" AdventureTags records");
			for (int i=0; i<adventureTags.length; i++) {
				Log.d(TAG, i+": AdventureID="+adventureTags[i].getAdvId()+", TagID="+adventureTags[i].getTagId());
			}
		}
		// Print the contents of the ADVENTUREMEMBERS_TABLE_NAME
		AdventureMembers [] adventureMembers = geoTaggerData.getAdventureMembers();
		if (adventureMembers != null) {
			Log.d(TAG, "There are "+adventureMembers.length+" AdventureMembers records");
			for (int i=0; i<adventureMembers.length; i++) {
				Log.d(TAG, i+": AdventureID="+adventureMembers[i].getAdvId()+", MemberID="+adventureMembers[i].getMemberId());
			}
		}
		
		Friend [] friends = geoTaggerData.getAllFriends();
		if (friends != null) {
			Log.d(TAG, "There are "+friends.length+" Friends records");
			for (int i=0; i<friends.length; i++) {
				Log.d(TAG, i+": UserID="+friends[i].getUserId()+", FriendID="+friends[i].getFriendId());
			}
		}
	}

	/**
	 * This method will perform any cached actions against the database.  If there are actions
	 * cached, this method will perform each action in sequence, and the associated post action
	 * operations.  When an action has been performed it will be removed from the cache.
	 * 
	 * TODO: Complete implementation and testing of this function
	 * @return Returns false if network is down or cannot finish cached actions
	 */
	public boolean performCachedActions() {
		// Checks if the network is up first.  Return false if NOT up
		if (!NetworkUtils.isNetworkUp(c))
			return false;
		
		synchronized (actionCacheLock) {
			JSONParser jsonParser = new JSONParser();

			CacheAction action;
				
			// while there are cached actions, perform the specific action
			while ((action = geoTaggerData.getFirstActionCache()) != null) {
				Log.d(TAG, "Get cached action parameters:");
				JSONObject params = action.getArray();
				if (params != null) {
					// Check if the network/database is not connected then return
					if (! NetworkUtils.isNetworkUp(c))
						break;
						
					GeotaggerHandler handler;
					handler = GeotaggerHandler.newHandler(action.handler, c);
					Object returnedRecord = null;
					boolean success;
						
					if (handler == null) {
						success = false;
					} else {
						ReturnInfo response;

						response = handler.performServerDbOperation(action.operation, params);
						success = response.success;
						if (response.success) {
							returnedRecord = response.object;
						}
					}
						
					// TODO: Need to perform post database operations
					// TODO: NEED to update ID fields in following records, that have been changed due to getting the record from the Server
					if (success && action.postActions != null) {
						performPostOp(action, returnedRecord);

					}
				}
					
				// Remove the cached action
				geoTaggerData.deleteActionCache(action.id);
			}
		}
		
		printCacheDatabase();
		return true;
	}

	private void updateCachedActions(String key, long oldValue, long newValue) {
		String oldSValue = Long.toString(oldValue);
		String newSValue = Long.toString(newValue);
		updateCachedActions(key, oldSValue, newSValue);
	}
	
	/**
	 * This method will update the input key/value in the Action Cache database
	 * @param key the value of the name of the name value pair to update
	 * @param oldValue the original value of the name value pair to update
	 * @param newValue the new value of the name value pair to update
	 */
	private void updateCachedActions(String key, String oldValue, String newValue) {
		// while there are cached actions, perform the specific action
		CacheAction [] cacheActions = geoTaggerData.getAllActionCache();

		for (CacheAction action : cacheActions) {
			JSONObject params = action.getArray();
			
			if (params != null && params.length() > 0) {
				boolean hasChanged = false;
				
				if (params.has(key)) {
					hasChanged = true;
					params.remove(key);
					try {
						params.put(key, newValue);
					} catch (Exception e) {
						hasChanged = false;
					}
				}
				
				// If a change was made then update the cached action
				if (hasChanged) {
					boolean updatedRec;
					updatedRec = action.update(params);
					if (updatedRec)
						updatedRec = geoTaggerData.modifyActionCache(action);
					
					if (!updatedRec)
						Log.w(TAG, "Could not update Action Cached record");
				}
			}
		}
	}
	

	/**
	 * This private method will perform the cached action's post action operations.
	 * Should have the following name/value pairs:
	 *    ACTION_OP_POSTID, {ACTION_UPDATE_POSTOP | ???}
	 *    ACTION_TAGID_POSTID, <TagID>
	 * or ACTION_COMMENTID_POSTID, <CommentID>
	 * or ACTION_IMAGEURL_POSTID, <URL>
	 * @param action The CacheAction object associated with this cached action
	 * @param newRecord this is the record to use in the post operation 
	 * @return Return true if successfully performed, otherwise false
	 */
	private boolean performPostOp(CacheAction action, Object recObject) {
		long cacheID;
		
		List<CachePostAction> postActionList = action.getPostArray();
		
		// if there is no post action list then return
		if (postActionList == null)
			return true;
		
		// Break out the post action operations
		for (CachePostAction cpa : postActionList) {
			// Handle Update operations
			// TODO: create objects to handle these operations
			if (cpa.action.equals(ACTION_UPDATE_POSTOP)) {
				// Handle Tag operations
				if (cpa.fieldID.equalsIgnoreCase(ACTION_TAGID_POSTID)) {
					cacheID = Long.valueOf(cpa.fieldValue);
					
					// TODO: need to update the cache by assigning the server records TagId to the cache record
					if (recObject instanceof Tag) {
						Tag tag = (Tag)recObject;
						Tag cacheRec;
						
						cacheRec = geoTaggerData.getTag(tag.getId());
						if (cacheRec != null) {
							Log.w(TAG, "performPostOp: Tag record already exists in the cache!");
							//TODO: SHould we replace or remove the temporary record???
						} else {
							if (!geoTaggerData.updateTagID(cacheID, tag.getId())) {
								Log.e(TAG, "performPostOp: could not update the tagID");
							}
						}
						// Still need to update the cached actions, regardless of the cached records
						updateCachedActions(cpa.fieldID, cacheID, tag.getId());
					} else {
						Log.e(TAG, "performPostOp: recObject is not an Tag type!");
					}
					// TODO: need to update any activities that have the Cache record TagID with the server id
				} else if (cpa.fieldID.equalsIgnoreCase(ACTION_COMMENTID_POSTID)) {
					cacheID = Long.valueOf(cpa.fieldValue);

					// Verify that this is a Comment record type, others are not valid
					if (recObject instanceof Comment) {
						Comment comment = (Comment)recObject;
						
						// Change the cached record's commentID to the server's commentID
						geoTaggerData.updateTagCommentID(cacheID, comment.getId());
					} else {
						Log.e(TAG, "performPostOp: recObject is not an Comment type!");
					}
					// TODO: need to update any activities that have the Cache record TagID with the server id
				} else if (cpa.fieldID.equalsIgnoreCase(ACTION_ADVENTUREID_POSTID) ||
						cpa.fieldID.equalsIgnoreCase(ACTION_ADVID_POSTID)) {
					cacheID = Long.valueOf(cpa.fieldValue);
					
					if (recObject instanceof Adventure) {
						Adventure adventure = (Adventure)recObject;
						Adventure cacheRec;
						
						cacheRec = geoTaggerData.getAdventure(adventure.getId());
						if (cacheRec != null) {
							Log.w(TAG, "performPostOp: Adventure record already exists in the cache!");
							//TODO: SHould we replace or remove the temporary record???
						} else {
							if (geoTaggerData.updateAdventureID(cacheID, adventure.getId())) {
								updateCachedActions(cpa.fieldID, cacheID, adventure.getId());
							} else {
								Log.e(TAG, "performPostOp: could not update the adventureID");
							}
						}
					} else {
						Log.e(TAG, "performPostOp: recObject is not an Adventure type!");
					}
				} else if (cpa.fieldID.equalsIgnoreCase(ACTION_IMAGEURL_POSTID)) {
					String fromUrl = cpa.fieldValue;
					String toUrl;
					if (recObject instanceof URL) {
						toUrl = ((URL)recObject).toString();
					} else if (recObject instanceof String) {
						toUrl = (String)recObject;
					} else {
						continue;
					}
					// Need to update the image cache references (just the key)
					geoTaggerData.updateCacheReferenceKey(fromUrl, toUrl);
					
					// Need to update the database
					// TODO: It is highly likely that this image is associated with one record type, although
					// images taken from the gallery can potentially have the same URL.
					geoTaggerData.updateTagCommentImageUrl(fromUrl, toUrl);
					geoTaggerData.updateTagImageUrl(fromUrl, toUrl);
					
					// This will update the cached actions with the update Image URL
					updateCachedActions(cpa.fieldID, fromUrl, toUrl);
				}
			}
		}		
		return true;
	}

}
