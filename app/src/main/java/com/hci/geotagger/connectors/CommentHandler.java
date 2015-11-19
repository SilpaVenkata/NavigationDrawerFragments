package com.hci.geotagger.connectors;

import java.io.File;
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
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.objects.Comment;

/**
 * This class contains the Tag Comment handler functions
 * 
 * @author paulcushman
 *
 */
public class CommentHandler extends GeotaggerHandler {
	private static String TAG = "CommentHandler";
	public static final String NAME = "CommentHandler";
	protected String [] mActionsSupported = {
			WebAPIConstants.OP_ADD_TAGCOMMENT		// Add a Tag Comment
	};

	
	/**
	 * Constructor for the TagHandler class.  The TagHandler class requires a context to make
	 * subsequent calls for caching.
	 * @param context current context
	 */
	public CommentHandler(Context context) {
		super(context, WebAPIConstants.TAGOP_URL);
		setActionList(mActionsSupported);
    }

	/**
	 * Overridden method to process the actions supported by this handler. This function
	 * is called during the handling of cached actions.
	 */
	@Override
	public ReturnInfo performServerDbOperation(String operation, JSONObject params) {
		if (operation.equals(WebAPIConstants.OP_ADD_TAGCOMMENT))
			return addToServerDB(params);

		return new ReturnInfo(ReturnInfo.FAIL_BADACTION);
	}


	/**
	 * The Tag Handler supports the server database Add Operation.
	 * This is a GeotaggerHandler method that identifies if the input operation is a
	 * Tag Handler Add operation.
	 */
	@Override
	protected boolean isAddOperation(String operation) {
		return operation.equals(WebAPIConstants.OP_ADD_TAGCOMMENT);
	}


	/**
	 * Add a Tag Comment to the database with a picture.  A ReturnInfo object is returned
	 * with result information and the Comment object.
	 * @param tagID ID of the Tag to add the comment to
	 * @param text Text of the comment to add
	 * @param username User name of the person adding the comment
	 * @param imgURL URL of the image to be added to the comment
	 * @return a ReturnInfo object with operation status and the new Comment object
	 */
	public ReturnInfo addTagComment(Comment comment) {
		String imageData;
		
		ReturnInfo dbresponse;
		Log.d(TAG, "Entering addTagComment");
		
		// Handle the Comment image
		long imageID = comment.getImageId();

		// Building Parameters
		JSONObject params = new JSONObject();
		JSONObject cmntParams = new JSONObject();
		try {
			cmntParams.put("tag", comment.getTagId());
			cmntParams.put("body", comment.getText());
			if (imageID > 0)
				cmntParams.put("documentId", imageID);
			params.put("comment", cmntParams);
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
        
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			dbresponse = addToServerDB(params);
			if (dbresponse.success) {
				if (dbresponse.object instanceof Comment) {
					if (! cache.addTagComment((Comment)(dbresponse.object))) {
						Log.d(TAG, "addTagComment: cache error");
//						return new ReturnInfo(ReturnInfo.FAIL_NOCACHE);
					}
				} else {
					// TODO: if NOT an instance of a Comment then what?
					dbresponse = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
				}
			} else {
				// TODO: If the add failed then what?
			}
		} else {
			/*
			 *  Create Tag Comment object based on what we know.  Since the record cannot
			 *  be added to the database we will generate a temporary CommentId value.
			 *  TODO: Convert from cached CommentID to the database's CommentID
			 */
			Date d = new Date();
			long cacheID = cache.getnextTagCommentCacheID();
			
			comment.setId(cacheID);
			comment.setCreatedDateTime(d);
			
			// Add the record to the cache
			if (cache.addTagComment(comment)) {
				dbresponse = new ReturnInfo();
				dbresponse.object = comment;
			} else {
				dbresponse = new ReturnInfo(ReturnInfo.FAIL_NONETWORK);
			}
			
			// TODO: Create post commands for the Action cache record.
	        List<CachePostAction> postParams = new ArrayList<CachePostAction>();
	        postParams.add(new CachePostAction(CacheHandler.ACTION_UPDATE_POSTOP, CacheHandler.ACTION_COMMENTID_POSTID, cacheID));
			
			// add the AddTag request to the cached list of DB transactions
			cache.cacheAction(NAME, WebAPIConstants.OP_ADD_TAGCOMMENT, params, postParams);
		}

		Log.d(TAG, "Leaving addTagComment");
		return dbresponse;
	}

	/**
	 * Internal method to add a Tag Comment to the server.  This method makes the specific JSON calls
	 * to add the record to the database.
	 * @param params
	 * @return
	 */
	@Override
	public ReturnInfo addToServerDB(JSONObject params) {
		ReturnInfo result = null;
		Log.d(TAG, "Entering addToServerDB");

		String url = String.format(WebAPIConstants.ACC_FORMAT_ADDCOMMENT, WebAPIConstants.BASE_URL_GTDB);

        //make webservice call to add tag comment to db
		try {
			JSONObject json = jsonParser.postToServer(url, params);
			
			Log.d(TAG, "addToServerDB: JSON Response from PHP: " + json.toString());
			result = new ReturnInfo(json);
			if (result.success) {
				result.object = createCommentFromJSON(json);
			}
		} catch (Exception ex) {
			Log.d(TAG, "addToServerDB: Exception occurred adding comment, returning null.");
			result = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		
		Log.d(TAG, "Leaving addToServerDB");
		return result;
	}
	
	
	/**
	 * This method generates a Comment object from the input JSON object.
	 * @param json input JSONObject object
	 * @return Comment object is returned
	 */
	private Comment createCommentFromJSON(List<NameValuePair> params, JSONObject json)
	{
		Comment c = null;
		Log.d(TAG, "Entering createCommentFromJSON");

		
        long tagId = 0;
        String comment = null;
        String uName = null;
        long cID = 0;
        String imageUrl = null;
        
        for (int i=0; i<params.size(); i++) {
        	BasicNameValuePair bnvp = (BasicNameValuePair)params.get(i);
        	if (bnvp.getName().equals("tagId")) {
        		tagId = Long.valueOf(bnvp.getValue()).longValue();
        	} else if (bnvp.getName().equals("comment")) {
        		comment = bnvp.getValue();
        	} else if (bnvp.getName().equals("uName")) {
        		uName = bnvp.getValue();
        	} else if (bnvp.getName().equals("imgUrl")) {
        		imageUrl = bnvp.getValue();
        	}
        }
        
		Date d = new Date(System.currentTimeMillis());

    	try 
    	{
    		if (json.has("ImageUrl"))
    			imageUrl = json.getString("ImageUrl");
    		
    		if (json.has("cID"))
    			cID = json.getLong("cID");
    		else
    			return null;
    		
    	} catch (JSONException e) {
       		Log.d("TagHandler", "CreateComment from JSONObject failed");
    		e.printStackTrace();
    	}

    	//below code keeps tags working until server has imageURL field for comments
    	if (imageUrl != null && imageUrl.length() > 0) {
    		c = new Comment(cID,tagId, comment, uName, d, imageUrl);
		} else {
    		c = new Comment(cID,tagId, comment, uName, d);
		} 
    	
		Log.d(TAG, "Leaving createCommentFromJSON");
    	return c;
	}


	
	
	/**
	 * This method generates a Comment object from the input JSON object.
	 * @param json input JSONObject object
	 * @return Comment object is returned
	 */
	private Comment createCommentFromJSON(JSONObject json) {
		Comment c = null;
		Log.d(TAG, "Entering createCommentFromJSON");

		Date d = new Date();
    	try {
    		long tagID = 0;
    		long commentID = 0;
    		String commentBody;
    		String userName = "";
    		String imageUrl = "";
    		
    		// Get the Tag ID
    		if (json.has("parent_tag")) {
        		JSONObject parentTag;

        		parentTag = json.getJSONObject("parent_tag");
        		if (parentTag.has("id"))
        			tagID = parentTag.getLong("id");
    		}
    		
    		// Get the username
    		if (json.has("owner")) {
    			JSONObject owner;
    			owner = json.getJSONObject("owner");
        		if (owner.has("username"))
        			userName = owner.getString("username");
    		}
    		
    		// Get the comment ID
    		if (json.has("id")) {
    			commentID = json.getLong("id");
    		} else {
    			return null;
    		}
    		
    		// Get comment body
    		if (json.has("body")) {
    			commentBody = json.getString("body");
    		} else {
    			commentBody = "";
    		}
    		
    		//format the date
    		SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
			d = ts.parse(json.getString("created_at"));

			if (json.has("image_url")) {
				imageUrl = json.getString("image_url");
			}
			//below code keeps tags working until server has imageURL field for comments
			if (imageUrl.length() > 0) {
				c = new Comment(commentID, tagID, commentBody, userName, d, imageUrl);
			} else {
				c = new Comment(commentID, tagID, commentBody, userName,  d);
			}
		} catch (Exception e) {
    		Log.d(TAG, "CreateComment from JSONObject failed");
			e.printStackTrace();
		}
    	
		Log.d(TAG, "Leaving createCommentFromJSON");
    	return c;
	}

	/**
	 * This method returns an ArrayList of Comment objects, that are associated with the
	 * input tagID value.
	 * @param tagID the Tag ID to use to retrieve the Comment records
	 * @return The ArrayList containing the Comment objects
	 */
	public ArrayList<Comment> getTagComments(long tagID) {
		ArrayList<Comment> comments = new ArrayList<Comment>();
		Log.d(TAG, "Entering getTagComments");

		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			JSONArray commentData = getTagCommentsFromServer(tagID);
			JSONObject obj;
			if (commentData != null) {
				// loop through each JSON entry in the JSON array (tags encoded as JSON)
				for (int i = 0; i < commentData.length(); i++) {
					obj = null;
					try {
						obj = commentData.getJSONObject(i);
					} catch (JSONException e) {
						Log.d(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
	
					if (obj != null) {
						Comment c = createCommentFromJSON(obj);
						comments.add(c);
						
						// add/update the record in the cache
						cache.addTagComment(c);
					}
				}
			}
		} else {
			// If the network was not up then lets check the cache for the records
			comments = cache.getTagComments(tagID);
		}

		Log.d(TAG, "Leaving getTagComments");
		return comments;
	}
	
	/**
	 * This method will retrieve all comments related to a specific tag from the server.
	 * The returned value is a JSONArray of Comment records.
	 * @param tagID The Tag ID used to retrieve the Comments
	 * @return The JSONArray object that contains the retrieved Comments
	 */
	
	private JSONArray getTagCommentsFromServer(long tagID) {
		Log.d(TAG, "Entering getTagCommentsFromServer");
		String url = String.format(WebAPIConstants.ACC_FORMAT_GETTAGCOMMENTS, WebAPIConstants.BASE_URL_GTDB, tagID);

		try {
			JSONObject result = jsonParser.getJSONObject(url);
			
	        try {
	        	JSONArray jArr;
	        	jArr = result.getJSONArray("comments");
	        	return jArr;
	        } catch (JSONException e) {
	            Log.e("JSON Parser", "Error parsing data " + e.toString());
	        }

		} catch (Exception ex) {
			Log.e(TAG, "Exception occurred getting tags, returning null.");
		}
		return null;
	}

}
