/*
 * Tag handler class is responsible for making calls to the webservice 
 * for tag operations, such as adding and retrieving tags.
 * 
 * Chris Loeschorn
 * Spring 2013
 */
package com.hci.geotagger.connectors;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.hci.geotagger.objects.GeoLocation;
import com.hci.geotagger.objects.Tag;

public class TagHandler extends GeotaggerHandler {

	private static String TAG = "TagHandler";
	public static final String NAME = "TagHandler";
	protected String [] mActionsSupported = {
			WebAPIConstants.OP_ADD_TAG,
			WebAPIConstants.OP_DELETE_TAG,
			WebAPIConstants.OP_EDIT_TAG
	};
	
	/**
	 * Constructor for the TagHandler class.  The TagHandler class requires a context to make
	 * subsequent calls for caching.
	 * @param context current context
	 */
	public TagHandler(Context context) {
		super(context, WebAPIConstants.TAGOP_URL);
		setActionList(mActionsSupported);
    }
	
	/**
	 * Overridden method to process the actions supported by this handler. This function
	 * is called during the handling of cached actions.
	 */
	@Override
	public ReturnInfo performServerDbOperation(String operation, JSONObject params) {
		if (operation.equals(WebAPIConstants.OP_ADD_TAG))
			return addToServerDB(params);
		if (operation.equals(WebAPIConstants.OP_DELETE_TAG))
			return deleteFromServerDB("tags", params);
		if (operation.equals(WebAPIConstants.OP_EDIT_TAG))
			return editToServerDB(params);

		return new ReturnInfo(ReturnInfo.FAIL_BADACTION);
	}


	/**
	 * The Tag Handler supports the server database Add Operation.
	 * This is a GeotaggerHandler method that identifies if the input operation is a
	 * Tag Handler Add operation.
	 */
	@Override
	protected boolean isAddOperation(String operation) {
		return operation.equals(WebAPIConstants.OP_ADD_TAG);
	}

	/**
	 * The Tag Handler supports the server database Delete Operation.
	 * This is a GeotaggerHandler method that identifies if the input operation is a
	 * Tag Handler Delete operation.
	 */
	@Override
	protected boolean isDeleteOperation(String operation) {
		return operation.equals(WebAPIConstants.OP_DELETE_TAG);
	}
	
	/**
	 * This method will add a Tag to the database.  The input Tag object contains the
	 * necessary fields to be added to the database.
	 * @param t this is the Tag object to add to the database
	 * @return Returns a ReturnInfo object which identifies the success of the Add operation
	 */
	public ReturnInfo AddTag(Tag t)
	{
		ReturnInfo dbresponse;
		Log.d(TAG, "Entering AddTag");
		
		// Building Parameters
		JSONObject params = new JSONObject();
		JSONObject tagParams = new JSONObject();
		
		try {
			tagParams.put("name", t.getName());
			tagParams.put("description", t.getDescription());
//			tagParams.put("image", t.getImageData());
			if (t.getImageId() > 0)
				tagParams.put("documentId", t.getImageId());
			tagParams.put("location", t.getLocationString());
        
			String lat, lon;
			if (t.getLocation() != null) {
				lat = Double.toString(t.getLocation().getLatitude());
				lon = Double.toString(t.getLocation().getLongitude());
			} else {
				lat = String.valueOf(0);
				lon = String.valueOf(0);
			}
			tagParams.put("latitude", lat);
			tagParams.put("longitude", lon);
        
			params.put("tag", tagParams);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
 
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			dbresponse = addToServerDB(params);
			if (dbresponse.success) {
				if (dbresponse.object instanceof Tag) {
					// Add the Tag object to the cache
					if (! cache.addTag((Tag)(dbresponse.object)))
						dbresponse = new ReturnInfo(ReturnInfo.FAIL_NOCACHE);
				} else {
					// TODO: if NOT an instance of a Comment then what?
					dbresponse = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
				}
			} else {
				// TODO: if a failure then what?
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
				Log.d(TAG,"addTag: Failure");
			}
		} else {
			// Since record was not added we need a Tag ID, so we use the next Cache Tag ID
			t.setId(cache.getnextTagCacheID());
			
			// Add the Tag object to the cache
			if (cache.addTag(t)) {
				dbresponse = new ReturnInfo();
				dbresponse.object = t;
			} else {
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_NONETWORK);
			}
			
			// TODO: Create post commands for the Action cache record.
	        List<CachePostAction> postParams = new ArrayList<CachePostAction>();
	        postParams.add(new CachePostAction(CacheHandler.ACTION_UPDATE_POSTOP, CacheHandler.ACTION_TAGID_POSTID, t.getId()));
	        postParams.add(new CachePostAction(CacheHandler.ACTION_UPDATE_POSTOP, CacheHandler.ACTION_TAGID2_POSTID, t.getId()));
			
			// add the AddTag request to the cached list of DB transactions
			cache.cacheAction(NAME, WebAPIConstants.OP_ADD_TAG, params, postParams);
		}
		Log.d(TAG, "Leaving AddTag");
		return dbresponse;
	}
	
	@Override
	public ReturnInfo addToServerDB(JSONObject params) {
		ReturnInfo result;
		
		String url = String.format(WebAPIConstants.ACC_FORMAT_ADDTAG, WebAPIConstants.BASE_URL_GTDB);
		Log.e("Inside TA", "Inside addJSON_TagToServer" + url);
		
		try {
			JSONObject json = jsonParser.postToServer(url, params);
			Log.d(TAG, "addTagToServer: JSON Response from PHP: " + json.toString());
			result = new ReturnInfo(json);
			Log.d("AddTagToServer", "Result is " + result.success);
			result.object = createTagFromJSON(json);
		} catch (Exception ex) {
			Log.d(TAG, "addTagToServer: Exception occurred adding Tag, returning null.");
			result = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		
		Log.d(TAG, "Leaving addTagToServer");
		return result;
	}

	/**
	 * This method will update a Tag in the database.  The input Tag object 
	 * contains the necessary fields to be modified in the database.
	 * @param t this is the Tag object to modify in the database
	 * @return Returns a ReturnInfo object which identifies the success of 
	 * the Update operation
	 */
	public ReturnInfo editTag(Tag t) {
		ReturnInfo dbresponse;
		Log.d(TAG, "Entering editTag");
		
		// Building Parameters
		JSONObject params = new JSONObject();
		JSONObject tagParams = new JSONObject();
		
		try {
			tagParams.put("name", t.getName());
			tagParams.put("description", t.getDescription());
//			tagParams.put("image", t.getImageData());
			tagParams.put("location", t.getLocationString());
        
			String lat, lon;
			if (t.getLocation() != null) {
				lat = Double.toString(t.getLocation().getLatitude());
				lon = Double.toString(t.getLocation().getLongitude());
			} else {
				lat = String.valueOf(0);
				lon = String.valueOf(0);
			}
			tagParams.put("latitude", lat);
			tagParams.put("longitude", lon);
        
			params.put("tag", tagParams);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
 
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			dbresponse = editToServerDB(t.getId(), params);
			if (dbresponse.success) {
				cache.editTag(t);
			} else {
				// TODO: if a failure then what?
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
				Log.d(TAG,"editTag: Failure");
			}
		} else {
			// Edit the Tag object to the cache
			if (cache.editTag(t)) {
				dbresponse = new ReturnInfo();
				dbresponse.object = t;
			} else {
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_NONETWORK);
			}
			
			try {
				// ID Needed for the call to edit the tag later
				params.put("id", t.getId());
				// add the AddTag request to the cached list of DB transactions
				cache.cacheAction(NAME, WebAPIConstants.OP_EDIT_TAG, params);
			} catch (Exception e) {
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
			}
		}
		Log.d(TAG, "Leaving editTag");
		return dbresponse;
	}
	
	private ReturnInfo editToServerDB(JSONObject params) {
		try {
			long tagID = params.getLong("id");
			params.remove("id");
			return editToServerDB(tagID, params);
		} catch (Exception e) {
			ReturnInfo response = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
			return response;
		}
	}

	public ReturnInfo editToServerDB(long tagID, JSONObject params) {
		ReturnInfo result;
		
		String url = String.format(WebAPIConstants.ACC_FORMAT_EDITTAG, WebAPIConstants.BASE_URL_GTDB, tagID);
		Log.d(TAG, "Inside addJSON_TagToServer" + url);
		
		try {
			if (jsonParser.restPutCall(url, params)) {
				result = new ReturnInfo();
			} else {
				result = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
			}
		} catch (Exception ex) {
			Log.e(TAG, "editToServerDB: Exception occurred adding Tag, returning null.");
			result = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		
		Log.d(TAG, "Leaving editToServerDB");
		return result;
	}


	/**
	 * This method returns a Tag object that is associated with the input tag ID.
	 * NOTE: This currently is only supported by the local cache. A function is needed
	 * on the server to support this.
	 * @param tagID
	 * @return
	 */
	public Tag getTag(long tagID) {
		Tag tag = null;
		
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			String url = String.format(WebAPIConstants.ACC_FORMAT_GETTAG, WebAPIConstants.BASE_URL_GTDB, tagID);

	        try {
	        	//make webservice call to get tags
	        	JSONObject jObject = jsonParser.getJSONObject(url);
	        	if (jObject != null) {
	        		if (jObject.has("tag")) {
	        			JSONObject tagJSON = null;
	        			tagJSON = jObject.getJSONObject("tag");
	        			
	        			tag = createTagFromJSON(tagJSON);
	        			
	        			cache.addTag(tag);
	        		}
	        	}
			} catch (Exception ex) {
				Log.d("GetTagsById", "Exception occurred getting tags, returning null.");
			}
		} else {
			tag = cache.getTag(tagID);
		}
		return tag;
	}

	/**
	 * Return all of the Tags associated with a specific Owner ID. The Tag objects returned
	 * are contained in the returned ArrayList.
	 * @param oId The Owner ID to use for the request.
	 * @return The ArrayList containing the associated Tags is returned
	 */
	public ArrayList<Tag> getTagsById(long oId) {
		ArrayList<Tag> tags;
		Log.d(TAG, "Entering getTagsById");

		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			tags = new ArrayList<Tag>();
			
			JSONArray tagData = getTagsByIdFromServer(oId);
			JSONObject obj;
			if (tagData != null) { //tagId
				// loop through each JSON entry in the JSON array (tags encoded as JSON)
				for (int i = 0; i < tagData.length(); i++) {
					obj = null;
					try {
						obj = tagData.getJSONObject(i);
					} catch (JSONException e) {
						Log.d(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
	
					if (obj != null) {
						Tag t = createTagFromJSON(obj);
						tags.add(t);
						
						// add/update the record in the cache
						cache.addTag(t);
					}
				}
			}
		} else {
			// If the network was not up then lets check the cache for the records
			tags = cache.getTags(oId);
		}
		
		Log.d(TAG, "Leaving getTagsById");
		return tags;
	}

	/**
	 * Static function to create a Tag object from a JSON Object.  All JSON functions
	 * are meant to be contained within the connectors are of the project.
	 * @param json The JSONObject to convert to a Tag object
	 * @return a Tag object is returned
	 */
	public static Tag createTagFromJSON(JSONObject json) {
		Tag t = null;
		JSONObject owner;
		String ownerID;
		String imageURL;
		String location;
		int rating;
		int oId;
		Log.d(TAG, "Entering createTagFromJSON");
		Date d = new Date();
    	try {
    		if (!json.has("rating")) {
    			rating = 0;
    		} else {
    			rating = json.getInt("rating");
    		}
    		
    		if (!json.has("location")) {
    			location = null;
    		} else {
    			location = json.getString("location");
    		}
    		SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
    		if (!json.has("created_at")) {
    			d = null;
    			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    			Date date = new Date();
    			d = date;
    		} else {
    			d = ts.parse(json.getString("created_at"));
    		}
 
			double lat= 0; 
			double lon = 0;
			if (json.has("latitude") && json.has("longitude")) {
				if (!json.getString("latitude").equalsIgnoreCase("null") && ! json.getString("longitude").equalsIgnoreCase("null")) {
					lat = json.getDouble("latitude");
					lon = json.getDouble("longitude");
				}
			} else {
				Log.d(TAG, "Does not have ##Longi and Lati##");
				lat = 0;
				lon = 0;
			}
			
			if (!json.has("image_url")) {
				imageURL = "/res/drawable/tagimage.jpg";
			} else {
				imageURL = json.getString("image_url");
			}
			
			if (json.has("owner")) {
				owner = json.getJSONObject("owner");
				ownerID = owner.getString("id");
				oId = Integer.parseInt(ownerID);
			} else if (json.has("TagID")) {
				ownerID = json.getString("TagID");
				oId = Integer.parseInt(ownerID);
			} else {
				oId = 0;
			}
			int vis = 1;
			
			GeoLocation geo = new GeoLocation(lat, lon);
//			commenting out because don't really need to pass username but hopefully doesn't break everything
//			//instantiate the tag object with properties from JSON
			t = new Tag(json.getLong("id"), json.getString("name"), json.getString("description"), imageURL,
					location,  rating, oId, geo, d,vis);
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
	 * Private method to query the server for the Tags associated Owner ID.  This method returns
	 * a JSONArray (array of JSON Objects) containing all the tags for the given user ID
	 * @param oId The Owner ID to use for the query
	 * @return A JSONArray with the results of the query
	 */
	private JSONArray getTagsByIdFromServer(long oId) {
		JSONArray resultsArray = null;
		Log.d(TAG, "Entering getTagsByIdFromServer");
		String url = WebAPIConstants.BASE_URL_GTDB + WebAPIConstants.ACC_USERS + oId + "/tags";

		int offset = 0;
		int limit = 100;

        try {
        	//make webservice call to get tags
        	JSONObject jObject = jsonParser.getJSONObject(url);
        	if (jObject != null) {
        		if (jObject.has("tags")) {
        			resultsArray = jObject.getJSONArray("tags");
        		}
        	}
		} catch (Exception ex) {
			Log.d("GetTagsById", "Exception occurred getting tags, returning null.");
		}

		Log.d(TAG, "Leaving getTagsByIdFromServer");
		return resultsArray;
	}

	/**
	 * This method will delete a tag from the database
	 * @param tagId ID of the tag to remove
	 * @return ReturnInfo object returned which identifies operation success
	 */
	@Override
	public ReturnInfo delete(long id) {
		ReturnInfo dbResponse;
		Log.d(TAG, "Entering deleteTag");

		// Building Parameters
		JSONObject params = new JSONObject();
		
		try {
			params.put("id", id);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}

		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			dbResponse = deleteFromServerDB("tags", params);

			if (dbResponse.success) {
				// Delete the record from the cache
				cache.deleteTag(id);
			}
		} else {
			// If the network is down are we going to handle delete operations?
			cache.deleteTag(id);
			
			// add the deleteTag request to the cached list of DB transactions
			cache.cacheAction(NAME, WebAPIConstants.OP_DELETE_TAG, params);
			
			// TODO: SHould we return more information??
			dbResponse = new ReturnInfo();
		}
		
		Log.d(TAG, "Leaving deleteTag");
		return dbResponse;
	}
	
}
