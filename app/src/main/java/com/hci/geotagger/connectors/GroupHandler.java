/*
 * Group handler class is responsible for making calls to the webservice 
 * for group operations, such as adding and retrieving group list etc.
 * 
 * Syed M Shah
 * Spring 2014
 */

package com.hci.geotagger.connectors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.GeoLocation;
import com.hci.geotagger.objects.Group;
import com.hci.geotagger.objects.Tag;
import com.hci.geotagger.objects.UserAccount;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.NetworkUtils;

public class GroupHandler  extends GeotaggerHandler {
	private static String TAG = "GroupHandler";
	public static final String NAME = "GroupHandler";
	public String [] mActionsSupported = {
	};

	/**
	 * Constructor for the GroupHandler class.  The TagHandler class requires a context to make
	 * subsequent calls for caching.
	 * @param context current context
	 */
	public GroupHandler(Context context) {
		super(context, null);
        setActionList(mActionsSupported);
    }
	
	/**
	 * Overridden method to process the actions supported by this handler. This function
	 * is called during the handling of cached actions.
	 */
	@Override
	public ReturnInfo performServerDbOperation(String operation, JSONObject params) {
		if (operation.equals(WebAPIConstants.OP_ADD_TAG2GROUP))
			return addTagToGroupToServerDB(params);
		if (operation.equals(WebAPIConstants.OP_REMOVE_TAGFROMGROUP))
			return removeTagFromGroupToServerDB(params);
		if (operation.equals(WebAPIConstants.OP_ADD_MEMBER2GROUP))
			return addMemberToGroupToServerDB(params);
		if (operation.equals(WebAPIConstants.OP_REMOVE_MEMBERFROMGROUP))
			return removeMemberFromGroupToServerDB(params);

		return new ReturnInfo(ReturnInfo.FAIL_BADACTION);
	}


	
	/****************************************************************************************
	 * GROUP GET METHODS 
	 ****************************************************************************************/

	/**
	 * @author Syed M Shah 
	 * As new REST api does not need the ID of the Owner to get all the Groups associated to the user we just need the URL.
	 * I am keeping oId just for caching the   
	 * @param oId
	 * @return
	 */
	
	public ArrayList<Group> getMyGroups(long oId) {
		Log.d(TAG, "Entering getGroupById");
		ArrayList<Group> groups = new ArrayList<Group>();

		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			JSONArray groupData = getMyGroupsFromServer();
			JSONObject obj;
			if (groupData != null) { //groupId
				// loop through each JSON entry in the JSON array (groups encoded as JSON)
				for (int i = 0; i < groupData.length(); i++) {
					obj = null;
					try {
						obj = groupData.getJSONObject(i);
					} catch (JSONException e) {
						Log.d(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
	
					if (obj != null) {
						Group g = createOwnerGroupsJSON(obj);
						groups.add(g);

						// add/update the record in the cache
						cache.addGroup(g);
					}
				}
			}
		} else {
			// If the network was not up then lets check the cache for the records
			groups = cache.getAllUserGroups(oId);
		}
		
		Log.d(TAG, "Leaving getTagsById");
		return groups;
	}
	
	/**
	 * Return all of the Groups associated with a specific Owner ID.  The Group objects returned
	 * are contained in the returned ArrayList.
	 * @param oId The Owner ID to use for the request.
	 * @return The ArrayList containing the associated Groups is returned
	 */

	
	public ArrayList<Group> getGroupById(int oId) {
		ArrayList<Group> groups = null;
		Log.d(TAG, "Entering getGroupById");

		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			groups = new ArrayList<Group>();
			
			JSONArray groupData = getAllGroupsFromServer();
			JSONObject obj;
			if (groupData != null) { //groupId
				// loop through each JSON entry in the JSON array (groups encoded as JSON)
				for (int i = 0; i < groupData.length(); i++) {
					obj = null;
					try {
						obj = groupData.getJSONObject(i);
					} catch (JSONException e) {
						Log.d(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
	
					if (obj != null) {
						Group g = createGroupFromJSON(obj);
						groups.add(g);
						
						// add/update the record in the cache
						cache.addGroup(g);
					}
				}
			}
		} else {
			// If the network was not up then lets check the cache for the records
			groups = cache.getAllUserGroups(oId);
		}
		
		Log.d(TAG, "Leaving getGroupById");
		return groups;
	}
	
	/**
	 * @author Syed M Shah
	 * @return
	 * Cache the data in this Method for Paul
	 */
	public ArrayList<Group> getAllGroups() {
		ArrayList<Group> groups;
		Log.d(TAG, "Entering getUserTags");
		groups = new ArrayList<Group>();

		if (cache.performCachedActions()) {
			JSONArray groupData = getAllGroupsFromServer();
			JSONObject objOwner;
			JSONObject allGroups = new JSONObject();
			long oId;
			if (groupData != null) {
				// loop through each JSON entry in the JSON array (tags encoded as JSON)
				for (int i = 0; i < groupData.length(); i++) {
					objOwner = null;
					try {
						objOwner = groupData.getJSONObject(i).getJSONObject("owner");
						Log.d(TAG, "Tag ID: get object" );
						oId = objOwner.getLong("id");
						allGroups = groupData.getJSONObject(i);
						allGroups.put("oId", oId);
						Log.d(TAG, "New JSON: " +allGroups.toString());
						Log.d(TAG, "Owner ID: " +oId);
					} catch (JSONException e) {
						Log.d(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
		
					if (allGroups != null) {
						Log.d(TAG, "call Create group from json @param allGroups");
						Log.d(TAG, "Group Detail " + allGroups.toString());
						Group g = createGroupFromJSON(allGroups);
						groups.add(g);
					}
				}
			}
		} else {
			
		}
		Log.d(TAG, "Leaving getTagsById");
		return groups;
	}
	
	/**
	 * @author Syed M Shah
	 * @return
	 * Cache the data in this Method for Paul
	 */
	public ArrayList<Group> getAllMyGroups() {
		ArrayList<Group> groups;
		Log.d(TAG, "Entering getAllMyGroups");
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			groups = new ArrayList<Group>();
			
			JSONArray groupData = getMyGroupsFromServer();
//			JSONArray groupData = getAllGroupsFromServer();
			JSONObject objOwner;
			JSONObject allGroups = new JSONObject();
			long oId;
			if (groupData != null) {
				
				// loop through each JSON entry in the JSON array (tags encoded as JSON)
				for (int i = 0; i < groupData.length(); i++) {
					objOwner = null;
					try {
						objOwner = groupData.getJSONObject(i).getJSONObject("owner");
						Log.d(TAG, "Tag ID: get object" );
						oId= objOwner.getLong("id");
						allGroups = groupData.getJSONObject(i);
						allGroups.put("oId", oId);
						Log.d(TAG, "New JSON: " +allGroups.toString());
						Log.d(TAG, "Owner ID: " +oId);
					} catch (JSONException e) {
						Log.d(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
	
					if (allGroups != null) {
						Log.d(TAG, "call Create group from json @param allGroups");
						Log.d(TAG, "Group Detail " + allGroups.toString());
						Group g = createGroupFromJSON(allGroups);
						groups.add(g);
						cache.addGroup(g);
					}
				}
			}
		} else {
			groups = null;
		}
		Log.d(TAG, "Leaving getAllMyGroups");
		return groups;
	}
	
	
	private JSONArray getMyGroupsFromServer() {
		Log.d(TAG, "Entering getMyGroupsFromServer");
		JSONArray resultsArray = null;

		 // Building Parameters
        String url = WebAPIConstants.BASE_URL_GTDB + WebAPIConstants.PROFILE_MYGROUPS;
        Log.e(TAG, "URL="+url);
        
		try {
			 //make webservice call to get groups
			resultsArray = jsonParser.getJSONArrayForOwnerGroups_REST(url);
			if(resultsArray == null) {
				Log.d(TAG, "No Results");
			}
		} catch (Exception ex) {
			Log.d(TAG, "Exception occurred getting groups, returning null.");
		}

		Log.d(TAG, "Leaving getMyGroupsFromServer");
		return resultsArray;
	}
	
	/**
	 * Gets all the Groups Member and Owner for the user. 
	 * @return JSONArray
	 */
	private JSONArray getAllGroupsFromServer()
	{
		Log.d(TAG, "Entering getAllGroupsFromServer");
		JSONArray resultsArray = null;

		 // Building Parameters
        String url = WebAPIConstants.BASE_URL_GTDB + WebAPIConstants.GROUPS;
        
		try {
			 //make webservice call to get groups
			resultsArray = jsonParser.getJSONArrayForGroups_REST(url);

			if(resultsArray == null) {
				Log.d(TAG, "No Results");
			}
		} catch (Exception ex) {
			Log.d(TAG, "Exception occurred getting groups, returning null.");
		}

		Log.d(TAG, "Leaving getAllGroupsFromServer");
		return resultsArray;
	}


	/****************************************************************************************
	 * GROUP MEMBER GET METHODS 
	 ****************************************************************************************/

	public ArrayList<Group> getMyMemberGroups(long oId) {
		ArrayList<Group> groups = null;
		Log.d(TAG, "Entering getGroupById");

		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			groups = new ArrayList<Group>();
			
			JSONArray groupData = getMyMemberGroupsFromServer();
			JSONObject obj;
			if (groupData != null) { //groupId
				// loop through each JSON entry in the JSON array (groups encoded as JSON)
				for (int i = 0; i < groupData.length(); i++) {
					obj = null;
					try {
						obj = groupData.getJSONObject(i);
					} catch (JSONException e) {
						Log.d(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
	
					if (obj != null) {
						Group g = createOwnerGroupsJSON(obj);
						groups.add(g);
						// add/update the record in the cache
						cache.addGroup(g);
					}
				}
			}
		} else {
			// If the network was not up then lets check the cache for the records
			groups = cache.getAllUserGroups(oId);
		}
		
		Log.d(TAG, "Leaving getTagsById");
		return groups;
	}
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * This method will add a Group to the database.  The input Group object contains the
	 * necessary fields to be added to the database.
	 * @param g this is the Group object to add to the database
	 * @return Returns a ReturnInfo object which identifies the success of the Add operation
	 */
	public ReturnInfo AddGroup(Group g) {
		ReturnInfo dbresponse;
		Log.d(TAG, "Entering AddGroup");
		JSONObject group;
		JSONObject groupData;
		
		if (cache.performCachedActions()) {
			// Building JSONObject parameters
			try {
				group = new JSONObject();
				groupData = new JSONObject();
				groupData.put("name", g.getName());
				groupData.put("description", g.getDescription());
				//TODO: Can have image as well to be added later... REST API does not have image field for image
				//groupData.put("image", g.getImageUrl());
				group.put("group", groupData);
				
				dbresponse = addGroupToServer(group);
				
				if (dbresponse.success){
					if (! cache.addGroup((Group)dbresponse.object)) 
						dbresponse = new ReturnInfo(ReturnInfo.FAIL_NOCACHE);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				dbresponse = null;
			}
		} else {
			g.setId(cache.getnextGroupCacheID());
			if (cache.addGroup(g)) {
				dbresponse = new ReturnInfo();
				dbresponse.object = g;
			} else {
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_NONETWORK);
			}
		}
		Log.d(TAG, "Leaving AddGroup");
		return dbresponse;
	}
	
	/**
	 * Internal method to add a Group to the server.  This method makes the specific JSON calls
	 * to add the record to the database.
	 * @param params
	 * @return
	 */
	private ReturnInfo addGroupToServer(JSONObject params) {
		ReturnInfo result;
		Log.d(TAG, "Entering addTagToServer");
		String url = WebAPIConstants.BASE_URL_GTDB + WebAPIConstants.GROUPS;

        //make webservice call to add Group to db
		try {
			JSONObject json = jsonParser.postToServer(url, params);
			Log.d(TAG, "addGroupToServer: JSON Response from PHP: " + json.toString());
			result = new ReturnInfo(json);
			Log.d(TAG, "Result is " + result.success);
//			if (result.success)
//				result.object = createGroupFromJSON(json.getJSONObject("result"));
		} catch (Exception ex) {
			Log.d(TAG, "addGroupToServer: Exception occurred adding comment, returning null.");
			result = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		
		Log.d(TAG, "Leaving addGroupToServer");
		return result;
	}
	
	// TODO: CHECK IF THIS IS NOT A DUPLICATE OF TEH TagHandler
	public static Tag createTagFromJSON(JSONObject json) {
		Tag t = null;
		JSONObject owner;
		String ownerID;
		String imageURL;
		int oId;
		Log.d(TAG, "Entering createTagFromJSON");
		Date d = new Date();
    	try {
    		//Syed M Shah
    		//New Format for date
    		SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
    		Log.d("CreateTagFromJson", "Inside the Method ");
			d = ts.parse(json.getString("created_at"));
			Log.d("CreateTagFromJson:","Created at"+ d.toString());
    	//	SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//	d = ts.parse(json.getString("CreatedDateTime"));
			double lat, lon;
			if(!json.getString("latitude").equalsIgnoreCase("null") && ! json.getString("longitude").equalsIgnoreCase("null")) {
				lat = json.getDouble("latitude");
				lon = json.getDouble("longitude");
			} else {
				lat = 0;
				lon = 0;
			}
			
			if(!json.has("image_url")) {
				imageURL = "/res/drawable/tagimage.jpg";
			} else {
				imageURL = json.getString("image_url");
			}
			owner = json.getJSONObject("owner");
			Log.d(TAG, "Owner of Tag: " +owner.toString());
			ownerID = owner.getString("id");
			oId = Integer.parseInt(ownerID);
			Log.d(TAG, "ID Owner of Tag: " +oId);
			int vis = 1;
			
			GeoLocation geo = new GeoLocation(lat, lon);
//			commenting out because don't really need to pass username but hopefully doesn't break everything
//			//instantiate the tag object with properties from JSON
			t = new Tag(json.getLong("id"), json.getString("name"), json.getString("description"), imageURL,
					json.getString("location"),  json.getInt("rating"),
					oId, geo, d,vis);
			Log.d(TAG, "Tag Object"+ t.getId()+"####"+t.getName()+"###"+t.getImageUrl()+"###"+
					t.getLocationString()+"####");

		} catch (JSONException e) {
    		Log.d("TagHandler", "CreateTag from JSONObject failed");
			e.printStackTrace();
		} catch (ParseException e) {
			Log.d("TagHandler", "Problem parsing timestamp from JSON");
			e.printStackTrace();
		}
    	
		Log.d(TAG, "Leaving createTagFromJSON");
    	return t;
	}

	/**
	 * Static function to create a Group object from a JSON Object.  All JSON functions
	 * are meant to be contained within the connectors are of the project.
	 * @param json The JSONObject to convert to a Group object
	 * @return a Group object is returned
	 */
	public static Group createGroupFromJSON(JSONObject json) {
		Group g = null;
		
		long oId = 0;
		try {
			JSONObject owner = json.getJSONObject("owner");
			oId = owner.getLong("id");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.d(TAG, "Entering createGroupFromJSON");
		if(!json.has("oId"))
		{
			
		}

		Date d = new Date();
    	try {
    		//format the date
    		SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
    		Log.d("CreateGroupFromJson", "Inside the Method ");
			d = ts.parse(json.getString("created_at"));
			g = new Group(json.getLong("id"), oId, json.getString("name"), json.getString("description"), d);
			//g = new Group(json.getLong("group_id"), json.getInt("owner_user_id"), json.getString("name"), json.getString("description"), json.getString("image_url"), d);
		} catch (JSONException e) {
    		Log.d("GroupHandler", "CreateTag from JSONObject failed");
			e.printStackTrace();
		} catch (ParseException e) {
			Log.d("GroupHandler", "Problem parsing timestamp from JSON");
			e.printStackTrace();
		}
    	
		Log.d(TAG, "Leaving createGroupFromJSON");
    	return g;
	}
	
	public static Group createOwnerGroupsJSON(JSONObject json)
	{
		Group g = null;
		Log.d(TAG, "Entering createGroupFromJSON");

		Date d = new Date();
    	try 
    	{
    		//format the date
    		SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
    		Log.d("CreateGroupFromJson", "Inside the Method ");
			d = ts.parse(json.getString("created_at"));
			g = new Group(json.getLong("id"), json.getJSONObject("owner").getLong("id"), json.getString("name"), json.getString("description"), d);
			//g = new Group(json.getLong("group_id"), json.getInt("owner_user_id"), json.getString("name"), json.getString("description"), json.getString("image_url"), d);
		} 
    	catch (JSONException e) 
    	{
    		Log.d("GroupHandler", "CreateTag from JSONObject failed");
			e.printStackTrace();
		} catch (ParseException e) {
			Log.d("GroupHandler", "Problem parsing timestamp from JSON");
			e.printStackTrace();
		}
    	
		Log.d(TAG, "Leaving createGroupFromJSON");
    	return g;
	}
	
	private JSONArray getMyMemberGroupsFromServer() {
		Log.d(TAG, "Entering getGroupByIdFromServer");
		JSONArray resultsArray = null;

		if (cache.performCachedActions()) {
	        String url = WebAPIConstants.BASE_URL_GTDB + WebAPIConstants.PROFILE_GROUPS;
			try {
				 //make webservice call to get groups
				resultsArray = jsonParser.getJSONArrayForMemberGroups_REST(url);
				if (resultsArray == null) {
					Log.d(TAG, "No Results");
				}
			} catch (Exception ex) {
				Log.d(TAG, "Exception occurred getting groups, returning null.");
			}
		} else {
			
		}
		Log.d(TAG, "Leaving getGroupsByIdFromServer");
		return resultsArray;
	}
	
	
	
	public ReturnInfo editGroup(Group group) {
		ReturnInfo dbresponse;
		Log.d(TAG, "Entering editGroup");

		 // Building Parameters
		JSONObject params = new JSONObject();
		JSONObject groupParams = new JSONObject();
		try {
			groupParams.put("name", group.getName());
			groupParams.put("description", group.getDescription());
			params.put("group", groupParams);
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
 
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			dbresponse = editToServerDB(group.getId(), params);
			
			if (dbresponse.success) {
				cache.addGroup(group);
			} else {
				// TODO: if a failure then what?
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
				Log.d(TAG,"editGroup: Failure");
			}
		} else {
			// Add the Tag object to the cache
			if (cache.addGroup(group)) {
				dbresponse = new ReturnInfo();
				dbresponse.object = group;
			} else {
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_NONETWORK);
			}
			
			// add the AddTag request to the cached list of DB transactions
			try {
				params.put("id", group.getId());
			} catch (Exception e) {
			}

			cache.cacheAction(NAME, WebAPIConstants.OP_EDIT_ADV, params);
		}
		Log.d(TAG, "Leaving editAdventure");
		return dbresponse;
	}
	
	// TODO: Need to update the POST to a PUT
	// TODO: Need to handle getting the ID key from the cached action
	private ReturnInfo editToServerDB(long id, JSONObject params) {
		ReturnInfo result;
		Log.d(TAG, "Entering editToServerDB");
		
		String url = String.format(WebAPIConstants.ACC_FORMAT_EDITGROUP, WebAPIConstants.BASE_URL_GTDB, id);

		try {
			if (jsonParser.restPutCall(url, params)) {
				result = new ReturnInfo();
			} else {
				result = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
			}
		} catch (Exception ex) {
			Log.d(TAG, "editToServerDB: Exception occurred editing group, returning null.");
			result = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		
		Log.d(TAG, "Leaving editToServerDB");
		return result;
	}

	
	
	

	/**
	 * This method will delete a tag from the database
	 * @param tagId ID of the tag to remove
	 * @return ReturnInfo object returned which identifies operation success
	 */
	public boolean deleteGroup(long id) {
		boolean status = false;
		String url = WebAPIConstants.BASE_URL_GTDB+WebAPIConstants.GROUP_DELETE + id;
		// If the network is up then try to get the record from the Server DB
		if (NetworkUtils.isNetworkUp(context)) {
		    //make webservice call to remove tag from db
		    try {
				JSONObject json = jsonParser.deleteRequestToREST(url);
				Log.d(TAG,"JSON Response from PHP: " + json.toString());
				Log.e(TAG,"Status Code:"+ Integer.valueOf(json.getInt("statusCode")));
				if(json.getInt("statusCode")== 204) {
					status =  true;
				} else {
					status = false;
				}
				// Tell the cache to delete the Adventure record
				//TODO: work with Cache to update delete of the Group
			} catch (Exception ex) {
				Log.d(TAG, "deleteGroup: Exception occurred deleting group, returning error.");
				return false;
			}
		} else {
			//TODO: Add the delete group operation to the cache
			//TODO: Delete the group from the cache
		}
		Log.i(TAG, String.valueOf(status));
		return status;
		
	}

	
	
	/****************************************************************************************
	 * GROUP TAG METHODS 
	 ****************************************************************************************/
	
	/**
	 * This method will create a relationship between the tag and the group identified
	 * by the input ID values.
	 * @param tagID ID of the tag 
	 * @param groupID ID of the group
	 * @return true if the relationship was created
	 */
	public boolean addTagToGroup(long tagID, long groupID) {
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			if (addTagToGroupToServerDB(tagID, groupID)) {
				cache.addGroupTag(groupID, tagID);
				return true;
			} else {
				return false;
			}
		} else {
			// Add the Tag object to the cache
			if (cache.addGroupTag(groupID, tagID)) {
				// There are no post operations for this record
				JSONObject params = new JSONObject();
				try {
					params.put("groupID", groupID);
					params.put("tagID", tagID);
				} catch (Exception e) {
				}
				cache.cacheAction(NAME, WebAPIConstants.OP_ADD_TAG2GROUP, params);
			} else {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This method is called from the action cache handler, to create the group tag
	 * when the cached action is being performed.
	 * @param params the list of parameters for this action.
	 * @return
	 */
	private ReturnInfo addTagToGroupToServerDB(JSONObject params) {
		try {
			long groupID = params.getLong("groupID");
			long tagID = params.getLong("tagID");
			if (addTagToGroupToServerDB(tagID, groupID))
				return new ReturnInfo(ReturnInfo.SUCCESS);
			else
				return new ReturnInfo(ReturnInfo.FAIL_GENERAL);
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
	}

	private boolean addTagToGroupToServerDB(long tagID, long groupID) {
		return jsonParser.sendRelationToServerDB(false, "addgrouptag", "group", tagID, groupID);
	}
	
	/**
	 * This method will remove the relationship between the group and tag identified 
	 * by the input ID values.
	 * @param tagID ID of the tag
	 * @param groupID ID of the group
	 * @return true if the relation is removed
	 */
	public boolean removeTagFromGroup(long tagID, long groupID) {
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			if (removeTagFromGroupToServerDB(tagID, groupID)) {
				cache.deleteGroupTag(groupID, tagID);
				return true;
			} else {
				return false;
			}
		} else {
			// Add the Tag object to the cache
			if (cache.deleteGroupTag(groupID, tagID)) {
				// There are no post operations for this record
				JSONObject params = new JSONObject();
				try {
					params.put("groupID", groupID);
					params.put("tagID", tagID);
				} catch (Exception e) {
				}
				cache.cacheAction(NAME, WebAPIConstants.OP_REMOVE_TAGFROMGROUP, params);
			} else {
				return false;
			}
		}
		return true;
	}
	
	private ReturnInfo removeTagFromGroupToServerDB(JSONObject params) {
		try {
			long groupID = params.getLong("groupID");
			long tagID = params.getLong("tagID");
			if (removeTagFromGroupToServerDB(tagID, groupID))
				return new ReturnInfo(ReturnInfo.SUCCESS);
			else
				return new ReturnInfo(ReturnInfo.FAIL_GENERAL);
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
	}

	/**
	 * This method will remove a Tag to Group relationship. The input ID values identify
	 * the specific Tag and Group.
	 * @param tagID
	 * @param groupID
	 * @return
	 */
	private boolean removeTagFromGroupToServerDB(long tagID, long groupID) {
		return jsonParser.sendRelationToServerDB(true, "removegrouptag", "group", tagID, groupID);
	}
	
	
	/**
	 * Return an array list with all of the Tags for a given adventure ID.
	 * The array could come from the cache or from the Server database.
	 * @param id the adventure ID to use
	 * @return
	 */
	public ArrayList<Tag> getAllGroupTags(long groupID) {
		ArrayList<Tag> tags;
		if (cache.performCachedActions()) {
			tags = new ArrayList<Tag>();

			JSONArray tagData = getAllGroupTagsFromServer(groupID);
			JSONObject obj;
			if (tagData != null) {
				// loop through each JSON entry in the JSON array (tags encoded as JSON)
				for (int i = 0; i < tagData.length(); i++) {
					obj = null;
					try {
						obj = tagData.getJSONObject(i);
					} catch (JSONException e) {
						Log.e(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
	
					if (obj != null) {
						Tag t = TagHandler.createTagFromJSON(obj);
						tags.add(t);

						// TODO: add/update the record in the cache
					}
				}
			}
		} else {
			tags = cache.getGroupTags(groupID);
		}
		return tags;
	}
	
	private JSONArray getAllGroupTagsFromServer(long gId) {
		Log.d(TAG, "Entering getGroupByIdFromServer");
		JSONArray resultsArray = null;

		// Building Parameters
        String url = WebAPIConstants.BASE_URL_GTDB + WebAPIConstants.GROUPS+gId+"/tags";
        
		try {
			 //make webservice call to get groups
			resultsArray = jsonParser.getJSONArrayForTAGS_REST(url);

			if (resultsArray == null) {
				Log.d(TAG, "No Results");
			}
		} catch (Exception ex) {
			Log.d(TAG, "Exception occurred getting groups, returning null.");
		}

		Log.d(TAG, "Leaving getGroupsByIdFromServer");
		return resultsArray;
	}
	
	
	/****************************************************************************************
	 * GROUP ADVENTURES METHODS 
	 ****************************************************************************************/
	
	public ArrayList<Adventure> getGroupAdventures(long groupID) {
		ArrayList<Adventure> adventures;

		if (cache.performCachedActions()) {
			adventures = new ArrayList<Adventure>();

			JSONArray adventureData = getGroupsAdventuresFromServer(groupID);
			JSONObject obj;
			if (adventureData != null) {
				// loop through each JSON entry in the JSON array (tags encoded as JSON)
				for (int i = 0; i < adventureData.length(); i++) {
					obj = null;
					try {
						obj = adventureData.getJSONObject(i);
					} catch (JSONException e) {
						Log.e(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
	
					if (obj != null) {
//						Adventure a = adventureHandler.createAdventureFromJSON(obj);
//						adventures.add(a);

						// TODO: add/update the record in the cache
					}
				}
			}
		} else {
//			adventures = cache.getGroupAdventures(groupID);
			adventures = new ArrayList<Adventure>();
		}
		return adventures;
	}

	private JSONArray getGroupsAdventuresFromServer(long gId) {
		Log.d(TAG, "Entering getGroupsAdventuresFromServer");
		JSONArray resultsArray = null;

		 // Building Parameters
        String url = WebAPIConstants.BASE_URL_GTDB + WebAPIConstants.GROUPS+gId+"/adventures";
        
		try {
			//TODO:Parser may need to be fixed based on the REST API url and Parameters to be passed
			//make webservice call to get groups
			resultsArray = jsonParser.getJSONArrayForGroups_REST(url);

			if(resultsArray == null) {
				Log.d(TAG, "No Results");
			}
		} catch (Exception ex) {
			Log.d(TAG, "Exception occurred getting groups, returning null.");
		}

		Log.d(TAG, "Leaving getGroupsAdventuresFromServer");
		return resultsArray;
	}


	
	/****************************************************************************************
	 * GROUP MEMBERS METHODS 
	 ****************************************************************************************/
	
	public ArrayList<UserAccount> getGroupMembers(long groupID) {
		ArrayList<UserAccount> uaList = new ArrayList<UserAccount>();
		// If the network is up then try to get the record from the Server DB
		if (cache.performCachedActions()) {
			JSONArray uaData = getGroupMembersFromServerDb(groupID);
			JSONObject obj;
			if (uaData != null) {
				// loop through each JSON entry in the JSON array (tags encoded as JSON)
				for (int i = 0; i < uaData.length(); i++) {
					obj = null;
					try {
						obj = uaData.getJSONObject(i);
					} catch (JSONException e) {
						Log.e(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
	
					if (obj != null) {
						UserAccount u = AccountHandler.createAccountFromJSON(obj);
						uaList.add(u);
						
						// TODO: add/update the record in the cache
					}
				}
			}
		} else {
			uaList = cache.getGroupMembers(groupID);
		}
		return uaList;
	}

	/*
	 * Return a JSONArray (array of JSON Objects) containing
	 * all the tags for the given adventure ID.
	 */	
	private JSONArray getGroupMembersFromServerDb(long groupID) {
		String url = String.format(WebAPIConstants.ACC_FORMAT_GETGROUPMEMBERS,
				WebAPIConstants.BASE_URL_GTDB, groupID);

		try {
			JSONArray resultsArray = jsonParser.getJSONArrayForMembersUrlREST(url);
			if (resultsArray != null) {
				return resultsArray;
			} else {
				Log.d(TAG, "getPeopleInGroupsFromServer: No Results");
				//parse result into array of jsonobjects and return it
				return null;
			}
		} catch (Exception ex) {
			Log.e(TAG, "Exception occurred getting tags, returning null.");
			return null;
		}
	}
	
	public boolean addMemberToGroup(long memberID, long groupID) {
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			if (addMemberToGroupToServerDB(memberID, groupID)) {
				cache.addGroupMember(groupID, memberID);
				return true;
			} else {
				return false;
			}
		} else {
			// Add the Tag object to the cache
			if (cache.addGroupMember(groupID, memberID)) {
				// There are no post operations for this record
				JSONObject params = new JSONObject();
				try {
					params.put("groupID", groupID);
					params.put("memberID", memberID);
				} catch (Exception e) {
				}
				cache.cacheAction(NAME, WebAPIConstants.OP_ADD_MEMBER2GROUP, params);
			} else {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This method is called from the action cache handler, to create the group tag
	 * when the cached action is being performed.
	 * @param params the list of parameters for this action.
	 * @return
	 */
	public ReturnInfo addMemberToGroupToServerDB(JSONObject params) {
		try {
			long groupID = params.getLong("groupID");
			long memberID = params.getLong("memberID");
			if (addMemberToGroupToServerDB(memberID, groupID))
				return new ReturnInfo(ReturnInfo.SUCCESS);
			else
				return new ReturnInfo(ReturnInfo.FAIL_GENERAL);
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
	}

	private boolean addMemberToGroupToServerDB(long memberID, long groupID) {
		return 	jsonParser.sendRelationToServerDB(false, "addgroupmember", "group", memberID, groupID);
	}
	
	/**
	 * This method will remove a member from a group. The input IDs identify which group and
	 * member to create a relationship with.
	 * @param memberID
	 * @param groupID
	 * @return
	 */
	public boolean removeMemberFromGroup(long memberID, long groupID) {
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			if (removeMemberFromGroupToServerDB(memberID, groupID)) {
				cache.deleteGroupTag(groupID, memberID);
				return true;
			} else {
				return false;
			}
		} else {
			// Add the Tag object to the cache
			if (cache.deleteGroupTag(groupID, memberID)) {
				// There are no post operations for this record
				JSONObject params = new JSONObject();
				try {
					params.put("groupID", groupID);
					params.put("memberID", memberID);
				} catch (Exception e) {
				}
				cache.cacheAction(NAME, WebAPIConstants.OP_REMOVE_MEMBERFROMGROUP, params);
			} else {
				return false;
			}
		}
		return true;
	}
	
	private ReturnInfo removeMemberFromGroupToServerDB(JSONObject params) {
		try {
			long groupID = params.getLong("groupID");
			long memberID = params.getLong("memberID");
			if (removeTagFromGroupToServerDB(memberID, groupID))
				return new ReturnInfo(ReturnInfo.SUCCESS);
			else
				return new ReturnInfo(ReturnInfo.FAIL_GENERAL);
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
	}
	
	private boolean removeMemberFromGroupToServerDB(long memberID, long groupID) {
		return jsonParser.sendRelationToServerDB(true, "removegroupmember", "group", memberID, groupID);
	}

	
}
