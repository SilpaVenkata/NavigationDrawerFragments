/**
 * Adventure handler class is responsible for making calls to the webservice 
 * for adventure operations, such as adding and retrieving adventures.
 */
package com.hci.geotagger.connectors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.hci.geotagger.cache.CacheHandler;
import com.hci.geotagger.cache.CachePostAction;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.NetworkUtils;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.Tag;
import com.hci.geotagger.objects.UserAccount;

public class AdventureHandler extends GeotaggerHandler {
	private static final String TAG = "AdventureHandler";
	public static final String NAME = "AdventureHandler";
	public String [] mActionsSupported = {
			WebAPIConstants.OP_ADD_ADV, 				// Add an Adventure
			WebAPIConstants.OP_EDIT_ADV,				// Edit an Adventure
			WebAPIConstants.OP_DELETE_ADV, 			// Delete an Adventure
			WebAPIConstants.OP_ADD_TAG2ADV, 			// Add a Tag to an Adventure
			WebAPIConstants.OP_REMOVE_TAGFROMADV,		// Remove a Tag from an Adventure
			WebAPIConstants.OP_ADD_PERSON,			// Add a person to an Adventure
			WebAPIConstants.OP_REMOVE_PERSONFROMADV	// Remove a person from an Adventure
	};
	
    private static final Object queryLock = new Object();

	public AdventureHandler(Context context) {
		super(context, WebAPIConstants.ADV_URL);
		setActionList(mActionsSupported);
    }
	
	/**
	 * Overridden method to process the actions supported by this handler. This function
	 * is called during the handling of cached actions.
	 */
	@Override
	public ReturnInfo performServerDbOperation(String operation, JSONObject params) {
		if (operation.equals(WebAPIConstants.OP_ADD_ADV))
			return addToServerDB(params);
		if (operation.equals(WebAPIConstants.OP_EDIT_ADV))
			return editToServerDB(params);
		if (operation.equals(WebAPIConstants.OP_DELETE_ADV))
			return deleteFromServerDB(params);
		if (operation.equals(WebAPIConstants.OP_REMOVE_TAGFROMADV))
			return removeAdventureTagfromServerDB(params);
		if (operation.equals(WebAPIConstants.OP_ADD_TAG2ADV))
			return addTagToAdventureToServerDB(params);
		if (operation.equals(WebAPIConstants.OP_ADD_PERSON))
			return this.addAdventureUserToServerDB(params);
		if (operation.equals(WebAPIConstants.OP_REMOVE_PERSONFROMADV))
			return removeAdventureUserfromServerDB(params);

		return new ReturnInfo(ReturnInfo.FAIL_BADACTION);
	}


	/**
	 * This method will add an Adventure record to the database.  If the network
	 * is NOT available then the operation will be cached.
	 * TODO: Add caching of the operation.
	 * @param a
	 * @return ReturnInfo return info object that is result of adding adventure to server
	 */
	public ReturnInfo addAdventure(Adventure a) {
		ReturnInfo dbresponse;
		Log.d(TAG, "Entering AddAdventure");

		 // Building Parameters
		JSONObject params = new JSONObject();
		JSONObject advParams = new JSONObject();
//		JSONObject ownerParams = new JSONObject();
		try {
//			ownerParams.put("userID", a.getCreatorID());
//			params.put("owner", ownerParams);
			advParams.put("name", a.getName());
			advParams.put("description", a.getDescription());
			params.put("adventure", advParams);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
 
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			dbresponse = addToServerDB(params);
			
			if (dbresponse.success) {
				if (dbresponse.object instanceof Adventure) {
					// Add the Adventure object to the cache
					if (! cache.addAdventure((Adventure)(dbresponse.object)))
						dbresponse = new ReturnInfo(ReturnInfo.FAIL_NOCACHE);
				} else {
					// TODO: if NOT an instance of a Comment then what?
					dbresponse = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
				}
			} else {
				// TODO: if a failure then what?
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
				Log.d(TAG,"addAdventure: Failure");
			}
		} else {
			// Since record was not added we need a Adventure ID, so we use the next Cache Tag ID
			a.setId(cache.getnextAdventureCacheID());
			
			// Add the Tag object to the cache
			if (cache.addAdventure(a)) {
				dbresponse = new ReturnInfo();
				dbresponse.object = a;
			} else {
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_NONETWORK);
			}
			
			// TODO: Create post commands for the Action cache record.
	        List<CachePostAction> postParams = new ArrayList<CachePostAction>();
	        postParams.add(new CachePostAction(CacheHandler.ACTION_UPDATE_POSTOP, CacheHandler.ACTION_ADVENTUREID_POSTID, a.getId()));
	        postParams.add(new CachePostAction(CacheHandler.ACTION_UPDATE_POSTOP, CacheHandler.ACTION_ADVID_POSTID, a.getId()));
			
			// add the AddTag request to the cached list of DB transactions
			cache.cacheAction(NAME, WebAPIConstants.OP_ADD_ADV, params, postParams);
		}
		Log.d(TAG, "Leaving addAdventure");
		return dbresponse;
	}

	
	public ReturnInfo editAdventure(Adventure a) {
		ReturnInfo dbresponse;
		Log.d(TAG, "Entering editAdventure");

		 // Building Parameters
		JSONObject params = new JSONObject();
		JSONObject advParams = new JSONObject();
		try {
			advParams.put("name", a.getName());
			advParams.put("description", a.getDescription());
			params.put("adventure", advParams);
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
 
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			dbresponse = editToServerDB(a.getId(), params);
			
			if (dbresponse.success) {
				cache.addAdventure(a);
				/*
				if (dbresponse.object instanceof Adventure) {
					// Add the Adventure object to the cache
					if (! cache.addAdventure((Adventure)(dbresponse.object)))
						dbresponse = new ReturnInfo(ReturnInfo.FAIL_NOCACHE);
				} else {
					// TODO: if NOT an instance of a Comment then what?
					dbresponse = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
				}
				*/
			} else {
				// TODO: if a failure then what?
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
				Log.d(TAG,"editAdventure: Failure");
			}
		} else {
			// Add the Tag object to the cache
			if (cache.addAdventure(a)) {
				dbresponse = new ReturnInfo();
				dbresponse.object = a;
			} else {
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_NONETWORK);
			}
			
			// add the AddTag request to the cached list of DB transactions
			try {
				params.put("id", a.getId());
			} catch (Exception e) {
				return dbresponse;
			}

			cache.cacheAction(NAME, WebAPIConstants.OP_EDIT_ADV, params);
		}
		Log.d(TAG, "Leaving editAdventure");
		return dbresponse;
	}
	
	private ReturnInfo editToServerDB(JSONObject params) {
		try {
			long advID = params.getLong("id");
			params.remove("id");
			return editToServerDB(advID, params);
		} catch (Exception e) {
			ReturnInfo response = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
			return response;
		}
	}
	
	// TODO: Need to update the POST to a PUT
	// TODO: Need to handle getting the ID key from the cached action
	public ReturnInfo editToServerDB(long id, JSONObject params) {
		ReturnInfo result;
		Log.d(TAG, "Entering editToServerDB");
		
		String url = String.format(WebAPIConstants.ACC_FORMAT_EDITADVENTURE, WebAPIConstants.BASE_URL_GTDB, id);

		try {
			 // Building Parameters
			JSONObject adv;
			if (!params.has("adventure")) {
				return new ReturnInfo(ReturnInfo.FAIL_GENERAL); 
			}
			adv = params.getJSONObject("adventure");
			
			if (jsonParser.restPutCall(url, adv)) {
				result = new ReturnInfo();
			} else {
				result = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
			}
		} catch (Exception ex) {
			Log.d(TAG, "editToServerDB: Exception occurred adding adventure, returning null.");
			result = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		
		Log.d(TAG, "Leaving editToServerDB");
		return result;
	}
	

	

	@Override
	protected boolean isAddOperation(String operation) {
		return operation.equals(WebAPIConstants.OP_ADD_ADV);
	}
	@Override
	protected boolean isDeleteOperation(String operation) {
		return operation.equals(WebAPIConstants.OP_DELETE_ADV);
	}

	/**
	 * Add an Adventure to the server database
	 * INSERT INTO adventures(OwnerID, Name, Description)
	 * if successful, returns:
	 * array('AdventureID'=>$last_id, 'OwnerID'=>$userID, 'Name'=>$name, 'Description'=>$desc);
	 * @param a adventure object to add to server
	 * @return JSONObject the json object of the adventure returned from the server
	 */
	@Override
	public ReturnInfo addToServerDB(JSONObject params) {
		ReturnInfo result;
		Log.d(TAG, "Entering addToServerDB");
		String name = "";
		String desc = "";
		long userID = 0;
		
		Log.d("addAdventureToServer", "JSON Response from REST: " );
		String url = String.format(WebAPIConstants.ACC_FORMAT_ADDADVENTURE, WebAPIConstants.BASE_URL_GTDB);

		try {
			JSONObject advParams;

			advParams = params;
			/*
			if (params.has("adventure")) {
				advParams = params.getJSONObject("adventure");
			} else {
				return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
			}
			*/
		
			JSONObject json = jsonParser.postToServer(url, advParams);
			
			Log.d(TAG, "JSON Response from PHP: " + json.toString());
			result = new ReturnInfo(json);
			Log.d(TAG, "addToServerDB: Result is " + result.success);
			if (result.success) {
				Date d = new Date();
	     		long id;
	     		// If the result has and ID then create and adventure to return
	     		if (json.has("id")) {
	     			id = json.getLong("id");
	     						
	     			Adventure a = new Adventure(id, userID, name, desc, d, d);
	     			result.object = a;
	     		} else {
	     			result.object = null;
	     		}
//				result.object = createAdventureFromJSON(json);
			}
		}
		catch (Exception ex)
		{
			Log.d(TAG, "addToServerDB: Exception occurred adding adventure, returning null.");
			result = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		
		Log.d(TAG, "Leaving addToServerDB");
		return result;
	}
	

	/**
	 * delete an adventure from the db
	 * @param id id of adventure
	 * @return boolean value indicating whether adventure was successfully removed
	 */
	@Override
	public ReturnInfo delete(long id) {
		ReturnInfo dbResponse;
		
		// Building Parameters
		JSONObject params = new JSONObject();
		try {
			params.put("id", id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	      
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
		    //make webservice call to remove tag from db
			dbResponse = deleteFromServerDB("adventures", params);
			if (dbResponse.success) {
				// Tell the cache to delete the Adventure record
			    cache.deleteAdventure(id);
			}
		} else {
			// Add the Tag object to the cache
		    cache.deleteAdventure(id);
			
			// add the DeleteTag request to the cached list of DB transactions
			cache.cacheAction(NAME, WebAPIConstants.OP_DELETE_ADV, params);

			dbResponse = new ReturnInfo();
		}
		return dbResponse;
	}
	
	@Override
	public ReturnInfo deleteFromServerDB(JSONObject params) {
		ReturnInfo result;
		
	    //make webservice call to remove tag from db
		result = super.deleteFromServerDB("adventure", params);
		Log.d(TAG, "deleteFromServerDB: Result is " + result.success);
		
	    return result;
	}
	
	public ArrayList<Adventure> getAllAdventures(int flags) {
		ArrayList<Adventure> adventures;

		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions() && ((flags & HandlerConstants.FLAG_SERVER) != 0)) {
			adventures = new ArrayList<Adventure>();

			adventures = getAllAdventuresFromServer();
			// loop through each JSON entry in the JSON array (tags encoded as JSON)
			for (int i = 0; i < adventures.size(); i++) {
				Adventure a = adventures.get(i);
					
				// Add the record to the cache
				cache.addAdventure(a);
			}
		} else {
			if ((flags & HandlerConstants.FLAG_CACHE) != 0) {
				// If the network was not up then lets check the cache for the records
				adventures = cache.getAllAdventures();
			} else {
				adventures = new ArrayList<Adventure>();
			}
		}
		return adventures;
	}

	public ArrayList<Adventure> getAdventuresOwnerOf(int flags) {
		ArrayList<Adventure> adventures;

		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions() && ((flags & HandlerConstants.FLAG_SERVER) != 0)) {
			adventures = new ArrayList<Adventure>();

			adventures = getAdventuresOwnerOfFromServer();
			// loop through each JSON entry in the JSON array (tags encoded as JSON)
			for (int i = 0; i < adventures.size(); i++) {
				Adventure a = adventures.get(i);
					
				// Add the record to the cache
				cache.addAdventure(a);
			}
		} else {
			if ((flags & HandlerConstants.FLAG_CACHE) != 0) {
				// If the network was not up then lets check the cache for the records
				adventures = cache.getAllUserAdventures(UserSession.CURRENTUSER_ID);
			} else {
				adventures = new ArrayList<Adventure>();
			}
		}
		return adventures;
	}

	public ArrayList<Adventure> getAdventuresMemberOf(int flags) {
		ArrayList<Adventure> adventures;

		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions() && ((flags & HandlerConstants.FLAG_SERVER) != 0)) {
			adventures = new ArrayList<Adventure>();

			adventures = getAdventuresMemberOfFromServer();
			// loop through each JSON entry in the JSON array (tags encoded as JSON)
			for (int i = 0; i < adventures.size(); i++) {
				Adventure a = adventures.get(i);
					
				// Add the record to the cache
				cache.addAdventure(a);
				cache.addAdventureMember(a.getId(), UserSession.CURRENTUSER_ID);
			}
		} else {
			if ((flags & HandlerConstants.FLAG_CACHE) != 0) {
				// If the network was not up then lets check the cache for the records
				adventures = cache.getAdventuresMemberOf();
			} else {
				adventures = new ArrayList<Adventure>();
			}
		}
		return adventures;
	}
	
	/**
	 * Return an array list with all the adventures for the given user ID 
	 * The array could come from the cache or from the Server database.
	 * @param id the adventure ID to use
	 * @param flags identify if the cache should be retrieved or the server record
	 * @return arraylist of adventures for given user id fetched from sever
	 */
	public ArrayList<Adventure> getAllAdventuresUserPartOf(long id, int flags) {
		ArrayList<Adventure> adventures;

		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions() && ((flags & HandlerConstants.FLAG_SERVER) != 0)) {
			adventures = new ArrayList<Adventure>();

			adventures = getAllAdventuresUserPartOfFromServer(id);
			// loop through each JSON entry in the JSON array (tags encoded as JSON)
			for (int i = 0; i < adventures.size(); i++) {
				Adventure a = adventures.get(i);
					
				// Add the record to the cache
				cache.addAdventure(a);
			}
		} else {
			if ((flags & HandlerConstants.FLAG_CACHE) != 0) {
				// If the network was not up then lets check the cache for the records
				adventures = cache.getAllUserAdventures(id);
			} else {
				adventures = new ArrayList<Adventure>();
			}
		}
		return adventures;
	}


	private ArrayList<Adventure> getAllAdventuresFromServer() {
		ArrayList<Adventure> adventures = new ArrayList<Adventure>();
		String url = WebAPIConstants.BASE_URL_GTDB+WebAPIConstants.ADV_ADVENTURE;

		int offset = 0;
		int limit = 50;
		int cntReturned = 0;
		do {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("offset", Integer.toString(offset)));
			params.add(new BasicNameValuePair("limit", Integer.toString(limit)));
			try {
				JSONObject resultsObject = jsonParser.getJSONObject(url, params);

				if (resultsObject != null) {
					JSONArray jArr = resultsObject.getJSONArray("adventures");
					cntReturned = jArr.length();
					offset += jArr.length();
					Log.e("JSON Parser Adventure", jArr.toString());
					
					JSONObject obj;
					// loop through each JSON entry in the JSON array (tags encoded as JSON)
					for (int i = 0; i < jArr.length(); i++) {
						obj = jArr.getJSONObject(i);

						if (obj != null) {
							Adventure a = createAdventureFromJSON(obj); 
							adventures.add(a);
						}
					}
				} else {
					cntReturned = 0;
					Log.d(TAG, "getAllAdventuresUserPartOff: No Results");
					return adventures;
				}
			} catch (Exception ex) {
				Log.d(TAG, "getAllAdventuresUserPartOff: Exception occurred getting adventures, returning null.");
				return adventures;
			}
		} while (cntReturned == limit);
		
		return adventures;
	}
	

	private ArrayList<Adventure> getAdventuresOwnerOfFromServer() {
		ArrayList<Adventure> adventures = new ArrayList<Adventure>();
		String url = String.format( WebAPIConstants.ACC_FORMAT_GETMYADVENTURES, WebAPIConstants.BASE_URL_GTDB);

		try {
			JSONObject resultsObject = jsonParser.getJSONObject(url);

			// If there are no results then return
			if (resultsObject == null) {
				Log.d(TAG, "getAdventuresOwnerOfFromServer: No Results");
				return adventures;
			}
			
			JSONObject advObject = resultsObject.getJSONObject("adventures");
			
			// Get array of the Adventures owned by this user
			JSONArray jArr = advObject.getJSONArray("owner_of");
			
			JSONObject obj;
			// loop through each JSON entry in the JSON array (tags encoded as JSON)
			for (int i = 0; i < jArr.length(); i++) {
				obj = jArr.getJSONObject(i);

				if (obj != null) {
					Adventure a = createAdventureFromJSON(obj); 
					adventures.add(a);
				}
			}
		} catch (Exception ex) {
			Log.d(TAG, "getAdventuresOwnerOfFromServer: Exception occurred getting adventures, returning null.");
			return adventures;
		}
		
		return adventures;
	}
	
	private ArrayList<Adventure> getAdventuresMemberOfFromServer() {
		ArrayList<Adventure> adventures = new ArrayList<Adventure>();
		String url = String.format( WebAPIConstants.ACC_FORMAT_GETADVENTURESMEMBEROF, WebAPIConstants.BASE_URL_GTDB);

		try {
			JSONObject resultsObject = jsonParser.getJSONObject(url);

			// If there are no results then return
			if (resultsObject == null) {
				Log.d(TAG, "getAdventuresOwnerOfFromServer: No Results");
				return adventures;
			}
			
			JSONObject advObject = resultsObject.getJSONObject("adventures");
			
			// Get array of the Adventures owned by this user
			JSONArray jArr = advObject.getJSONArray("member_of");
			
			JSONObject obj;
			// loop through each JSON entry in the JSON array (tags encoded as JSON)
			for (int i = 0; i < jArr.length(); i++) {
				obj = jArr.getJSONObject(i);

				if (obj != null) {
					Adventure a = createAdventureFromJSON(obj); 
					adventures.add(a);
				}
			}
		} catch (Exception ex) {
			Log.d(TAG, "getAdventuresOwnerOfFromServer: Exception occurred getting adventures, returning null.");
			return adventures;
		}
		
		return adventures;
	}

	
	/**
	 * Return a JSONArray (array of JSON Objects) containing
	 * all the adventures for the given user ID
	 * @param user id
	 * @return json array indicating all adventures for given user id
	 */
	private ArrayList<Adventure> getAllAdventuresUserPartOfFromServer(long id) {
		ArrayList<Adventure> adventures = new ArrayList<Adventure>();
		String url = WebAPIConstants.BASE_URL_GTDB+WebAPIConstants.ADV_ADVENTURE;

		int offset = 0;
		int limit = 50;
		int cntReturned = 0;
		do {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("offset", Integer.toString(offset)));
			params.add(new BasicNameValuePair("limit", Integer.toString(limit)));
			try {
				JSONObject resultsObject = jsonParser.getJSONObject(url, params);

//				JSONArray resultsArray = jsonParser.getJSONArrayForOadventuresREST(url, params);
				if (resultsObject != null) {
					JSONArray jArr = resultsObject.getJSONArray("adventures");
					cntReturned = jArr.length();
					offset += jArr.length();
					Log.e("JSON Parser Adventure", jArr.toString());
					
					JSONObject obj;
					// loop through each JSON entry in the JSON array (tags encoded as JSON)
					for (int i = 0; i < jArr.length(); i++) {
						obj = jArr.getJSONObject(i);

						if (obj != null) {
							Adventure a = createAdventureFromJSON(obj); 
							adventures.add(a);
						}
					}
				} else {
					cntReturned = 0;
					Log.d(TAG, "getAllAdventuresUserPartOff: No Results");
					return adventures;
				}
			} catch (Exception ex) {
				Log.d(TAG, "getAllAdventuresUserPartOff: Exception occurred getting adventures, returning null.");
				return adventures;
			}
		} while (cntReturned == limit);
		
		
		

		return adventures;
	}
	
	/**
	 * Creates an adventure from the json object returned from the server
	 * @param json
	 * @return returns the adventure created so that it can be cached locally
	 */
	private Adventure createAdventureFromJSON(JSONObject json)
	{
		Date d = new Date();
		Date m = new Date();
    	try {
     		SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
    		Log.d("CreateTagFromJson", "Inside the Method ");
			if (json.has("created_at"))
				d = ts.parse(json.getString("created_at"));	
			if (json.has("modified_at"))
				m = ts.parse(json.getString("modified_at"));
			
			Log.d("CreateTagFromJson", String.valueOf(json.getJSONObject("owner").getLong("id")));
			Adventure a = new Adventure(json.getLong("id"), 
					 json.getJSONObject("owner").getLong("id"),
					json.getString("name"), json.getString("description"), d,m);
			
			return a;			
		} catch (JSONException e) {
    		Log.d(TAG, "CreateTag from JSONObject failed");
			e.printStackTrace();
		} catch (ParseException e) {
			Log.d(TAG, "Problem parsing timestamp from JSON");
			e.printStackTrace();
		}
    	return null;
	}
	
	/**
	 * Adds an individual tag to the Adventure's list of tags.
	 * @param the tag id that identifies the tag to be added to the adventure
	 * @param the id of the adventure to which the tag should be added to
	 * @return a boolean value indication success or failure
	 */
	public boolean addTagToAdventure(long tagId, long advID)
	{
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			if (addTagToAdventureToServerDB(tagId, advID)) {
				cache.addAdventureTag(advID, tagId);
				return true;
			} else {
				return false;
			}
		} else {
			// Add the Tag object to the cache
			if (cache.addAdventureTag(advID, tagId)) {
				// There are no post operations for this record
				JSONObject params = new JSONObject();
				try {
					params.put("advID", advID);
					params.put("tagID", tagId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				cache.cacheAction(NAME, WebAPIConstants.OP_ADD_TAG2ADV, params);
			} else {
				return false;
			}
		}
		return true;
	}

	public ReturnInfo addTagToAdventureToServerDB(JSONObject params) {
		try {
			long advID = params.getLong("advID");
			long tagID = params.getLong("tagID");
			if (addTagToAdventureToServerDB(tagID, advID))
				return new ReturnInfo();
			else
				return new ReturnInfo(ReturnInfo.FAIL_GENERAL);
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
	}
	
	private boolean addTagToAdventureToServerDB(long tagID, long adventureID) {
		return jsonParser.sendRelationToServerDB(false, "addadventuretag", "adventure", tagID, adventureID);
	}

	static HashMap<Long,Long> adventureIDs = new HashMap<Long,Long>();

	/**
	 * Return an array list with all of the Tags for a given adventure ID.
	 * The array could come from the cache or from the Server database.
	 * @param id the adventure ID to use
	 * @return arraylist of tags of the given adventure
	 */
	public ArrayList<Tag> getAllAdventureTags(long id, boolean cacheOnly) {
		ArrayList<Tag> tags;

		synchronized (queryLock) {
			Long key = id;
			Long value = adventureIDs.get(key);
			
			// If the network is up then try to get the record from the Server DB
			// If the adventure has already been searched then use the cache
			if (!cacheOnly && value == null && NetworkUtils.isNetworkUp(context)) {
				tags = new ArrayList<Tag>();
	
				JSONArray tagData = getAllAdventureTagsFromServer(id);
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
							cache.addTag(t);
							cache.addAdventureTag(id, t.getId());
						}
					}
				}
				// Add this key to the list of already searched for entries
				value = key;
				adventureIDs.put(key, value);
			} else {
				// If the network was not up then lets check the cache for the records
				tags = cache.getAdventureTags(id);
			}
		}
		return tags;
	}

	/**
	 * Return a JSONArray (array of JSON Objects) containing
	 * all the tags for the given adventure ID.
	 * @return the json array of all tags for the given adventure
	 */	
	private JSONArray getAllAdventureTagsFromServer(long id) {	
		Log.d("getAllAdvenTagsFromServer AH:", String.valueOf(id));
		String url = WebAPIConstants.BASE_URL_GTDB+WebAPIConstants.ACC_USERADVENTURE+id+"/tags";

		try {
			//make webservice call to get tags
			JSONArray resultsArray = jsonParser.getJSONArrayForTAGS_REST(url);
			if(resultsArray != null){
				//Log.d("TagHandler GetTagsByID", "JSON Response from PHP: " + resultsArray.toString());
				//parse result into array of jsonobjects and return it
				return resultsArray;
			} else {
				Log.d(TAG, "getAllAdventureTagsFromServer: No Results");
				//parse result into array of jsonobjects and return it
				return null;
			}
		} catch (Exception ex) {
			Log.e(TAG, "Exception occurred getting tags, returning null.");
			return null;
		}
	}
	
	
	/**
	 * deletes an individual tag from the adventure
	 * @param tagId the tag id to be deleted from the adventure
	 * @param advID the id of the adventure from which the tag is to be deleted
	 * @return a boolean value indicating success or failure
	 */
	public boolean removeTagFromAdventure(long tagID, long adventureID) {
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
	        
			if (removeAdventureTagfromServerDB(tagID, adventureID)) {
				// Delete the record from the cache
				cache.deleteAdventureTag(adventureID, tagID);
			} else {
				return false;
			}
		} else {
			// Delete the record from the cache
			cache.deleteAdventureTag(adventureID, tagID);

			// Building Parameters
			JSONObject params = new JSONObject();
			try {
				params.put("advID", adventureID);
				params.put("tagID", tagID);

				// TODO: Add post action operations
	        
				// add the deleteTag request to the cached list of DB transactions
				cache.cacheAction(NAME, WebAPIConstants.OP_REMOVE_TAGFROMADV, params);
			} catch (Exception e) {
				return false;
			}

			// TODO: Add remove tag from adventure operation to cache
			return false;
		}
		return true;
	}

	public ReturnInfo removeAdventureTagfromServerDB(JSONObject params) {
		long advID;
		long tagId;
		try {
			advID = params.getLong("advID");
			tagId = params.getLong("tagId");
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		if ( removeAdventureTagfromServerDB(tagId, advID))
			return new ReturnInfo();
		else
			return new ReturnInfo(ReturnInfo.FAIL_GENERAL);
	}
	
	private boolean removeAdventureTagfromServerDB(long tagID, long adventureID) {
		return jsonParser.sendRelationToServerDB(true, "removeadventuretag", "adventure", tagID, adventureID);
	}


	
	/**
	 * Adds an individual user to the adventure.
	 * @param userID the user id to add to the adventure
	 * @param advID the adventure id that identifies the adventure the user is to be added to
	 * @return a boolean indicating success or failure
	 */
	public boolean addUserToAdventureById(long userID, long advID)
	{
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			if (addAdventureUserToServerDB(userID, advID)) {
				cache.addAdventureMember(advID, userID);
				return true;
			} else {
				return false;
			}
		} else {
			// Add the Adventure Member object to the cache
			if (cache.addAdventureMember(advID, userID)) {
			} else {
				return false;
			}
			
			// Building Parameters
			try {
				JSONObject params = new JSONObject();
		        params.put("advID", advID); 
		        params.put("userID", userID);
	
				// TODO: Create post commands for the Action cache record.
		        List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		        postParams.add(new BasicNameValuePair(CacheHandler.ACTION_OP_POSTID, CacheHandler.ACTION_UPDATE_POSTOP));
		        postParams.add(new BasicNameValuePair(CacheHandler.ACTION_ADVENTUREID_POSTID, Long.toString(advID)));
		        postParams.add(new BasicNameValuePair(CacheHandler.ACTION_ACCOUNTID_POSTID, Long.toString(userID)));
				
				// add the AddTag request to the cached list of DB transactions
				cache.cacheAction(null, WebAPIConstants.OP_ADD_PERSON, params);
			} catch (Exception e) {
			}

			return false;
		}
	}
	
	public ReturnInfo addAdventureUserToServerDB(JSONObject params) {
		try {
			long advID = params.getLong("advID");
			long userID = params.getLong("userID");
			if (addAdventureUserToServerDB(userID, advID))
				return new ReturnInfo();
			else
				return new ReturnInfo(ReturnInfo.FAIL_GENERAL);
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
	}

	
	private boolean addAdventureUserToServerDB(long userID, long adventureID) {
		return jsonParser.sendRelationToServerDB(false, "addadventuremember", "adventure", userID, adventureID);
	}

	
	/**
	 * returns all users associated with the adventure as an arrayList of user accounts.
	 * @param aId
	 * @return Arraylist of user accounts associated with adventure id
	 */

	public ArrayList<UserAccount> getPeopleInAdventure(long aId) {
		ArrayList<UserAccount> uaList;

		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			uaList = new ArrayList<UserAccount>();

			JSONArray uaData = getPeopleInAdventureFromServer(aId);
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
						
						// add/update the record in the cache
						cache.addAccount(u);
						cache.addAdventureMember(aId, u.getId());
					}
				}
			}
		} else {
			uaList = cache.getAdventureMembers(aId);
		}
		return uaList;
	}

	/**
	 * Return a JSONArray (array of JSON Objects) containing
	 * all the tags for the given adventure ID.
	 * @param adventure id to fetch users from
	 * @return Json array from server of representing users in adventure
	 */	
	private JSONArray getPeopleInAdventureFromServer(long aId) {
		String url = String.format( WebAPIConstants.ACC_FORMAT_GETADVENTUREMEMBERS, WebAPIConstants.BASE_URL_GTDB, aId);
		Log.i("AH getPeopleInAdventureFromServer", url.toString());
		
		try {
			JSONArray resultsArray = jsonParser.getJSONArrayForMembersUrlREST(url);
			if (resultsArray != null) {
				//Log.d("TagHandler GetTagsByID", "JSON Response from PHP: " + resultsArray.toString());
				//parse result into array of jsonobjects and return it
				return resultsArray;
			} else {
				Log.d(TAG, "getPeopleInAdventureFromServer: No Results");
				//parse result into array of jsonobjects and return it
				return null;
			}
			
		} catch (Exception ex) {
			Log.e(TAG, "Exception occurred getting tags, returning null.");
			return null;
		}
	}	
	
	/**
	 * delete a person from the adventure
	 * @param userID user id to remove from adventure
	 * @param advID adventure id of person to remove from server
	 * @return boolean value indicating success or failure
	 */
	public boolean removeUserFromAdventure(long userID, long adventureID)
	{
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			if (removeAdventureUserfromServerDB(userID, adventureID)) {
				// remove the user from the adventure in the cache
				cache.deleteAdventureMember(adventureID, userID);
			} else {
				return false;
			}
		} else {
			// remove the user from the adventure in the cache
			cache.deleteAdventureMember(adventureID, userID);
			
			JSONObject params = new JSONObject();
			try {
				params.put("advID", adventureID);
				params.put("userID", userID);
			} catch (Exception e) {
				return false;
			}
	        
			cache.cacheAction(NAME, WebAPIConstants.OP_REMOVE_PERSONFROMADV, params);
			
			return false;
		}
		return true;
	}
	
	public ReturnInfo removeAdventureUserfromServerDB(JSONObject params) {
		long advID;
		long userID;
		try {
			advID = params.getLong("advID");
			userID = params.getLong("userID");
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		if ( removeAdventureUserfromServerDB(userID, advID))
			return new ReturnInfo();
		else
			return new ReturnInfo(ReturnInfo.FAIL_GENERAL);
	}

	
	private boolean removeAdventureUserfromServerDB(long userID, long adventureID) {
		return jsonParser.sendRelationToServerDB(true, "removeadventuremember", "adventure", userID, adventureID);
	}
}