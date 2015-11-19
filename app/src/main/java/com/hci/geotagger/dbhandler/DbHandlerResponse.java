package com.hci.geotagger.dbhandler;

import com.hci.geotagger.dbhandler.DbHandlerReceiver.MessageLayout;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * This class is to be used by Activities that will interface with the Database Handler
 * Requests made to the Database Handler will send responses to the Handler defined within
 * this object. The handler will use the interface to communicate back to the Activity.
 * 
 * @author Paul Cushman
 */
public class DbHandlerResponse {
	private static final String TAG = "DbHandlerResponse";
	
	private String mID = null;
	private Context mContext = null;
	private Handler mHandler = null;
	private DbMessageResponseInterface mCallbacks = null;

	/**
	 * This interface definition is used to communicate back to the Activity that this
	 * class object is instantiated within.
	 */
	public interface DbMessageResponseInterface {
		public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response);
	}
	
	/**
	 * Constructor for this class. Saves the input values to be used by the
	 * methods of this class.
	 * @param id Unique ID for this class
	 * @param context Context to perform actions that need one (not used currently) 
	 * @param callbacks Interface callbacks used by this object to communicate back to the activity
	 */
	public DbHandlerResponse(String id, Context context, DbMessageResponseInterface callbacks) {
		mID = id;
		mContext = context;
		mCallbacks = callbacks;
		mHandler = databaseResponseHandler;
	}
	
	/**
	 * Return the "Unique" id of this object
	 * @return The ID associated with this class object
	 */
	public String getID() {
		return mID;
	}
	
	/**
	 * Return the Handler for this object
	 * @return The handler associated with this class object
	 */
	public Handler getHandler() {
		return mHandler;
	}

	/**
	 * This is the message handler for the Database Handler
	 */
	private Handler databaseResponseHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mCallbacks != null) {
				if (msg.obj != null && msg.obj instanceof MessageLayout) {
					MessageLayout msgLayout = (MessageLayout)msg.obj;
					mCallbacks.DbMessageResponse_DBCallback(msg.what, msgLayout.messageID, msg.arg1==1, msg.arg2==1, msgLayout.obj);
				}
			}
		}
	};
	
	public static void sendResponse(Handler handler, int what, int arg1, int arg2, Object response) {
		Message msg;
		msg = handler.obtainMessage(what, arg1, arg2, response);
		msg.sendToTarget();
	}
}
