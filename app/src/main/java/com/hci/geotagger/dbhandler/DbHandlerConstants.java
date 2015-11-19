package com.hci.geotagger.dbhandler;

/**
 * This class defines static values that are used by the DbHandler.
 * 
 * @author Paul Cushman
 *
 */
public class DbHandlerConstants {
	/*
	 * DB Messages
	 * The following definitions are actions that are supported by the DB Handler
	 */
	
	/*
	 * These are generic DB handler actions. The object type passed to the handler
	 * will identify which handlers to use
	 */
	public static final int DBMSG_ADD = 1;				// Add a record
	public static final int DBMSG_UPDATE = 2;			// Modify a record's values
	public static final int DBMSG_DELETE = 3;			// Delete a record
	
	// The following actions are more specific in nature
	
	public static final int DBMSG_GET_ALL_TAGS = 200;	// Get all Tags associated with a user
	public static final int DBMSG_GET_TAG = 201;		// Get a specific tag
	
	public static final int DBMSG_GET_USERS = 300;				// Get all of the users
	public static final int DBMSG_GET_GROUPS = 350;				// Get all groups
	public static final int DBMSG_GET_GROUPS_OWNER = 351;		// Get groups current user is owner of
	public static final int DBMSG_GET_GROUPS_MEMBER = 352;		// Get groups current user is member of
	public static final int DBMSG_GET_GROUP_TAGS = 353;			// Get Tags associated with a group
	public static final int DBMSG_GET_GROUP_MEMBERS = 354;		// Get Members associated with a group
	public static final int DBMSG_GET_GROUP_ADVENTURES = 355;	// Get Adventures associated with a group
	
	public static final int DBMSG_GET_ALL_ADVENTURES = 400;			// Get all adventures
	public static final int DBMSG_GET_ADVENTURES_MEMBEROF = 401;	// Get all user's adventures
	public static final int DBMSG_GET_ADVENTURES_OWNEROF = 402;		// Get logged in user's adventures
	
	public static final int DBMSG_GET_ADVENTURE_TAGS = 410;		// Get tags of an adventure
	
	public static final int DBMSG_GET_TAG_COMMENTS = 500;		// Get Tag's comments
	public static final int DBMSG_GET_COMMENT = 502;			// Get a specific comment
	
	public static final int DBMSG_GET_ADVENTURE_USERS = 600;	// Get users associated with an adventure
	
	public static final int DBMSG_UPLOAD_IMAGE = 700;			// Upload image to server
	public static final int DBMSG_GET_SCALED_IMAGES = 701;		// Get an image and scale it
	
	public static final int DBMSG_LOGIN_BYNAME = 800;			// Do a login by name/password
	public static final int DBMSG_LOGIN_VALIDATETOKEN = 801;	// Validate the access_token, login
	public static final int DBMSG_LOGIN_REFRESHTOKEN = 802;		// Refresh the token using refresh_token
	
	// This message is used to cancel any messages on the queue
	public static final int DBMSG_CANCEL_MESSAGES = 1000;
	
	/*
	 * Definition of flags used by the Database Handler
	 */
	
	/*
	 * These flags are used to identify where to get records from. If only FLAG_CACHE is set
	 * then the record will be retrieved from ONLY the cache.  If only FLAG_SERVER is set
	 * then the reocord(s) will be retrieved from only the server.
	 */
	public static final int FLAG_CACHE = 1;
	public static final int FLAG_SERVER = 2;
	public static final int FLAG_DEFAULT = FLAG_CACHE | FLAG_SERVER;
	
}
