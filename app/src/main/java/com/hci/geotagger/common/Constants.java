package com.hci.geotagger.common;

/**
 * Class that contains many values used in many places in GeoTagger application,including server url's, 
 * success an failure strings, and operation strings.
 *
 */
public final class Constants {
	//JSON object strings
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	public static final String ERROR_MSG = "error_msg";
	
	//Shared preferences settings 
	public static final String LOGIN_DATAFILE = "LoginData";
	public static final int MODE_PRIVATE = 0;
	public static final String KEY_LOGGEDIN = "LoggedIn";
	public static final String KEY_UID= "UserID";
	public static final String KEY_PASS = "Password";
	public static final String KEY_ACCESS_TOKEN = "AccessToken";
	public static final String KEY_REFRESH_TOKEN = "RefreshToken";
	public static final boolean ADVENTURE_ADD_CAPABLE = true;
	public static final boolean ADVENTURE_EDITABLE = false;
	public static final boolean ADVENTURE_ADDTAG_CAPABLE= true;
	public static final boolean LIMIT_HOME_TO_ADVENTURE = true;

	//Login Mode
	public static final int LOGIN_BYID = 0;
	public static final int LOGIN_BYNAME = 1;
	
	//Exception Messages
	public static final String USERNAME_IS_TAKEN = "Requested username has already been taken. Please try a different username.";
	public static final String UNKNOWN_ERROR = "Unknown error occurred. Please try again.";
	
	//Request codes
	public static final int SELECT_IMG = 1;
	public static final int CAPTURE_IMG = 2;
	public static final int ADD_COMMENT_ACTIVITY_RESULT = 3;
	
	//Visibility
	public static final int VISIBILITY_PRIVATE = 0;
	public static final int VISIBILITY_FULL = 1;
	public static final int VISIBILITY_LIMITED = 2;
	
	//Default rating
	public static final int RATING_DEFAULT = 0;
	
	//Categories
	public static final String CATEGORY_DEFAULT = "Default";
	
	//Images
	public static final int MAX_IMAGE_HEIGHT = 600;
	public static final int MAX_IMAGE_WIDTH = 600;
	public static final int IMAGE_QUALITY = 75;
	public static final String IMAGE_EXT = ".jpg";
	public static final String IMAGE_PREFIX = "img";
	
	//Cache Constants
	public static final String CACHE_DIRECTORY = "Geotagger Cache";
	public static final String CACHE_FILEPREFIX = "GTCache_";
	
	//Image Album Name
	public static final String ALBUM_NAME = "Geotagger_Images";
	
	//DateTime format
	public static final String DATETIME_FORMAT = "MM/dd/yy hh:mma";
	public static final String DATE_FORMAT = "MM/dd/yy";
	public static final String TIME_FORMAT = "hh:mma";
	
	//ADDFriend result constants
	public static final int ADDFRIEND_SUCCESS = 1;
	public static final int ADDFRIEND_ALREADYFRIENDS = 1;
	public static final int ADDFRIEND_USERNOTFOUND = 2;
	public static final int ADDFRIEND_ERROR = 3;
	
	//img cache size for tag/friend thumbnails
	public static final int IMG_CACHE_SIZE = 50;	
	
	//added by Kale for new UI testing
	public static final boolean USE_NEW_TAGVIEW_UI = true;
	
	//show database dumps
	public static final boolean SHOW_DB_DUMPS = false;
	
	// Activity arguments definitions
	public static final String EXTRA_ADVENTURE = "adventure";
	public static final String EXTRA_TAGID = "tagID";
	public static final String EXTRA_IMAGEID = "imageID";
	public static final String EXTRA_COMMENTID = "commentID";
	public static final String EXTRA_ID = "id";
}
