package com.hci.geotagger.connectors;

import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.cache.CacheHandler;
import com.hci.geotagger.common.Constants;

/**
 * This class is the base class for all Geotagger Connector Handlers. This
 * class will provide base methods that can be used to access all of the
 * Handlers.
 * 
 * When creating new handlers the following steps should be taken to update
 * this base class:
 * 1. add an entry for the new handler into the newHandler() method
 * 2. create a unique NAME value for the new handler, used to identify the 
 *    specific handler
 * 3. identify which server DB operations are supported by the handler
 * 4. override the specific server DB operation method with handler specific
 *    code or call the baseOperationToServerDB() base class method.
 * 
 * @author Paul Cushman
 *
 */
public class GeotaggerHandler {

	private static String TAG = "GeotaggerHandler";
	protected JSONParser jsonParser;
	protected Context context;
	protected CacheHandler cache;
	protected String mOperationURL;
	
	/**
	 * Constructor for the GeotaggerHandler base class.
	 * @param context current context
	 */
	public GeotaggerHandler(Context context, String URL) {
        jsonParser = new JSONParser();
        this.context = context;
        cache = new CacheHandler(context);
        this.mOperationURL = URL;
    }
	
	/**
	 * This method will register the input actions, associated with this handler,
	 * with the GeotaggerApplication instance.
	 * @param actions this is the list of actions supported by this handler
	 */
	protected void setActionList(String [] actions) {
        if (actions != null && actions.length > 0) {
	        if (context instanceof Activity) {
	        	Activity activity = (Activity)context;
	        	GeotaggerApplication app = (GeotaggerApplication)activity.getApplication();
	        	if (app != null) {
	        		for (String action : actions) {
	        			Log.d(TAG, "Adding action handler"+action);
	        			app.addHandlerAction(this, action);
	        		}
	        	}
	        }
        }
	}

	/**
	 * This method will return an instance of a Geotagger Handler that is associated
	 * with the input actionName. The input actionName MUST uniquely identify the
	 * handler.
	 * @param actionName
	 * @param context
	 * @return
	 */
	public static GeotaggerHandler newHandler(String actionName, Context context) {
		if (actionName == null)
			return null;
		
		if (actionName.equals(AdventureHandler.NAME))
			return new AdventureHandler(context);
		else if (actionName.equals(TagHandler.NAME))
			return new TagHandler(context);
		else if (actionName.equals(CommentHandler.NAME))
			return new CommentHandler(context);
		else if (actionName.equals(ImageHandler.NAME))
			return new ImageHandler(context);
		else if (actionName.equals(GroupHandler.NAME))
			return new GroupHandler(context);
		return null;
	}
	
	/**
	 * This is a private method which will get the "operation" name-value-pair from 
	 * the input params variable and return the specific operation value.
	 * @param params The list of name-value-pair parameters for an action to be performed
	 * @return The operation in the input params, or null if not found
	 */
	private String getOperation(JSONObject params) {
		String retString = null;
		if (params.has("operation")) {
			try {
				retString = params.getString("operation");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retString;
	}
	
	/**
	 * For record types that support the higher level delete action this should be 
	 * overridden.
	 * @param id this is the ID of the reocrd to delete
	 * @return return true if the record was delted successfully, false if not
	 */
	public ReturnInfo delete(long id) {
		return null;
	}
	
	/**
	 * This method will perform the handler database operation associated with the 
	 * input params.
	 * @param params These are the params associated with the action to perform
	 * @return The ReturnInfo object that identifies the result is returned.
	 */
	public ReturnInfo performServerDbOperation(JSONObject params) {
		String operation = getOperation(params);
		return performServerDbOperation(operation, params);
	}
	
	
	public ReturnInfo performServerDbOperation(String operation, JSONObject params) {
		
		if (isAddOperation(operation))
			return addToServerDB(params);
		if (isUpdateOperation(operation))
			return updateToServerDB(params);
		if (isDeleteOperation(operation))
			return deleteFromServerDB(params);
		return null;
	}
	
	/*
	 * These methods are used to identify if there are specific operations associated with
	 * the handler. Each handler should override any of these methods which are associated
	 * with handler operations that are suppored by the handler.
	 */
	protected boolean isAddOperation(String operation) {
		return false;
	}
	protected boolean isUpdateOperation(String operation) {
		return false;
	}
	protected boolean isDeleteOperation(String operation) {
		return false;
	}

	protected ReturnInfo baseOperationToServerDB(JSONObject params) {
		ReturnInfo result = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
	    return result;
	}
	
	/**
	 * Internal method to add a Record to the server.  This method makes the specific JSON calls
	 * to add the record to the database.
	 * @param params
	 * @return
	 */
	public ReturnInfo addToServerDB(JSONObject params) {
		return baseOperationToServerDB(params);
	}

	/**
	 * Internal method to add a Record to the server.  This method makes the specific JSON calls
	 * to add the record to the database.
	 * @param params
	 * @return
	 */
	public ReturnInfo updateToServerDB(JSONObject params) {
		return baseOperationToServerDB(params);
	}

	protected ReturnInfo deleteFromServerDB(JSONObject params) {
		return new ReturnInfo(ReturnInfo.FAIL_GENERAL);
	}
	
	protected ReturnInfo deleteFromServerDB(String objectType, JSONObject params) {
		ReturnInfo retValue;
		long id;
		try {
			if (params.has("id")) {
				id = params.getLong("id");
				JSONParser jsonParser = new JSONParser();
				String url = String.format(WebAPIConstants.ACC_FORMAT_DELETEOBJECT, WebAPIConstants.BASE_URL_GTDB, objectType, id);
		    	
				retValue = jsonParser.deleteRequest(url);
			} else {
				retValue = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			retValue = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		return retValue;
	}
	
	public String getURL() {
		return mOperationURL;
	}
	
	public void printCache() {
		if (cache != null)
			cache.printCacheDatabase();
	}
	
	
}
