package com.hci.geotagger;

import com.hci.geotagger.connectors.GeotaggerHandler;
import com.hci.geotagger.connectors.OAuthHandler;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerRcvController;
import com.hci.geotagger.dbhandler.DbHandlerResponse;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/**
 * This is the Application class associated with the GeoTagger application. This class
 * was implemented initially to support the interface to and from the DbHandler threads.
 * Any values that to be shared between Activities of this application can be defined
 * here using static variables.
 * 
 * @author Paul Cushman
 *
 */
public class GeotaggerApplication extends Application {

	private OAuthHandler oauth;

	private static DbHandlerRcvController mDbHandlerRcvController = null;
	private static ObjectTable mDbResponseTable = null;
	private static int mDbHandlerRequestID = 1;
	
	/*
	 *  This table maintains a list of Handler Actions. This is used to allow handlers to
	 *  register what DB actions they support, which will be used by the caching to perform
	 *  cached actions.
	 */
	private static ObjectTable mHandlerActionTable = null;
	
	/**
	 * Overriding the onCreate method. This onCreate method will create the data structures
	 * necessary to support the DbHandler and then create and start the DbHandler Receive
	 * Controller object. This object is used to communicate to the DbHandler Receiver
	 * thread(s).
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		oauth = new OAuthHandler(this);

		mDbResponseTable = new ObjectTable();
		mDbHandlerRcvController = new DbHandlerRcvController(this);
		mDbHandlerRcvController.start();
		
		mHandlerActionTable = new ObjectTable();
	}

	public boolean isAuthorized() {
		return oauth.hasAccessToken();
	}

	public int sendMsgToDbHandler(DbHandlerResponse rspHdlr, Context context, int action, Object obj) {
		return sendMsgToDbHandler(rspHdlr, context, action, obj, DbHandlerConstants.FLAG_DEFAULT);
	}

	/**
	 * Send message to the database handler
	 * @param rspHdlr
	 * @param context
	 * @param action
	 * @param obj
	 */
	public int sendMsgToDbHandler(DbHandlerResponse rspHdlr, Context context, int action, Object obj, int flags) {
		if (mDbHandlerRcvController == null)
			return 0;
		mDbHandlerRcvController.sendMessage(rspHdlr.getID(), mDbHandlerRequestID, context, action, obj, flags);
		return mDbHandlerRequestID++;
	}

	public int sendMsgToDbHandler(DbHandlerResponse rspHdlr, Context context, int action, int index, Object obj) {
		return sendMsgToDbHandler(rspHdlr, context, action, index, obj, DbHandlerConstants.FLAG_DEFAULT);
	}

	/**
	 * Send message to the database handler
	 * @param rspHdlr
	 * @param context
	 * @param action
	 * @param obj
	 */
	public int sendMsgToDbHandler(DbHandlerResponse rspHdlr, Context context, int action, int index, Object obj, int flags) {
		if (mDbHandlerRcvController == null)
			return 0;
		mDbHandlerRcvController.sendMessage(rspHdlr.getID(), mDbHandlerRequestID, context, action, obj, flags);
		return mDbHandlerRequestID++;
	}
	
	/**
	 * Send a message to the Database Handler Thread
	 * @param what
	 * @param arg1
	 * @param arg2
	 */
	public int sendMsgToDbHandler(DbHandlerResponse rspHdlr, Context context, int action) {
		if (mDbHandlerRcvController == null)
			return 0;
		mDbHandlerRcvController.sendMessage(rspHdlr.getID(), mDbHandlerRequestID, context, action);
		return mDbHandlerRequestID++;
	}

	/** 
	 * This method will add the input response handler to the table of response handlers.
	 * If a handler with the same key exists then the old one will be deleted and the new
	 * one will be added.
	 * @param rspHdlr
	 */
	public void addResponseHandler(DbHandlerResponse rspHdlr) {
		if (mDbResponseTable != null) {
			mDbResponseTable.addObject(rspHdlr);
		}
	}
	
	/**
	 * This method will remove the input response handler from the table of response handlers
	 * @param rspHdlr
	 */
	public void removeResponseHandler(DbHandlerResponse rspHdlr) {
		if (mDbResponseTable != null) {
			mDbResponseTable.removeObject(rspHdlr);
		}
	}
	
	/**
	 * This method will send a response to the handler associated with the input key
	 * @param key
	 * @param what
	 * @param response
	 */
	public boolean sendResponse(String key, int what, int arg1, int arg2, Object response) {
		Handler handler = (Handler)mDbResponseTable.getObject(key);
		if (handler == null)
			return false;
		DbHandlerResponse.sendResponse(handler, what, arg1, arg2, response);
		return true;
	}
	
	/**
	 * This method will add the input Handler to the list of GeotaggerHandler, using
	 * the input actionKey as the key.  The actionKey is specific to a database action
	 * that can be performed by the handler.
	 * @param handler The GeotaggerHandler associated with the input actionKey
	 * @param actionKey The actionKey supported by the input GeotaggerHandler
	 */
	public void addHandlerAction(GeotaggerHandler handler, String actionKey) {
		if (mHandlerActionTable != null)
			mHandlerActionTable.addObject(actionKey, handler);
	}
	
	/**
	 * This method will retrieve the Handler object associated with the input action
	 * key value. The returned object is an instantiated handler.
	 * @param action The DB action to search for
	 * @return The Handler object associated with the input action key
	 */
	public Object getHandlerAction(String action) {
		Object retObject = null;
		if (mHandlerActionTable != null)
			retObject = mHandlerActionTable.getObject(action);
		return retObject;
	}
}