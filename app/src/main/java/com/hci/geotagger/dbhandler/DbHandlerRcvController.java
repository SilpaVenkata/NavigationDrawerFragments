package com.hci.geotagger.dbhandler;

import java.io.File;
import java.util.ArrayList;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.AccountHandler;
import com.hci.geotagger.connectors.AdventureHandler;
import com.hci.geotagger.connectors.CommentHandler;
import com.hci.geotagger.connectors.GeotaggerHandler;
import com.hci.geotagger.connectors.ImageHandler;
import com.hci.geotagger.connectors.ReturnInfo;
import com.hci.geotagger.connectors.TagHandler;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.AdventureTags;
import com.hci.geotagger.objects.Comment;
import com.hci.geotagger.objects.GeotaggerObject;
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
 * This class maintains and supports multiple DbHandlerReceivers. This class will  
 * 
 * To implement multiple threads for the DbHandlerReceivers modify the constructor, the start() 
 * function and the getReceiver() method.
 * 
 * @author Paul Cushman
 *
 */
public class DbHandlerRcvController {
	private static final String TAG = "DbHandlerRcvController";
	
	private GeotaggerApplication mApp = null;
	private DbHandlerReceiver mDbHandlerReceiver_uploadImage = null;
	private DbHandlerReceiver mDbHandlerReceiver = null;
	
	public Handler mHandler;

	/**
	 * Constructor for the DbHandlerRcvController class.
	 * This constructor will save the input Application and create the DbHandler receivers
	 * that are maintained by this object. Currently there are 2 DbHandler receivers created.
	 * One DbHandler will process the upload image requests, the other DbHandler will process
	 * the remainder of the actions. The DbHandler receivers are not started, a call to the
	 * start() method must be made to do that.
	 * @param app This is the GeotaggerApplication object
	 */
	public DbHandlerRcvController(GeotaggerApplication app) {
		mApp = app;
		mDbHandlerReceiver = new DbHandlerReceiver(mApp);
		mDbHandlerReceiver_uploadImage = new DbHandlerReceiver(mApp);
	}
	
	/**
	 * This method will start the DbHandler receiver threads.
	 */
	public void start() {
		mDbHandlerReceiver.start();
		mDbHandlerReceiver_uploadImage.start();
	}
	
	// TODO: SHould add the ability to stop the DbHandler threads

	/**
	 * This method will return the DbHandlerReceiver thread instance to use to communicate
	 * with the DbHandlerReceiver.  Currently the action will identify which thread to use.
	 * The thread to use is based on the input DbHandler action.
	 * @param action The action which will identify which DbHandler thread to use
	 * @return The DbHandlerReceiver object is returned
	 */
	private DbHandlerReceiver getReceiver(int action) {
//		if (action == DbHandlerConstants.DBMSG_UPLOAD_IMAGE)
//			return mDbHandlerReceiver_uploadImage;
		return mDbHandlerReceiver;
	}

	/*
	 * The following is a group of overloaded sendMessage() methods. Each method supports a
	 * different way to send a message to a DbHandler Receiver.
	 */
	
	/**
	 * Send a message to a DbHandler Receiver thread.
	 * @param key This key identifies the Activity to respond to. It should be unique.
	 * @param id A message which will be returned in all results. No significance to the Dbhandler Receiver
	 * @param context The Activities context. Needed to access some services.
	 * @param action Identifies the action to be performed
	 * @param obj An object that is associated with the action.
	 * @return Returns true if the message is sent successfully, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action, Object obj) {
		return sendMessage(key, id, context, action, obj, DbHandlerConstants.FLAG_DEFAULT);
	}
	
	/**
	 * Send a message to a DbHandler Receiver thread.
	 * @param key This key identifies the Activity to respond to. It should be unique.
	 * @param id A message which will be returned in all results. No significance to the Dbhandler Receiver
	 * @param context The Activities context. Needed to access some services.
	 * @param action Identifies the action to be performed
	 * @param obj An object that is associated with the action.
	 * @param flags Flags that identify how to perform the action. See the flags in the DbHandlerConstants file
	 * @return Returns true if the message is sent successfully, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action, Object obj, int flags) {
		DbHandlerReceiver rcvr;
		
		rcvr = getReceiver(action);
		if (rcvr == null)
			return false;
		
		return rcvr.sendMessage(key,  id,  context,  action, obj, flags);
	}
	
	/**
	 * Send a message to a DbHandler Receiver thread.
	 * @param key This key identifies the Activity to respond to. It should be unique.
	 * @param id A message which will be returned in all results. No significance to the Dbhandler Receiver
	 * @param context The Activities context. Needed to access some services.
	 * @param action Identifies the action to be performed
	 * @param index Index associated with the action
	 * @param obj An object that is associated with the action.
	 * @param flags Flags that identify how to perform the action. See the flags in the DbHandlerConstants file
	 * @return Returns true if the message is sent successfully, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action, int index, Object obj) {
		return sendMessage(key, id, context, action, index, obj, DbHandlerConstants.FLAG_DEFAULT);
	}
	
	/**
	 * Send a message to a DbHandler Receiver thread.
	 * @param key This key identifies the Activity to respond to. It should be unique.
	 * @param id A message which will be returned in all results. No significance to the Dbhandler Receiver
	 * @param context The Activities context. Needed to access some services.
	 * @param action Identifies the action to be performed
	 * @param index Index associated with the action
	 * @param obj An object that is associated with the action.
	 * @param flags Flags that identify how to perform the action. See the flags in the DbHandlerConstants file
	 * @return Returns true if the message is sent successfully, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action, int index, Object obj, int flags) {
		DbHandlerReceiver rcvr;
		
		rcvr = getReceiver(action);
		if (rcvr == null)
			return false;
		
		return rcvr.sendMessage(key, id, context, action, index, obj, flags);
	}

	/**
	 * Send a message to a DbHandler Receiver thread.
	 * @param key This key identifies the Activity to respond to. It should be unique.
	 * @param id A message which will be returned in all results. No significance to the Dbhandler Receiver
	 * @param context The Activities context. Needed to access some services.
	 * @param action Identifies the action to be performed
	 * @return Returns true if the message is sent successfully, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action) {
		return sendMessage(key, id, context, action, DbHandlerConstants.FLAG_DEFAULT);
	}
	
	/**
	 * Send a message to a DbHandler Receiver thread.
	 * @param key This key identifies the Activity to respond to. It should be unique.
	 * @param id A message which will be returned in all results. No significance to the Dbhandler Receiver
	 * @param context The Activities context. Needed to access some services.
	 * @param action Identifies the action to be performed
	 * @param flags Flags that identify how to perform the action. See the flags in the DbHandlerConstants file
	 * @return Returns true if the message is sent successfully, false if not
	 */
	public boolean sendMessage(String key, int id, Context context, int action, int flags) {
		DbHandlerReceiver rcvr;
		
		rcvr = getReceiver(action);
		if (rcvr == null)
			return false;
		
		return rcvr.sendMessage(key, id, context, action, flags);
	}
	
}
