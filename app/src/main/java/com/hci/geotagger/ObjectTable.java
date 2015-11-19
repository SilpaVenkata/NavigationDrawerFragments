package com.hci.geotagger;

import java.util.HashMap;

import com.hci.geotagger.dbhandler.DbHandlerResponse;

import android.os.Handler;

/**
 * The HandlerTable object is used to maintain a table of objects so that:
 *   - activities can activate and deactivate the handlers 
 *   - handlers can activate actions
 * This is used by the application to maintain lists of keyed objects.
 * 
 * @author Paul Cushman
 */
public class ObjectTable {
	private static final String TAG = "ObjectTable";
	
	HashMap<String, Object> mMap = new HashMap<String, Object>();
    private static final Object hdlrLock = new Object();
	
    /**
     * Constructor for this class. There is nothing to do to construct this class object.
     */
	public ObjectTable() {
	}
	
	/**
	 * This method will return the message Object associated with the input key
	 * @param key
	 * @return The message Object associated with the input key
	 */
	public Object getObject(String key) {
		Object handler;
		
		synchronized (hdlrLock) {
			handler = (Object)mMap.get(key);
		}
		return handler;
	}
	
	/**
	 * This method will add the object associated with the input object. Current
	 * object types supported include: DbHandlerResponse
	 * @param object the object that contains a key and object to add to the list
	 * @return True if the handler is added successfully, false otherwise
	 */
	public boolean addObject(Object object) {
		Object targetObject;
		String key;
		
		if (object instanceof DbHandlerResponse) {
			DbHandlerResponse dbHandler = (DbHandlerResponse)object;
			targetObject = dbHandler.getHandler();
			key = dbHandler.getID();
		} else {
			return false;
		}
		
		if (targetObject == null || key == null)
			return false;

		synchronized (hdlrLock) {

			// If the key already exists then remove that entry
			if (mMap.containsKey(key)) {
				mMap.remove(key);
			}
			mMap.put(key, targetObject);
		}
		return true;
	}

	public boolean addObject(String key, Object object) {
		if (object == null || key == null)
			return false;

		synchronized (hdlrLock) {

			// If the key already exists then remove that entry
			if (mMap.containsKey(key)) {
				mMap.remove(key);
			}
			mMap.put(key, object);
		}
		return true;
	}

	/**
	 * This method will remove the object associated with the input key from
	 * the list of the current objects.
	 * @param key The key of the object to be removed
	 * @return true if the object is removed.
	 */
	public boolean removeObject(String key) {
		if (key == null)
			return false;

		synchronized (hdlrLock) {

			// If the key already exists then remove that entry
			if (mMap.containsKey(key)) {
				mMap.remove(key);
			}
		}
		return true;
	}

	/**
	 * This method will remove the message handler associated with the input DbHandlerResponse
	 * object.
	 * @param responseHandler The handler to remove
	 * @return True if the handler is removed
	 */
	public boolean removeObject(Object object) {
		String key;
		if (object instanceof DbHandlerResponse) {
			DbHandlerResponse dbHandler = (DbHandlerResponse)object;
			key = dbHandler.getID();
		} else {
			return false;
		}
		return(removeObject(key));
	}

}
