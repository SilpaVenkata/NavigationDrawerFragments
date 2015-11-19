package com.hci.geotagger.cache;

import static android.provider.BaseColumns._ID;

import java.io.File;
import java.util.Date;

import com.hci.geotagger.objects.Group;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.AdventureMembers;
import com.hci.geotagger.objects.AdventureTags;
import com.hci.geotagger.objects.Comment;
import com.hci.geotagger.objects.Friend;
import com.hci.geotagger.objects.GeoLocation;
import com.hci.geotagger.objects.MyUserAccount;
import com.hci.geotagger.objects.Relations;
import com.hci.geotagger.objects.Tag;
import com.hci.geotagger.objects.UserAccount;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The CacheDatabase class contains definitions and methods that are used to
 * access the Geotagger Cache Database.  This database is a close representation 
 * of the Geotagger server database with additional tables and fields to maintain
 * a caching relation between the mobile device and the server database.  The
 * CacheDatabase class extends the SQLiteOpenHelper class.
 * 
 * @author Paul Cushman
 *
 */
public class CacheDatabase extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "geotaggercache.db";
	
	//NOTE: YOU MUST CHANGE THE VERSION NUMBER IF THE SCHEMA IS CHANGED!!!
	private static final int DATABASE_VERSION = 11;
	
	private static final String TAG = "CacheDatabase";
	
	private static final String GENERAL_UPDATETIME_FIELD = "UpdateTime";		// General purpose Update Time field(s)
   
	private static final String ACTION_TABLE_NAME = "ActionCache";			// Action Cache Table
	private static final String ACTION_TIME_FIELD = "CacheTime";				// Time of cache, used as ordering key
	private static final String ACTION_ACTION_FIELD = "ActionString";		// Action to perform on the database
	private static final String ACTION_POSTACTIONS_FIELD = "PostActions";	// Post DB operation actions to perform
	
	private static final String ACTION_TIME_KEY = "ActionTimeKey";			// Key for Action Time
	
	private static final String CACHEREF_TABLE_NAME = "CacheReference";		// Image Cache Table
	private static final String CACHEREF_KEY_FIELD = "cacheRefKey";			// Key field, file name
	private static final String CACHEREF_UPDATETIME_FIELD = "UpdateTime";	// Last time record is touched/viewed
	private static final String CACHEREF_FILENAME_FIELD = "cacheRefFName";	// File name where cached image is located
	
	private static final String ACCOUNT_TABLE_NAME = "AccountTable";			// Accounts table
	private static final String ACCOUNT_ID_FIELD = "AccountID";
	private static final String ACCOUNT_USERNAME_FIELD = "Username";
	private static final String ACCOUNT_NAME_FIELD = "Name";
	private static final String ACCOUNT_EMAILADDRESS_FIELD = "EmailAddress";
	private static final String ACCOUNT_PASSWORD_FIELD = "Password";
	private static final String ACCOUNT_IMAGE_FIELD = "Image";
	private static final String ACCOUNT_DESCRIPTION_FIELD = "Description";
	private static final String ACCOUNT_LOCATION_FIELD = "Location";
	private static final String ACCOUNT_QUOTE_FIELD = "Quote";
	private static final String ACCOUNT_TYPE_FIELD = "Type";
	private static final String ACCOUNT_VISIBILITY_FIELD = "Visibility";
	private static final String ACCOUNT_CREATEDDATETIME_FIELD = "CreatedDateTime";
	private static final String ACCOUNT_RATINGSCORE_FIELD = "RatingScore";
	private static final String ACCOUNT_UPDATETIME_FIELD = GENERAL_UPDATETIME_FIELD;
	
	private static final String ACCOUNT_ACCOUNTID_KEY = "AccountID_UNIQUE";
	private static final String ACCOUNT_USERNAME_KEY = "Username_UNIQUE";

	private static final String FRIEND_TABLE_NAME = "FriendAssociationTable";			// Friends Table
	private static final String FRIEND_UID_FIELD = "uID";
	private static final String FRIEND_FID_FIELD = "fID";

	private static final String FRIEND_UID_KEY = "FriendsUID_UNIQUE";
	private static final String FRIEND_FID_KEY = "FriendsFID_UNIQUE";

	

	private static final String TAGCOMMENT_TABLE_NAME = "TagCommentTable";
	private static final String TAGCOMMENT_COMMENTID_FIELD = "commentID";
	private static final String TAGCOMMENT_TAGID_FIELD = "tagID";
	private static final String TAGCOMMENT_COMMENT_FIELD = "comment";
	private static final String TAGCOMMENT_UNAME_FIELD = "uName";
	private static final String TAGCOMMENT_IMGURL_FIELD = "imgUrl";
	private static final String TAGCOMMENT_UPDATETIME_FIELD = GENERAL_UPDATETIME_FIELD;
	
	
	private static final String TAGS_TABLE_NAME = "TagsTable";
	private static final String TAGS_TAGID_FIELD = "tagID";
	private static final String TAGS_OWNERID_FIELD = "OwnerID";
	private static final String TAGS_VISIBILITY_FIELD = "Visibility";
	private static final String TAGS_NAME_FIELD = "Name";
	private static final String TAGS_DESCRIPTION_FIELD = "Description";
	private static final String TAGS_IMAGEURL_FIELD = "ImageUrl";
	private static final String TAGS_LOCATION_FIELD = "Location";
	private static final String TAGS_LATITUDE_FIELD = "Latitude";
	private static final String TAGS_LONGITUDE_FIELD = "Longitude";
	private static final String TAGS_CREATEDDATETIME_FIELD = "CreatedDateTime";
	private static final String TAGS_CATEGORY_FIELD = "Category";
	private static final String TAGS_RATINGSCORE_FIELD = "RatingScore";
	private static final String TAGS_UPDATETIME_FIELD = GENERAL_UPDATETIME_FIELD;

	private static final String TAGS_TAGID_KEY = "TAGS_TagID_KEY";
	private static final String TAGS_OWNERID_KEY = "TAGS_OwnerID_KEY";

	private static final String ADVENTURES_TABLE_NAME = "AdventuresTable";
	private static final String ADVENTURES_ADVENTUREID_FIELD = "adventureID";
	private static final String ADVENTURES_OWNERID_FIELD = "OwnerID";
	private static final String ADVENTURES_NAME_FIELD = "Name";
	private static final String ADVENTURES_DESCRIPTION_FIELD = "Description";
	private static final String ADVENTURES_VISIBILITY_FIELD = "Visibility";
	private static final String ADVENTURES_CREATEDDATETIME_FIELD = "CreatedDateTime";
	private static final String ADVENTURES_UPDATETIME_FIELD = GENERAL_UPDATETIME_FIELD;

	private static final String ADVENTURES_TAGID_KEY = "ADVENTURES_TagID_KEY";
	private static final String ADVENTURES_OWNERID_KEY = "ADVENTURES_OwnerID_KEY";

	private static final String ADVENTURETAGS_TABLE_NAME = "AdventureTagssTable";
	private static final String ADVENTURETAGS_ADVENTUREID_FIELD = "adventureID";
	private static final String ADVENTURETAGS_TAGID_FIELD = "tagID";

	private static final String ADVENTUREMEMBERS_TABLE_NAME = "AdventureMembersTable";
	private static final String ADVENTUREMEMBERS_ADVENTUREID_FIELD = "adventureID";
	private static final String ADVENTUREMEMBERS_ACCOUNTID_FIELD = "AccountID";

	//Group table
	private static final String GROUPS_TABLE_NAME = "GroupsTable";
	private static final String GROUPS_GROUPID_FIELD = "groupID";
	private static final String GROUPS_OWNERID_FIELD = "OwnerID";
	private static final String GROUPS_NAME_FIELD = "Name";
	private static final String GROUPS_DESCRIPTION_FIELD = "Description";
	private static final String GROUPS_IMAGEURL_FIELD = "ImageUrl";
	private static final String GROUPS_CREATEDDATE_FIELD = "CreatedDateTime";

	private static final String GROUPS_GROUPID_KEY = "GROUPS_group_id_KEY";
	private static final String GROUPS_OWNERID_KEY = "GROUPS_owner_user_id_KEY";

	//Group Member Table 
	private static final String GROUPMEMBERS_TABLE_NAME = "GroupMembers";
	private static final String GROUPMEMBERS_GROUPID_FIELD = "groupID";
	private static final String GROUPMEMBERS_USERID_FIELD = "userID";
	private static final String GROUPMEMBERS_MEMBERTYPE_FIELD = "MemberType";
	private static final String GROUPMEMBERS_JOINDATE_FIELD = "JoinDate";

	//Group Tags Table
	private static final String GROUPTAGS_TABLE_NAME = "GroupTags";
	private static final String GROUPTAGS_GROUPID_FIELD = "groupID";
	private static final String GROUPTAGS_TAGID_FIELD = "tagID";


	/*
	 *  Starting ID value to use when record is cached, but NOT in database.  Records
	 *  that are stored in the database will have positive ID numbers.  Records that are
	 *  added to the cache, but are not yet in the database will have negative number ID
	 *  values.  Create actions for these records are pending successful database opertions.
	 */
	private static final long CACHE_ID_START = -2;

	private Activity	m_thisActivity;
    private static final Object dbLock = new Object();

    /**
     * This constructor calls the SQLiteOpenHelper constructor to create the SQLite
     * Helper object.
     * @param ctx The context of the activity constructing this object.
     */
	public CacheDatabase(Context ctx) { 
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		//m_thisActivity = (Activity)ctx;//TODO(Silpa)
	}

	/**
	 * This method will return the name of this databsae.
	 * @return A string name of the Geotagger cache database
	 */
	public String getName() {
		return DATABASE_NAME;
	}

	/**
	 * This method will create the tables and keys associated with the
	 * Geotagger database.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) { 

		// Action Cache Table
		db.execSQL("CREATE TABLE " + ACTION_TABLE_NAME + " (" +
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				ACTION_TIME_FIELD + " INTEGER UNIQUE," +
				ACTION_ACTION_FIELD + " TEXT NOT NULL," +
				ACTION_POSTACTIONS_FIELD + " TEXT);" );

		db.execSQL("CREATE INDEX " + ACTION_TIME_KEY + " ON " + ACTION_TABLE_NAME + "(" + ACTION_TIME_FIELD + ");" );

		// Cache Reference Table
		db.execSQL("CREATE TABLE " + CACHEREF_TABLE_NAME + " (" +
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		   		CACHEREF_KEY_FIELD + " TEXT NOT NULL," +
		   		CACHEREF_FILENAME_FIELD + " TEXT NOT NULL," +
		   		CACHEREF_UPDATETIME_FIELD + " INTEGER);" );

		// Account Table
		db.execSQL("CREATE TABLE " + ACCOUNT_TABLE_NAME + " (" +
		   		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		   		ACCOUNT_ID_FIELD + " INTEGER UNIQUE," +
		   		ACCOUNT_USERNAME_FIELD + " TEXT NOT NULL," +
		   		ACCOUNT_NAME_FIELD + " TEXT," +
		   		ACCOUNT_EMAILADDRESS_FIELD + " TEXT," +
		   		ACCOUNT_PASSWORD_FIELD + " TEXT," +
		   		ACCOUNT_IMAGE_FIELD + " TEXT," +
		   		ACCOUNT_DESCRIPTION_FIELD + " TEXT," +
		   		ACCOUNT_LOCATION_FIELD + " TEXT," +
		   		ACCOUNT_QUOTE_FIELD + " TEXT," +
		   		ACCOUNT_TYPE_FIELD + " INTEGER," +
		   		ACCOUNT_VISIBILITY_FIELD + " INTEGER," +
		   		ACCOUNT_CREATEDDATETIME_FIELD + " INTEGER," +
		   		ACCOUNT_RATINGSCORE_FIELD + " INTEGER," +
		   		ACCOUNT_UPDATETIME_FIELD + " INTEGER);" );
	   
		db.execSQL("CREATE INDEX " + ACCOUNT_ACCOUNTID_KEY + " ON " + ACCOUNT_TABLE_NAME + "(" + ACCOUNT_ID_FIELD + ");" );
		db.execSQL("CREATE INDEX " + ACCOUNT_USERNAME_KEY + " ON " + ACCOUNT_TABLE_NAME + "(" + ACCOUNT_USERNAME_FIELD + ");" );

		
		// Friend Association Table
		db.execSQL("CREATE TABLE " + FRIEND_TABLE_NAME + " (" +
		   		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		   		FRIEND_UID_FIELD + " INTEGER," +
		   		FRIEND_FID_FIELD + " INTEGER," +
		   		"FOREIGN KEY("+FRIEND_UID_FIELD+") REFERENCES "+ ACCOUNT_TABLE_NAME + "(" + ACCOUNT_ID_FIELD + ")," +
		   		"FOREIGN KEY("+FRIEND_FID_FIELD+") REFERENCES "+ ACCOUNT_TABLE_NAME + "(" + ACCOUNT_ID_FIELD + "), " +
		   		"UNIQUE("+ FRIEND_UID_FIELD + ", " +  FRIEND_FID_FIELD + ") ON CONFLICT REPLACE " +
				");");

	   
		db.execSQL("CREATE INDEX " + FRIEND_UID_KEY + " ON " + FRIEND_TABLE_NAME + "(" + FRIEND_UID_FIELD + ");" );
		db.execSQL("CREATE INDEX " + FRIEND_FID_KEY + " ON " + FRIEND_TABLE_NAME + "(" + FRIEND_FID_FIELD + ");" );
		
		
		// Tag Comment Table
		db.execSQL("CREATE TABLE " + TAGCOMMENT_TABLE_NAME + " (" +
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				TAGCOMMENT_COMMENTID_FIELD + " INTEGER UNIQUE," +
				TAGCOMMENT_TAGID_FIELD + " INTEGER," +
		   		TAGCOMMENT_COMMENT_FIELD + " TEXT NOT NULL," +
		   		TAGCOMMENT_UNAME_FIELD + " TEXT NOT NULL," +
		   		TAGCOMMENT_IMGURL_FIELD + " TEXT," +
		   		TAGCOMMENT_UPDATETIME_FIELD + " INTEGER);" );
		
		// Tags Table
		db.execSQL("CREATE TABLE " + TAGS_TABLE_NAME + " (" +
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				TAGS_TAGID_FIELD + " INTEGER UNIQUE," +
		   		TAGS_OWNERID_FIELD + " INTEGER," +
		   		TAGS_NAME_FIELD + " TEXT NOT NULL," +
		   		TAGS_DESCRIPTION_FIELD + " TEXT," +
		   		TAGS_IMAGEURL_FIELD + " TEXT," +
		   		TAGS_LOCATION_FIELD + " TEXT," +
		   		TAGS_LATITUDE_FIELD + " DOUBLE," +
		   		TAGS_LONGITUDE_FIELD + " DOUBLE," +
		   		TAGS_CREATEDDATETIME_FIELD + " INTEGER," +
		   		TAGS_VISIBILITY_FIELD + " INTEGER," +
		   		TAGS_CATEGORY_FIELD + " TEXT," +
		   		TAGS_RATINGSCORE_FIELD + " INTEGER, " +
		   		TAGS_UPDATETIME_FIELD + " INTEGER);" );
		
		db.execSQL("CREATE INDEX " + TAGS_TAGID_KEY + " ON " + TAGS_TABLE_NAME + "(" + TAGS_TAGID_FIELD + ");" );
		db.execSQL("CREATE INDEX " + TAGS_OWNERID_KEY + " ON " + TAGS_TABLE_NAME + "(" + TAGS_OWNERID_FIELD + ");" );

		// Adventures Table
		db.execSQL("CREATE TABLE " + ADVENTURES_TABLE_NAME + " (" +
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				ADVENTURES_ADVENTUREID_FIELD + " INTEGER UNIQUE," +
				ADVENTURES_OWNERID_FIELD + " INTEGER," +
				ADVENTURES_NAME_FIELD + " TEXT NOT NULL," +
				ADVENTURES_DESCRIPTION_FIELD + " TEXT," +
				ADVENTURES_CREATEDDATETIME_FIELD + " INTEGER," +
				ADVENTURES_VISIBILITY_FIELD + " INTEGER," +
				ADVENTURES_UPDATETIME_FIELD + " INTEGER);" );

		db.execSQL("CREATE INDEX " + ADVENTURES_TAGID_KEY + " ON " + ADVENTURES_TABLE_NAME + "(" + ADVENTURES_ADVENTUREID_FIELD + ");" );
		db.execSQL("CREATE INDEX " + ADVENTURES_OWNERID_KEY + " ON " + ADVENTURES_TABLE_NAME + "(" + ADVENTURES_OWNERID_FIELD + ");" );

		// Adventure Tags Table
		db.execSQL("CREATE TABLE " + ADVENTURETAGS_TABLE_NAME + " (" +
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				ADVENTURETAGS_ADVENTUREID_FIELD + " INTEGER," +
				ADVENTURETAGS_TAGID_FIELD + " INTEGER," +
				"FOREIGN KEY("+ADVENTURETAGS_ADVENTUREID_FIELD+") REFERENCES "+ ADVENTURES_TABLE_NAME + "(" + ADVENTURES_ADVENTUREID_FIELD + ")," +
				"FOREIGN KEY("+ADVENTURETAGS_TAGID_FIELD+") REFERENCES "+ TAGS_TABLE_NAME + "(" + TAGS_TAGID_FIELD + "), " +
				"UNIQUE("+ ADVENTURETAGS_ADVENTUREID_FIELD + ", " +  ADVENTURETAGS_TAGID_FIELD + ") ON CONFLICT REPLACE " +
				");");
		
		// Adventure Members Table
		db.execSQL("CREATE TABLE " + ADVENTUREMEMBERS_TABLE_NAME + " (" +
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				ADVENTUREMEMBERS_ADVENTUREID_FIELD + " INTEGER," +
				ADVENTUREMEMBERS_ACCOUNTID_FIELD + " INTEGER," +
				"FOREIGN KEY("+ADVENTUREMEMBERS_ADVENTUREID_FIELD+") REFERENCES "+ ADVENTURES_TABLE_NAME + "(" + ADVENTURES_ADVENTUREID_FIELD + ")," +
				"FOREIGN KEY("+ADVENTUREMEMBERS_ACCOUNTID_FIELD+") REFERENCES "+ ACCOUNT_TABLE_NAME + "(" + ACCOUNT_ID_FIELD + "), " +
				"UNIQUE("+ ADVENTUREMEMBERS_ADVENTUREID_FIELD + ", " +  ADVENTUREMEMBERS_ACCOUNTID_FIELD + ") ON CONFLICT REPLACE " +
				");");
		
		//Group Table
		db.execSQL("CREATE TABLE " + GROUPS_TABLE_NAME + "(" +
				_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, "+ 
				GROUPS_GROUPID_FIELD + " INTEGER UNIQUE, "+ 
				GROUPS_OWNERID_FIELD + " INTEGER, " +
				GROUPS_NAME_FIELD + " TEXT NOT NULL, " +
				GROUPS_DESCRIPTION_FIELD+ " TEXT," +
				GROUPS_IMAGEURL_FIELD+ " TEXT, "+
				GROUPS_CREATEDDATE_FIELD+ " INTEGER);");
			
		db.execSQL("CREATE INDEX " + GROUPS_GROUPID_KEY + " ON " + GROUPS_TABLE_NAME + "(" + GROUPS_GROUPID_FIELD + ");" );
		db.execSQL("CREATE INDEX " + GROUPS_OWNERID_KEY + " ON " + GROUPS_TABLE_NAME + "(" + GROUPS_OWNERID_FIELD + ");" );
			
		//Group Members table 
		db.execSQL("CREATE TABLE " + GROUPMEMBERS_TABLE_NAME + "(" +
				_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, "+ 
				GROUPMEMBERS_GROUPID_FIELD + " INTEGER, "+ 
				GROUPMEMBERS_USERID_FIELD + " INTEGER UNIQUE, " +
				GROUPMEMBERS_MEMBERTYPE_FIELD + " INTEGER, " +
				GROUPMEMBERS_JOINDATE_FIELD + " INTEGER);");

		//Group Tags table 
		db.execSQL("CREATE TABLE " + GROUPTAGS_TABLE_NAME + "(" +
				_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, "+ 
				GROUPTAGS_GROUPID_FIELD + " INTEGER, "+ 
				GROUPTAGS_TAGID_FIELD + " INTEGER);");

	}

	/**
	 * When the database version is modified an upgrade of the database will
	 * be performed.  This method will be called when this occurs.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	   clearAndCreateDB(db);
	}

	/**
	 * This method will clear all of the database indices and tables and then
	 * re-create the database tables and indices
	 * @param db The open database
	 */
	public void clearAndCreateDB(SQLiteDatabase db) {
		// Remove files referenced from Cache Reference table
		deleteAllCacheReferences(db);
		
		// Drop the Views

		// Drop the Indices
		db.execSQL("DROP INDEX IF EXISTS " + ACTION_TIME_KEY );
		db.execSQL("DROP INDEX IF EXISTS " + ACCOUNT_ACCOUNTID_KEY );
		db.execSQL("DROP INDEX IF EXISTS " + ACCOUNT_USERNAME_KEY );
		db.execSQL("DROP INDEX IF EXISTS " + TAGS_TAGID_KEY );
		db.execSQL("DROP INDEX IF EXISTS " + TAGS_OWNERID_KEY );
		db.execSQL("DROP INDEX IF EXISTS " + FRIEND_UID_KEY );
		db.execSQL("DROP INDEX IF EXISTS " + FRIEND_FID_KEY );
		
		db.execSQL("DROP INDEX IF EXISTS " + GROUPS_GROUPID_KEY );
		db.execSQL("DROP INDEX IF EXISTS " + GROUPS_OWNERID_KEY );

		
		// Drop the Tables
		db.execSQL( "DROP TABLE IF EXISTS " +ACTION_TABLE_NAME );
		db.execSQL( "DROP TABLE IF EXISTS " +CACHEREF_TABLE_NAME );
		db.execSQL( "DROP TABLE IF EXISTS " +ACCOUNT_TABLE_NAME );
		db.execSQL( "DROP TABLE IF EXISTS " +FRIEND_TABLE_NAME );
		db.execSQL( "DROP TABLE IF EXISTS " +TAGCOMMENT_TABLE_NAME );
		db.execSQL( "DROP TABLE IF EXISTS " +TAGS_TABLE_NAME );
		db.execSQL( "DROP TABLE IF EXISTS " +ADVENTURES_TABLE_NAME);
		db.execSQL( "DROP TABLE IF EXISTS " +ADVENTURETAGS_TABLE_NAME);
		db.execSQL( "DROP TABLE IF EXISTS " +ADVENTUREMEMBERS_TABLE_NAME);
		db.execSQL( "DROP TABLE IF EXISTS " +GROUPTAGS_TABLE_NAME);
		db.execSQL( "DROP TABLE IF EXISTS " +GROUPMEMBERS_TABLE_NAME);
		db.execSQL( "DROP TABLE IF EXISTS " +GROUPS_TABLE_NAME);

		onCreate(db);
	}
   
	/**
	 * This method will delete all records from all of the tables 
	 * in the Geotagger cache database.
	 */
	public void clearDB() {
		SQLiteDatabase db = getWritableDatabase();
	   
		db.execSQL( "DELETE FROM " +ACTION_TABLE_NAME );
		db.execSQL( "DELETE FROM " +CACHEREF_TABLE_NAME );
		db.execSQL( "DELETE FROM " +ACCOUNT_TABLE_NAME );
		db.execSQL( "DELETE FROM " +FRIEND_TABLE_NAME );
		db.execSQL( "DELETE FROM " +CACHEREF_TABLE_NAME );
		db.execSQL( "DELETE FROM " +TAGS_TABLE_NAME );
		db.execSQL( "DELETE FROM " +ADVENTURES_TABLE_NAME);
		db.execSQL( "DELETE FROM " +ADVENTURETAGS_TABLE_NAME);
		db.execSQL( "DELETE FROM " +ADVENTUREMEMBERS_TABLE_NAME);
		db.execSQL( "DELETE FROM " +GROUPTAGS_TABLE_NAME);
		db.execSQL( "DELETE FROM " +GROUPMEMBERS_TABLE_NAME);
		db.execSQL( "DELETE FROM " +GROUPS_TABLE_NAME);

		db.close();
	}
	

	/****************************************************************************************
	 * Database Cache Functions 
	 ****************************************************************************************/

   	/**
   	 * This method will return the current size of the database.
   	 * @return the size of the database
   	 */
	public long getSize() {
		File f = m_thisActivity.getDatabasePath(DATABASE_NAME);
		long dbSize = f.length();
		return dbSize;
	}
	
	/**
	 * This method will clean up the database by removing any records that are old and have not
	 * been accessed in a determined period of time.
	 * 
	 * TODO: Implement me
	 * @return Returns true if successful, false otherwise
	 */
	public boolean cleanUp(long cleanTime) {
		return true; 
	}

	/**
	 * This is a private method which will generate an update time that is used
	 * for the UpdateTime fields of the Geotagger Cache Database
	 * @return A long value which is the current update time value
	 */
	private long getUpdateTime() {
		Date dt = new Date(System.currentTimeMillis());
		long lngDt = dt.getTime();
		return lngDt;
	}

	/**
	 * This is a general purpose method to update any record.  The UpdateTime field will
	 * be added to the list of values.  The UpdateTime field must have the same name for 
	 * every record that uses this method
	 * @param table This is the table to modify
	 * @param where This is the where clause to use to identify the specific record
	 * @param values This is a ContentValues object that contains the values to update
	 * @return If successful true is returned, otherwise false
	 */
	private boolean updateRecord(String table, String where, ContentValues values) {
		if (values == null)
			values = new ContentValues();

		// Add the update time to the values
		long lngDt = getUpdateTime();
		values.put(GENERAL_UPDATETIME_FIELD, lngDt);
	
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			numRows = db.update(table, values, where, null);
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}

	
	/****************************************************************************************
	 * ACTION CACHE TABLE
	 ****************************************************************************************/
	
	/**
	 * This method will add an Action record to the cache.  The input string
	 * represents a database action to be performed later.  This method will NOT
	 * associate a post action string with the cached action.
	 * The addActionCache(CacheAction) function should be used instead of this one.
	 * @param action The string representation of a pending database operation
	 * @return The row ID of the inserted Action cache records is returned
	 */
	public long addActionCache(String handlerName, String action) {
		CacheAction cacheAction;
		cacheAction = new CacheAction(handlerName, action);
		return addActionCache(cacheAction);
	}

	/**
	 * This method will add an Action record to the cache.  The input string
	 * represents a database action to be performed later.
	 * @param action The CacheAction object representing the action to be cached
	 * @return The row ID of the inserted Action cache records is returned
	 */
	public long addActionCache(CacheAction action) {
		ContentValues values = new ContentValues();
	
		values.put(ACTION_ACTION_FIELD, action.actionParams);
		long lngDt = getUpdateTime();
		values.put(ACTION_TIME_FIELD, lngDt);
		if (action.postActions != null)
			values.put(ACTION_POSTACTIONS_FIELD, action.postActions);

		return addActionCache(values);
	}
	
	/**
	 * Private method to generically handle the adding of Action Cache
	 * records.
	 * @param values The ContentValues objects for the fields of the new record
	 * @return The index of the new record is returned.
	 */
	private long addActionCache(ContentValues values) {
		long row;
		
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			
			try {
				row = db.insertOrThrow(ACTION_TABLE_NAME, null, values);
			} catch (Exception e) {
				e.printStackTrace();
				row = -1;
			}
			db.close();
        }
		return row;
	}

	public boolean modifyActionCache(CacheAction action) {
		int rows = 0;
		ContentValues values = new ContentValues();
		values.put(ACTION_ACTION_FIELD, action.actionParams);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			String whereClause;
				
			whereClause = _ID + " = " + action.id;
			rows = db.update(ACTION_TABLE_NAME, values, whereClause, null);
			db.close();
		}
		return rows > 0;
	}

	public int getActionCacheCount() {
		int cnt;
		synchronized (dbLock) {
		    String countQuery = "SELECT  * FROM " + ACTION_TABLE_NAME;
		    SQLiteDatabase db = this.getReadableDatabase();
		    Cursor cursor = db.rawQuery(countQuery, null);
		    cnt = cursor.getCount();
		    cursor.close();
		}
	    return cnt;
	}

	// TODO: This needs to be modified to ONLY get one record from the DB
	public CacheAction getFirstActionCache() {
		CacheAction retCacheAction;
		String [] DEFAULTS_FROM = { 
				_ID, 
				"min(" + ACTION_TIME_FIELD + ")",
				ACTION_ACTION_FIELD,
				ACTION_POSTACTIONS_FIELD,
		};
		
		if (getActionCacheCount() == 0)
			return null;
				
//		String orderby = ACTION_TIME_FIELD + " ASC";
		Cursor cursor;
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			cursor = db.query(ACTION_TABLE_NAME, DEFAULTS_FROM, null, null, null, null, null);  

			m_thisActivity.startManagingCursor(cursor);
		
			if (cursor.getCount() > 0 && cursor.moveToNext()) {
				retCacheAction = new CacheAction(cursor.getString(2), cursor.getLong(0));
				retCacheAction.postActions = cursor.getString(3);
			} else {
				retCacheAction = null;
			}
			
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}		
		return retCacheAction;
	}


	/**
	 * This method will retrieve all of the Action Cache records that are currently
	 * pending.  The returned array contains the Action string as well as the ID of
	 * the Action Cache records (which can be used to remove the record).
	 * @return An array of ActionCache
	 */
	public CacheAction [] getAllActionCache() {
		CacheAction [] retArray;
		String [] DEFAULTS_FROM = { 
				_ID, 
				ACTION_TIME_FIELD,
				ACTION_ACTION_FIELD,
				ACTION_POSTACTIONS_FIELD,
		};
				
		String orderby = ACTION_TIME_FIELD + " ASC";
		Cursor cursor;
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			cursor = db.query(ACTION_TABLE_NAME, DEFAULTS_FROM, null, null, null, null, orderby);

			m_thisActivity.startManagingCursor(cursor);
		
			// Allocate the return array of filename strings
			retArray = new CacheAction[cursor.getCount()];
			int i = 0;
			while (cursor.moveToNext()) {
				retArray[i] = new CacheAction( cursor.getString(2), cursor.getLong(0));
				retArray[i].postActions = cursor.getString(3);
				i++;
			}
			
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}		
		return retArray;
	}

	/**
	 * This method will delete the Action Cache records with the specific ID
	 * @param id The Action Cache record id to delete
	 * @return Returns true if the record is deleted, otherwise false
	 */
	public boolean deleteActionCache(long id) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = _ID + " = " + id;
			numRows = db.delete(ACTION_TABLE_NAME, whereClause, null);
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}

	/**
	 * This method will delete all Action cache records from the database.
	 */
	public void deleteAllActionCache() {
		synchronized (dbLock) {
			SQLiteDatabase db = this.getWritableDatabase();
			db.delete(ACTION_TABLE_NAME, null, null);
			db.close();
		}
	}

	/****************************************************************************************
	 * CACHE REFERENCE TABLE
	 * Used to cache images (references to)
	 ****************************************************************************************/
   
	/**
	 * This method adds a Cache Reference entry into the Cache Database
	 * @param key this is the key that will be used to reference this record
	 * @param fileName this is the filename associated with the cache reference
	 * @return return true if successfully added
	 */
	public long addCacheReference(String key, String fileName ) {
		long row;

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
	
			values.put(CACHEREF_KEY_FIELD, key);
			values.put(CACHEREF_FILENAME_FIELD, fileName);
			long lngDt = getUpdateTime();
			values.put(CACHEREF_UPDATETIME_FIELD, lngDt);
			
			try {
				row = db.insertOrThrow(CACHEREF_TABLE_NAME, null, values);
			} catch (Exception e) {
				e.printStackTrace();
				row = -1;
			}
			db.close();
        }
		return row;
	}

	/**
	 * This method will update the key field for the cache reference table.
	 * @param key this is the key that will be used to reference this record
	 * @param newKey the new value for the key field
	 * @return return true if successfully added
	 */
	public boolean updateCacheReferenceKey(String key, String newKey ) {
		boolean retValue = false;

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
	
			values.put(CACHEREF_KEY_FIELD, newKey);
			
			String whereClause;
			whereClause = CACHEREF_KEY_FIELD + " = \"" + key + "\"";

			try {
				int rows = db.update(CACHEREF_TABLE_NAME, values, whereClause, null);
				if (rows > 0)
					retValue = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			db.close();
        }
		return retValue;
	}

	/**
	 * Get the file name that is referenced by the input key value.
	 * @param key The key to use to retrieve the file name
	 * @return The file name is returned, or null if the record does not exist
	 */
	public String getCacheReferenceFileName(String key) {
		String retStr = null;
		String[] DEFAULTS_FROM = { 
				_ID, 
				CACHEREF_FILENAME_FIELD, 
				CACHEREF_UPDATETIME_FIELD,
			};

		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = CACHEREF_KEY_FIELD + " = \"" + key + "\"";
			Cursor cursor = db.query(CACHEREF_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			m_thisActivity.startManagingCursor(cursor);
	
			if (cursor.moveToNext()) {
				retStr = cursor.getString(1);
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}
		
		// Update the access time for the record
		updateCacheReference(key);

		return retStr;
	}
	
	/**
	 * This is a diagnostic method which returns an array of all of the Cache
	 * Reference records.
	 * @return Returns an array of Cahce Reference records
	 */
	public CacheRef [] getAllCacheReferenceRecords() {
		CacheRef [] retArray;
		String[] DEFAULTS_FROM = { 
				_ID, 
				CACHEREF_KEY_FIELD,
				CACHEREF_FILENAME_FIELD, 
				CACHEREF_UPDATETIME_FIELD,
			};
		String orderby = CACHEREF_UPDATETIME_FIELD + " ASC";
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			Cursor cursor = db.query(CACHEREF_TABLE_NAME, DEFAULTS_FROM, null, null, null, null, orderby);
			m_thisActivity.startManagingCursor(cursor);
	
			// Allocate the return array of filename strings
			retArray = new CacheRef[cursor.getCount()];
			int i = 0;
			while (cursor.moveToNext()) {
				retArray[i] = new CacheRef();
				retArray[i].key = cursor.getString(1);
				retArray[i].filename = cursor.getString(2);
				retArray[i++].updateTime = cursor.getLong(3);
			}
			
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}		
		return retArray;
	}
	
	/**
	 * This public class is used to return cached image information.
	 * This class contains the key for the image, the file name of the image
	 * and the current update time for the image cache record.
	 * 
	 * @author Paul Cushman
	 *
	 */
	public class CacheRef {
		String key;
		String filename;
		long updateTime;
	}

	/**
	 * Modify the record identified by the input key.  The input filename is put
	 * into the record and the reference time is updated.
	 * @param key The key associated with the input filename
	 * @param fileName The file name to save in the database
	 * @return Returns true if the record is found and updated.
	 */
	public boolean modifyCacheReferenceFileName( String key, String fileName ) {
		ContentValues values = new ContentValues();
		values.put(CACHEREF_FILENAME_FIELD, fileName);

		String whereClause = CACHEREF_KEY_FIELD + " = \"" + key + "\"";
		return updateRecord(CACHEREF_TABLE_NAME, whereClause, values);
	}

	
	/**
	 * This method will update the updateTime field of the associated cache
	 * reference record.
	 * @param key The key associated with the record to be updated.
	 * @return Returns true of the record is updated, false if not
	 */
	public boolean updateCacheReference(String key) {
		String whereClause = CACHEREF_KEY_FIELD + " = \"" + key + "\"";
		return updateRecord(CACHEREF_TABLE_NAME, whereClause, null);
	}
	
	/**
	 * Delete the specific cache reference record.
	 * @param key The key for the record to delete.
	 * @return Returns true if the record is deleted, false if no record is found
	 */
	public boolean deleteCacheReference( String key ) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = CACHEREF_KEY_FIELD + " = \"" + key + "\"";
			numRows = db.delete(CACHEREF_TABLE_NAME, whereClause, null);
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}

	/**
	 * This method will delete all cache reference records from the database.
	 */
	public void deleteAllCacheReferences() {
		synchronized (dbLock) {
			SQLiteDatabase db = this.getWritableDatabase();
			deleteAllCacheReferences(db);
			db.close();
		}
	}

	/**
	 * This is a private method which is called by the public deleteAllCacheReferences
	 * methods to perform the actual cache reference deletions.
	 * @param db This is an open database.
	 */
	private void deleteAllCacheReferences(SQLiteDatabase db) {
		String[] DEFAULTS_FROM = { _ID, CACHEREF_FILENAME_FIELD, };

		synchronized (dbLock) {
			// Get all records
			Cursor cursor = db.query(CACHEREF_TABLE_NAME, DEFAULTS_FROM, null, null, null, null, null);
	
			m_thisActivity.startManagingCursor(cursor);
			
			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					String fileName = cursor.getString(1);
					
					if (m_thisActivity.deleteFile(fileName))
						Log.d(TAG, "deleteAllCacheReferences: deleted "+fileName);
					else
						Log.d(TAG, "deleteAllCacheReferences: could not delete "+fileName);
				}
			}
			
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
	
			// Delete all of the Cache Reference Table records
			db.delete(CACHEREF_TABLE_NAME, null, null);
		}
	}

	/****************************************************************************************
	 * ACCOUNT TABLE 
	 ****************************************************************************************/

	/**
	 * This method will add an Account record.  The input object can be either a UserAccount
	 * object or a MyUserAccount object.  If the object is an instance of a MyUserAccount
	 * object then the password field will be saved in the cache database.
	 * @param acctObject Instance of a UserAccount or MyUserAccount class
	 * @return Returns true if successfully added to the cache, false if not added
	 */
	public boolean addAccount(Object acctObject) {
		long id;
		
		UserAccount ua = (UserAccount)acctObject;
		
		ContentValues values = new ContentValues();
		values.put(ACCOUNT_ID_FIELD, ua.getId());
		values.put(ACCOUNT_USERNAME_FIELD, ua.getuName());
		values.put(ACCOUNT_NAME_FIELD, ua.getName());
		values.put(ACCOUNT_EMAILADDRESS_FIELD, ua.getEmail());
		if (acctObject instanceof MyUserAccount) {
			MyUserAccount mua = (MyUserAccount)acctObject;
			values.put(ACCOUNT_PASSWORD_FIELD, mua.getPass());
		}
		values.put(ACCOUNT_IMAGE_FIELD, ua.getImage());
		values.put(ACCOUNT_DESCRIPTION_FIELD, ua.getDescription());
		values.put(ACCOUNT_LOCATION_FIELD, ua.getLocation());
		values.put(ACCOUNT_QUOTE_FIELD, ua.getQuote());
		values.put(ACCOUNT_TYPE_FIELD, ua.getType());
		values.put(ACCOUNT_VISIBILITY_FIELD, ua.getVisibility());
		Date dt = ua.getCreatedDateTime();
		long lngDt = dt.getTime();
		values.put(ACCOUNT_CREATEDDATETIME_FIELD, lngDt);
		values.put(ACCOUNT_RATINGSCORE_FIELD, ua.getUserRating());
		lngDt = getUpdateTime();
		values.put(ACCOUNT_UPDATETIME_FIELD, lngDt);
		
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			try {
				id = db.insertOrThrow(ACCOUNT_TABLE_NAME, null, values);
			} catch (SQLException e) {
				Log.d(TAG, "addAccount got SQLException! "+e.getMessage());
				id = -1;
			}
			
			// If the id is -1 then the insert failed.  Try to update the record instead
			if (id == -1) {
				String whereClause;
				
				whereClause = ACCOUNT_ID_FIELD + " = " + ua.getId();
				int rows = db.update(ACCOUNT_TABLE_NAME, values, whereClause, null);
				if (rows > 0)
					id = 1;
			}
			db.close();
		}
		return id >= 0;
	}
	
	/**
	 * This method will return a MyUserAccount from the cache database, using the
	 * User's ID and Password fields
	 * @param id The ID of the user account record to retrieve
	 * @param password The Password value of the account record to retrieve
	 * @return The MyUserAccount that was retrieved, or null if not found
	 */
	public MyUserAccount getMyUserAccount(int id, String password) {
		String selString = ACCOUNT_ID_FIELD + " = " + id;
		// TODO: Figure out how to handle password
//		String selString = ACCOUNT_ID_FIELD + " = " + id + " and " + 
//							ACCOUNT_PASSWORD_FIELD + " = \"" + password + "\"";
		Object acctObj = getAccountWithKey(selString, true);
		return (MyUserAccount)acctObj;
	}

	/**
	 * This method will return a MyUserAccount from the cache database, using the
	 * User's name and Password fields
	 * @param username The name of the user account record to retrieve
	 * @param password The Password value of the account record to retrieve
	 * @return The MyUserAccount that was retrieved, or null if not found
	 */
	public MyUserAccount getMyUserAccount(String username, String password) {
		String selString = ACCOUNT_USERNAME_FIELD + " = \"" + username + "\""; 
		// TODO: Figure out how to handle password
//		String selString = ACCOUNT_USERNAME_FIELD + " = \"" + username + "\" and " + 
//				ACCOUNT_PASSWORD_FIELD + " = \"" + password + "\"";
		Object acctObj = getAccountWithKey(selString, true);
		return (MyUserAccount)acctObj;
	}

	public UserAccount [] getAdventureMembers(long advID) {
		UserAccount [] members = null;
		
		Long [] memberIds;
		
		memberIds = getAdventureMemberIds(advID);
		if (memberIds == null || memberIds.length == 0)
			return null;
		
		// Allocate an array of Members/UserAccount objects
		members = new UserAccount [memberIds.length];
		
		for (int i=0; i<memberIds.length; i++) {
			members[i] = getAccount(memberIds[i]);
		}
		return members;
	}

	/**
	 * This method will retrieve a list of Adventures that the
	 * input user ID is a meber of.
	 * @param memberID The user ID to search with
	 * @return Array of Adventure objects
	 */
	public Adventure [] getMemberAdventures(long memberID) {
			Adventure [] adventures = null;
			Long [] adventureIds;
			
			adventureIds = getMemberAdventureIds(memberID);
			if (adventureIds == null || adventureIds.length == 0)
				return null;
			
			// Allocate an array of Adventures
			adventures = new Adventure [adventureIds.length];
			
			for (int i=0; i<adventureIds.length; i++) {
				adventures[i] = this.getAdventure(adventureIds[i]);
			}
			return adventures;
	}

	/**
	 * This method will return the UserAccount class object from the cache database
	 * that has the input account ID value.
	 * @param id The value of the account ID to search for
	 * @return Returns the associated UserAccount class object retrieved
	 */
	public UserAccount getAccount(long id) {
		String selString = ACCOUNT_ID_FIELD + " = " + id;
		Object acctObj = getAccountWithKey(selString, false);
		return (UserAccount)acctObj;
	}
	
	/**
	 * This method will return the UserAccount class object from the cache database
	 * that has the input user name
	 * @param id The value of the user name to search for
	 * @return Returns the associated UserAccount class object retrieved
	 */
	public UserAccount getAccount(String username) {
		String selString = ACCOUNT_USERNAME_FIELD + " = \"" + username + "\"";
		Object acctObj = getAccountWithKey(selString, false);
		return (UserAccount)acctObj;
	}

	/**
	 * This private method performs the actual cache database search associated with
	 * the public getAccount() methods.  This method will return a MyUserAccount class object
	 * if that is requested.
	 * @param selString The select search string to use for the SQL select query
	 * @param myUser Whether a MyUserAccount record should be returned, false if UserAccount
	 * @return If found a MyUserAccount or UserAccount record is returned, if not found then null
	 */
	private Object getAccountWithKey(String selString, boolean myUser) {
		long recID = -1;
		UserAccount ua = null;
		MyUserAccount mua = null;
		String[] DEFAULTS_FROM = {
				_ID,
				ACCOUNT_ID_FIELD,
				ACCOUNT_USERNAME_FIELD,
				ACCOUNT_NAME_FIELD,
				ACCOUNT_EMAILADDRESS_FIELD,
				ACCOUNT_IMAGE_FIELD,
				ACCOUNT_DESCRIPTION_FIELD,
				ACCOUNT_LOCATION_FIELD,
				ACCOUNT_QUOTE_FIELD,
				ACCOUNT_TYPE_FIELD,
				ACCOUNT_VISIBILITY_FIELD,
				ACCOUNT_CREATEDDATETIME_FIELD,
				ACCOUNT_RATINGSCORE_FIELD,
				ACCOUNT_PASSWORD_FIELD, };

		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			Cursor cursor = db.query(ACCOUNT_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			m_thisActivity.startManagingCursor(cursor);
	
			if (cursor.moveToNext()) {
				recID = cursor.getLong(0);
				int id = cursor.getInt(1);
				String username = cursor.getString(2);
				String email = cursor.getString(4);
				String img = cursor.getString(5);
				String desc = cursor.getString(6);
				String loc = cursor.getString(7);
				String quote = cursor.getString(8);
				int type = cursor.getInt(9);
				int vis = cursor.getInt(10);
				
				long createddate = cursor.getLong(11);
				Date ts = new Date(createddate);
	
				int rating = cursor.getInt(12);
				
				if (myUser) {
					String password = cursor.getString(13);
					mua = new MyUserAccount(id, username, email, password, img, desc, loc, quote, type, vis, ts, rating);
					mua.setName(cursor.getString(3));
				} else {
					ua = new UserAccount(id, username, email, img, desc, loc, quote, type, vis, ts, rating);
					ua.setName(cursor.getString(3));
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}
		
		// If the record was found then update the updatetime for the record
		if (recID != -1) {
			String where = _ID + " = " + recID;
			updateRecord(ACCOUNT_TABLE_NAME, where, null);
		}
		
		if (myUser)
			return mua;
		else
			return ua;
	}

	/**
	 * This method will return the account ID that is associated with the Account
	 * record with the input user name.
	 * @param username The username to search for
	 * @return A long value account ID
	 */
	public long getAccountID(String username) {
		long recID = -1;
		long retID = -1;
		String[] DEFAULTS_FROM = {
				_ID,
				ACCOUNT_ID_FIELD, };
		String selString = ACCOUNT_USERNAME_FIELD + " = \"" + username + "\"";

		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			Cursor cursor = db.query(ACCOUNT_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			m_thisActivity.startManagingCursor(cursor);
	
			if (cursor.moveToNext()) {
				recID = cursor.getLong(0);
				retID = cursor.getLong(1);
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}
		
		// If the record was found then update the updatetime for the record
		if (recID != -1) {
			String where = _ID + " = " + recID;
			updateRecord(ACCOUNT_TABLE_NAME, where, null);
		}
		return retID;
	}


	/**
	 * Get the Username field from the account record that contains the input ID
	 * @param id The ID of the record to retrieve
	 * @return The Username, or null if not found
	 */
	public String getUsernameFromAccount(int id) {
		long recID = -1;
		String username = null;
		
		String[] DEFAULTS_FROM = {
				_ID,
				ACCOUNT_USERNAME_FIELD, };

		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = ACCOUNT_ID_FIELD + " = " + id;
			Cursor cursor = db.query(ACCOUNT_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			m_thisActivity.startManagingCursor(cursor);
	
			if (cursor.moveToNext()) {
				recID = cursor.getLong(0);
				username = cursor.getString(1);
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		// If the record was found then update the updatetime for the record
		if (recID != -1) {
			String where = _ID + " = " + recID;
			updateRecord(ACCOUNT_TABLE_NAME, where, null);
		}

		return username;
	}

	
	/****************************************************************************************
	 * FRIEND ASSOCIATION TABLE 
	 ****************************************************************************************/

	/**
	 * This method will add a Friend record to the Geotagger cache database.  If the
	 * record already exists a false value will be returned.
	 * @param fId The ID of the friend's Account record
	 * @param uId The ID of the user's Account record
	 * @return Returns true if added, false if it already exists or an error occurred
	 */
	public boolean addFriend(long uId, long fId) {
		long id;
		
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(FRIEND_FID_FIELD, fId);
			values.put(FRIEND_UID_FIELD, uId);
			
			try {
				id = db.insertOrThrow(FRIEND_TABLE_NAME, null, values);
			} catch (SQLException e) {
				id = -1;
			}
			db.close();
		}
		return id >= 0;
	}
	
	/**
	 * This method will delete a Friend record.
	 * @param fId The friend ID
	 * @param uId the User ID
	 * @return True if a record is removed, false otherwise
	 */
	public boolean deleteFriend(long fId, long uId) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = FRIEND_FID_FIELD + " = " + fId + " and " +
								FRIEND_UID_FIELD + " = " + uId;
			
			numRows = db.delete(FRIEND_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}
	
	/**
	 * This method will get a list of friends associated with the input user ID
	 * @param uId The Account ID for the user, to search with
	 * @return Returns an array of UserAccount class objects
	 */
	public UserAccount [] getFriends(long uId) {
		long fID = -1;
		long [] friendIDs = null;
		UserAccount [] friends = null;
		int numRecords = 0;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				FRIEND_FID_FIELD, };

		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = FRIEND_UID_FIELD + " = " + uId;
			cursor = db.query(FRIEND_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			numRecords = cursor.getCount();
			if (numRecords >= 0) {
				friendIDs = new long[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
					fID = cursor.getLong(1);
					friendIDs[i] = fID;
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		// Get the UserAccount records for all of the friends
		friends = new UserAccount[numRecords];
		for (int i=0; i<numRecords; i++) {
			friends[i] = getAccount(friendIDs[i]);
		}

		return friends;
	}

	public Friend [] getAllFriends() {
		Friend [] friends = null;
		int numRecords = 0;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				FRIEND_UID_FIELD,
				FRIEND_FID_FIELD, };

		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			cursor = db.query(FRIEND_TABLE_NAME, DEFAULTS_FROM, null, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			numRecords = cursor.getCount();
			if (numRecords >= 0) {
				friends = new Friend[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long fID = cursor.getLong(2);
					long uID = cursor.getLong(1);
					
					friends[i] = new Friend(uID, fID);
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return friends;
	}


	/****************************************************************************************
	 * TAG COMMENT TABLE 
	 ****************************************************************************************/

	/**
	 * This method will add a Comment class object to the Geotagger Cache database.  If the
	 * add fails, meaning the record already exists, then the existing record will be updated.
	 * The update time of the existing record will be updated.
	 * @param comment The Comment class object to add to the cache
	 * @return Returns true if successfully added, false if failed.
	 */
	public boolean addTagComment(Comment comment) {
		long id;
		
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(TAGCOMMENT_COMMENTID_FIELD, comment.getId());
			values.put(TAGCOMMENT_TAGID_FIELD, comment.getParentTagId());
			values.put(TAGCOMMENT_COMMENT_FIELD, comment.getText());
			values.put(TAGCOMMENT_UNAME_FIELD, comment.getUsername());
			String imageURL = comment.getImageURL();
			if (imageURL == null || imageURL.length() == 0) {
				File imageFile = comment.getImageUploadFile();
				if (imageFile != null) {
					String imagePath = imageFile.getPath();
					values.put(TAGCOMMENT_IMGURL_FIELD, imagePath);
				}
			} else {
				values.put(TAGCOMMENT_IMGURL_FIELD, imageURL);
			}
			long lngDt = getUpdateTime();
			values.put(TAGCOMMENT_UPDATETIME_FIELD, lngDt);
			
			try {
				id = db.insertOrThrow(TAGCOMMENT_TABLE_NAME, null, values);
			} catch (Exception e) {
	//			Log.d(TAG, "addTagComment got SQLException! "+e.getMessage());
				id = -1;
			}
			
			// If the id is -1 then the insert failed.  Try to update the record instead
			if (id == -1) {
				String whereClause;
				
				whereClause = TAGS_TAGID_FIELD + " = " + comment.getId();
				int rows;
				try {
					rows = db.update(TAGCOMMENT_TABLE_NAME, values, whereClause, null);
				} catch (Exception e) {
					Log.d(TAG, "addTagComment got SQLException! "+e.getMessage());
					rows = 0;
					id = -1;
				}
				if (rows > 0)
					id = 1;
				else {
					Log.e(TAG, "addTagComment could NOT add or update record");
				}
			}
			db.close();
		}
		return id >= 0;
	}
	
	public boolean updateTagCommentID(long curCommentID, long newCommentID) {
		boolean retValue;
		ContentValues values = new ContentValues();
		values.put(TAGCOMMENT_COMMENTID_FIELD, newCommentID);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			String whereClause;
				
			whereClause = TAGCOMMENT_COMMENTID_FIELD + " = " + curCommentID;
			int rows = db.update(TAGCOMMENT_TABLE_NAME, values, whereClause, null);
			if (rows > 0)
				retValue = true;
			else {
				Log.e(TAG, "updateTagCommentID: could not modify the commentID!");
				retValue = false;
			}
			db.close();
		}
		return retValue;
	}


	/**
	 * Delete a specific Tag Comment, using the input commentID
	 * @param tagID tagID of the tag to remove
	 * @return true if successful, false oherwise
	 */
	public boolean deleteTagComment(long commentID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = TAGCOMMENT_COMMENTID_FIELD + " = " + commentID;
			
			numRows = db.delete(TAGCOMMENT_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}
	
	/**
	 * This method will get a list of Comment associated with a specific Tag.
	 * @param tagID The ID of the Tag to use to retrieve the comments
	 * @return Return an array of Comment class objects
	 */
	public Comment [] getTagComments(long tagID) {
		long recID = -1;
		Comment comment = null;
		Comment [] comments = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				TAGCOMMENT_COMMENTID_FIELD,
				TAGCOMMENT_TAGID_FIELD,
				TAGCOMMENT_COMMENT_FIELD,
				TAGCOMMENT_UNAME_FIELD,
				TAGCOMMENT_IMGURL_FIELD,
				TAGCOMMENT_UPDATETIME_FIELD, };

		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = TAGCOMMENT_TAGID_FIELD + " = " + tagID;
			cursor = db.query(TAGCOMMENT_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				comments = new Comment[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					recID = cursor.getLong(0);
					int commentID = cursor.getInt(1);
					String text = cursor.getString(3);
					String uname = cursor.getString(4);
					String img = cursor.getString(5);
		
					long updateTime = cursor.getLong(6);
					Date dt = new Date(updateTime);
		
					comment = new Comment(commentID, tagID, text, uname, dt, img);
					comments[i] = comment;
					
					// If the record was found then update the updatetime for the record
					if (recID != -1) {
						String where = _ID + " = " + recID;
						updateRecord(TAGCOMMENT_TABLE_NAME, where, null);
					}
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return comments;
	}

	
	/**
	 * This method will return the next "cache" id to use for the Tag Comment
	 * record type.  Cache IDs are negative values and start at CACHE_ID_START
	 * and decrease from there.
	 * @return A value of CACHE_ID_START and lower is returned
	 */
	public long getnextTagCommentCacheID() {
		long id;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			Cursor c = db.query(TAGCOMMENT_TABLE_NAME, new String[] { "min(" + TAGCOMMENT_COMMENTID_FIELD + ")" }, null, null, null, null, null);  
			c.moveToFirst();  //ADD THIS!
			id = c.getLong(0);
			
			db.close();
		}
		if (id > CACHE_ID_START)
			return CACHE_ID_START;
		return id-1;
	}

	/**
	 * This method will change the ImageURL specified by the oldImageUrl to the
	 * value of the newImageUrl
	 * TODO: This may be slow if an index is not used.
	 * @param oldImageUrl
	 * @param newImageUrl
	 * @return true is returned if a value was changed.
	 */
	public boolean updateTagCommentImageUrl(String oldImageUrl, String newImageUrl) {
		boolean retValue = false;

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
	
			values.put(TAGCOMMENT_IMGURL_FIELD, newImageUrl);
			
			String whereClause;
			whereClause = TAGCOMMENT_IMGURL_FIELD + " = \"" + oldImageUrl + "\"";

			try {
				int rows = db.update(TAGCOMMENT_TABLE_NAME, values, whereClause, null);
				if (rows > 0)
					retValue = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			db.close();
        }
		return retValue;
	}


	/****************************************************************************************
	 * TAGS TABLE 
	 ****************************************************************************************/

	/**
	 * This method will add a Tag record into the Geotagger cache database.  If the add
	 * fails, the record already exists, in which case the existing record will be updated.
	 * The update time will be updated.
	 * @param tag The Tag class object to add into the database
	 * @return Returns true if successfully added, false if not added
	 */
	public boolean addTag(Tag tag) {
		long id;
		
		ContentValues values = new ContentValues();
		values.put(TAGS_TAGID_FIELD, tag.getId());  
		values.put(TAGS_OWNERID_FIELD, tag.getOwnerId());
		values.put(TAGS_VISIBILITY_FIELD, tag.getVisibility());
		values.put(TAGS_NAME_FIELD, tag.getName());
		values.put(TAGS_DESCRIPTION_FIELD, tag.getDescription());
		values.put(TAGS_IMAGEURL_FIELD, tag.getImageUrl());
		values.put(TAGS_LOCATION_FIELD, tag.getLocationString());
		GeoLocation loc = tag.getLocation();
		double lat = 0;
		double lon = 0;
		if (loc != null) {
			lat = loc.getLatitude();
			lon = loc.getLongitude();
		}
		values.put(TAGS_LATITUDE_FIELD, lat);
		values.put(TAGS_LONGITUDE_FIELD, lon);

		Date dt = tag.getCreatedDateTime();
		if (dt != null) {
			long lngDt = dt.getTime();
			values.put(TAGS_CREATEDDATETIME_FIELD, lngDt);
		}

		values.put(TAGS_CATEGORY_FIELD, tag.getCategory());
		values.put(TAGS_RATINGSCORE_FIELD, tag.getRatingScore());
		
		long lngDt = getUpdateTime();
		values.put(TAGS_UPDATETIME_FIELD, lngDt);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			try {
				id = db.insertOrThrow(TAGS_TABLE_NAME, null, values);
			} catch (SQLException e) {
	//			Log.d(TAG, "addTag got SQLException! "+e.getMessage());
				id = -1;
			}
			
			// If the id is -1 then the insert failed.  Try to update the record instead
			if (id == -1) {
				String whereClause;
				
				whereClause = TAGS_TAGID_FIELD + " = " + tag.getId();
				int rows = db.update(TAGS_TABLE_NAME, values, whereClause, null);
				if (rows > 0)
					id = 1;
				else {
					Log.e(TAG, "addTag could NOT add or update record");
				}
			}
			db.close();
		}
		return id >= 0;
	}
	
	/**
	 * This method will update a Tag record in the database.
	 * @param tag The Tag object used to update the record.
	 * @return True is returned if successufl
	 */
	public boolean editTag(Tag tag) {
		long id;
		
		ContentValues values = new ContentValues();
//		values.put(TAGS_TAGID_FIELD, tag.getId());  
		values.put(TAGS_OWNERID_FIELD, tag.getOwnerId());
		values.put(TAGS_VISIBILITY_FIELD, tag.getVisibility());
		values.put(TAGS_NAME_FIELD, tag.getName());
		values.put(TAGS_DESCRIPTION_FIELD, tag.getDescription());
		values.put(TAGS_IMAGEURL_FIELD, tag.getImageUrl());
		values.put(TAGS_LOCATION_FIELD, tag.getLocationString());
		GeoLocation loc = tag.getLocation();
		double lat = 0;
		double lon = 0;
		if (loc != null) {
			lat = loc.getLatitude();
			lon = loc.getLongitude();
		}
		values.put(TAGS_LATITUDE_FIELD, lat);
		values.put(TAGS_LONGITUDE_FIELD, lon);
		values.put(TAGS_CATEGORY_FIELD, tag.getCategory());
		values.put(TAGS_RATINGSCORE_FIELD, tag.getRatingScore());
		
		long lngDt = getUpdateTime();
		values.put(TAGS_UPDATETIME_FIELD, lngDt);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			String whereClause = TAGS_TAGID_FIELD + " = " + tag.getId();
			int rows = db.update(TAGS_TABLE_NAME, values, whereClause, null);
			if (rows > 0)
				id = 1;
			else {
				Log.e(TAG, "editTag could NOT update record");
				id = 0;
			}
			db.close();
		}
		return id >= 0;
	}
	
	public boolean updateTagID(long curTagID, long newTagID) {
		boolean retValue;
		ContentValues values = new ContentValues();
		values.put(TAGS_TAGID_FIELD, newTagID);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			String whereClause;
				
			whereClause = TAGS_TAGID_FIELD + " = " + curTagID;
			int rows = db.update(TAGS_TABLE_NAME, values, whereClause, null);
			if (rows > 0)
				retValue = true;
			else {
				Log.e(TAG, "updateTagID: could not modify the tagID!");
				retValue = false;
			}
			db.close();
		}
		return retValue;
	}

	/**
	 * This method will change the ImageURL specified by the oldImageUrl to the
	 * value of the newImageUrl
	 * TODO: This may be slow if an index is not used.
	 * @param oldImageUrl
	 * @param newImageUrl
	 * @return true is returned if a value was changed.
	 */
	public boolean updateTagImageUrl(String oldImageUrl, String newImageUrl) {
		boolean retValue = false;

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
	
			values.put(TAGS_IMAGEURL_FIELD, newImageUrl);
			
			String whereClause;
			whereClause = TAGS_IMAGEURL_FIELD + " = \"" + oldImageUrl + "\"";

			try {
				int rows = db.update(TAGS_TABLE_NAME, values, whereClause, null);
				if (rows > 0)
					retValue = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			db.close();
        }
		return retValue;
	}
	
	/**
	 * This method will get the Tag class objects associated with a specific Adventure.
	 * 
	 * TODO: Implement me
	 * @param advID The adventure ID associated with the Tags to retrieve
	 * @return Returns an array of Tag class objects
	 */
	public Tag [] getAdventureTags(long advID) {
		Tag [] tags = null;
		
		Long [] tagIds;
		
		tagIds = getAdventureTagIds(advID);
		if (tagIds == null || tagIds.length == 0)
			return null;
		
		int numTags = 0;
		// First confirm that the Tags have valid Tag IDs
		for (int i=0; i<tagIds.length; i++)
			if (tagIds[i] != 0)
				numTags++;
		
		// Allocate an array of tags
		tags = new Tag [numTags];
		int curTag = 0;
		
		for (int i=0; i<tagIds.length; i++) {
			if (tagIds[i] != 0)
				tags[curTag++] = getTag(tagIds[i]);
		}
		
		// Verify that a record was actually returned for all of the TagIDs
		numTags = 0;
		for (int i=0; i<tags.length; i++) {
			if (tags[i] != null)
				numTags++;
		}
		
		if (numTags < tags.length) {
			Tag [] fixTags = new Tag [numTags];
			curTag = 0;
			for (int i=0; i<tags.length; i++) {
				if (tags[i] != null)
					fixTags[curTag++] = tags[i];
			}
			tags = fixTags;
		}
		return tags;
	}

	public Tag getTag(long tagID) {
		Tag tag = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				TAGS_TAGID_FIELD,
				TAGS_OWNERID_FIELD,
				TAGS_VISIBILITY_FIELD,
				TAGS_NAME_FIELD,
				TAGS_DESCRIPTION_FIELD,
				TAGS_IMAGEURL_FIELD,
				TAGS_LOCATION_FIELD,
				TAGS_LATITUDE_FIELD,
				TAGS_LONGITUDE_FIELD,
				TAGS_CREATEDDATETIME_FIELD,
				TAGS_CATEGORY_FIELD,
				TAGS_RATINGSCORE_FIELD, };


		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = TAGS_TAGID_FIELD + " = \"" + tagID + "\"";
			cursor = db.query(TAGS_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords > 0) {
				cursor.moveToPosition(0);
						
				long recID = cursor.getLong(0);
//				int tagID = cursor.getInt(1);
				int owner = cursor.getInt(2);
				int vis = cursor.getInt(3);
				String name = cursor.getString(4);
				String desc = cursor.getString(5);
				String img = cursor.getString(6);
				String locStr = cursor.getString(7);
				double latitude = cursor.getDouble(8);
				double longitude = cursor.getDouble(9);
		
				long createddate = cursor.getLong(10);
				Date ts = new Date(createddate);
						
				String category = cursor.getString(11);
				int rating = cursor.getInt(12);
		
				// format the location field
				GeoLocation loc = new GeoLocation(latitude, longitude);
						
				tag = new Tag(tagID, name, desc, img, locStr, category, rating, owner, loc, vis, ts);
						
				// If the record was found then update the updatetime for the record
				if (recID != -1) {
					String where = _ID + " = " + recID;
					updateRecord(TAGS_TABLE_NAME, where, null);
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return tag;
	}
	
	public Tag [] getTags() {
		Tag tag = null;
		Tag [] tags = null;
		Long [] recIDs = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				TAGS_TAGID_FIELD,
				TAGS_OWNERID_FIELD,
				TAGS_VISIBILITY_FIELD,
				TAGS_NAME_FIELD,
				TAGS_DESCRIPTION_FIELD,
				TAGS_IMAGEURL_FIELD,
				TAGS_LOCATION_FIELD,
				TAGS_LATITUDE_FIELD,
				TAGS_LONGITUDE_FIELD,
				TAGS_CREATEDDATETIME_FIELD,
				TAGS_CATEGORY_FIELD,
				TAGS_RATINGSCORE_FIELD, };


		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			cursor = db.query(TAGS_TABLE_NAME, DEFAULTS_FROM, null, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				tags = new Tag[numRecords];
				recIDs = new Long[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					int tagID = cursor.getInt(1);
					int owner = cursor.getInt(2);
					int vis = cursor.getInt(3);
					String name = cursor.getString(4);
					String desc = cursor.getString(5);
					String img = cursor.getString(6);
					double latitude = cursor.getDouble(7);
					double longitude = cursor.getDouble(8);
		
					long createddate = cursor.getLong(9);
					Date ts = new Date(createddate);
						
					String category = cursor.getString(10);
					int rating = cursor.getInt(12);
		
					// format the location field
					GeoLocation loc = new GeoLocation(latitude, longitude);
						
					tag = new Tag(tagID, name, desc, img, loc.toString(), category, rating, owner, loc, vis, ts);
					tags[i] = tag;
					recIDs[i] = recID;
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}
		
		// Update the updatetime for the retrieve records
		for (Long id : recIDs) {
			// If the record was found then update the updatetime for the record
			if (id != -1) {
				String where = _ID + " = " + id;
				updateRecord(TAGS_TABLE_NAME, where, null);
			}
		}

		return tags;
	}
	


	/**
	 * This method will retrieve a list of Tag class objects associated with a specific
	 * owner ID.
	 * @param ownerID The owner ID to use with the query
	 * @return Returns an array of Tag class objects
	 */
	public Tag [] getTags(long ownerID) {
		Tag tag = null;
		Tag [] tags = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				TAGS_TAGID_FIELD,
				TAGS_OWNERID_FIELD,
				TAGS_VISIBILITY_FIELD,
				TAGS_NAME_FIELD,
				TAGS_DESCRIPTION_FIELD,
				TAGS_IMAGEURL_FIELD,
				TAGS_LOCATION_FIELD,
				TAGS_LATITUDE_FIELD,
				TAGS_LONGITUDE_FIELD,
				TAGS_CREATEDDATETIME_FIELD,
				TAGS_CATEGORY_FIELD,
				TAGS_RATINGSCORE_FIELD, };


		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = TAGS_OWNERID_FIELD + " = \"" + ownerID + "\"";
			cursor = db.query(TAGS_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				tags = new Tag[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long tagID = cursor.getLong(1);
					long owner = cursor.getLong(2);
					int vis = cursor.getInt(3);
					String name = cursor.getString(4);
					String desc = cursor.getString(5);
					String img = cursor.getString(6);
					double latitude = cursor.getDouble(7);
					double longitude = cursor.getDouble(8);
		
					long createddate = cursor.getLong(9);
					Date ts = new Date(createddate);
						
					String category = cursor.getString(10);
					int rating = cursor.getInt(12);
		
					// format the location field
					GeoLocation loc = new GeoLocation(latitude, longitude);
						
					tag = new Tag(tagID, name, desc, img, loc.toString(), category, rating, owner, loc, vis, ts);
					tags[i] = tag;
						
					// If the record was found then update the updatetime for the record
					if (recID != -1) {
						String where = _ID + " = " + recID;
						updateRecord(TAGS_TABLE_NAME, where, null);
					}
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return tags;
	}
	
	/**
	 * Delete a specific tag, using the input tagID
	 * @param tagID tagID of the tag to remove
	 * @return true if successful, false oherwise
	 */
	public boolean deleteTag(long tagID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = TAGS_TAGID_FIELD + " = " + tagID;
			
			numRows = db.delete(TAGS_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}
	
	/**
	 * This method will return the next "cache" id to use for the Tag record type.
	 * Cache IDs are negative values and start at CACHE_ID_START and decrease from there.
	 * @return A value of CACHE_ID_START and lower is returned
	 */
	public long getnextTagCacheID() {
		long id;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			Cursor c = db.query(TAGS_TABLE_NAME, new String[] { "min(" + TAGS_TAGID_FIELD + ")" }, null, null, null, null, null);  
			c.moveToFirst();  //ADD THIS!
			id = c.getLong(0);
			
			db.close();
		}
		if (id > CACHE_ID_START)
			return CACHE_ID_START;
		return id-1;
	}


	
	
	/****************************************************************************************
	 * ADVENTURE TAGS TABLE 
	 ****************************************************************************************/

	public boolean addAdventureTag(long adventureId, long tagId) {
		long id;
		
		ContentValues values = new ContentValues();
		values.put(ADVENTURETAGS_ADVENTUREID_FIELD, adventureId);
		values.put(ADVENTURETAGS_TAGID_FIELD, tagId);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			try {
				id = db.insertOrThrow(ADVENTURETAGS_TABLE_NAME, null, values);
			} catch (SQLException e) {
	//			Log.d(TAG, "addTag got SQLException! "+e.getMessage());
				id = -1;
			}
			
			db.close();
		}
		return id >= 0;
	}
	
	public boolean deleteAdventureTag(long adventureID, long tagID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = ADVENTURETAGS_ADVENTUREID_FIELD + " = " + adventureID + " AND " +
					ADVENTURETAGS_TAGID_FIELD + " = " + tagID;
			
			numRows = db.delete(ADVENTURETAGS_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}
	
	/**
	 * This method will remove all AdventureTags records with the input adventureID
	 * @param adventureID
	 * @return
	 */
	public boolean deleteAdventureTags(long adventureID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = ADVENTURETAGS_ADVENTUREID_FIELD + " = " + adventureID;
			
			numRows = db.delete(ADVENTURETAGS_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}
	
	public AdventureTags [] getAdventureTags() {
		AdventureTags [] retRecs = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				ADVENTURETAGS_ADVENTUREID_FIELD,
				ADVENTURETAGS_TAGID_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			cursor = db.query(ADVENTURETAGS_TABLE_NAME, DEFAULTS_FROM, null, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				retRecs = new AdventureTags[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long adventureID = cursor.getLong(1);
					long tagId = cursor.getLong(2);

					retRecs[i] = new AdventureTags(adventureID, tagId);
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return retRecs;
	}


	
	public Long [] getAdventureTagIds(long adventureId) {
		Long tagId;
		Long [] tagIds = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				ADVENTURETAGS_ADVENTUREID_FIELD,
				ADVENTURETAGS_TAGID_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = ADVENTURETAGS_ADVENTUREID_FIELD + " = \"" + adventureId + "\"";
			cursor = db.query(ADVENTURETAGS_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				tagIds = new Long[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long adventureID = cursor.getLong(1);
					tagId = cursor.getLong(2);

					tagIds[i] = tagId;
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return tagIds;
	}

	
	/****************************************************************************************
	 * ADVENTURE MEMBERS TABLE 
	 ****************************************************************************************/

	public boolean addAdventureMember(long adventureId, long memberId) {
		long id;
		
		ContentValues values = new ContentValues();
		values.put(ADVENTUREMEMBERS_ADVENTUREID_FIELD, adventureId);
		values.put(ADVENTUREMEMBERS_ACCOUNTID_FIELD, memberId);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			try {
				id = db.insertOrThrow(ADVENTUREMEMBERS_TABLE_NAME, null, values);
			} catch (SQLException e) {
	//			Log.d(TAG, "addTag got SQLException! "+e.getMessage());
				id = -1;
			}
			
			db.close();
		}
		return id >= 0;
	}

	public boolean deleteAdventureMember(long adventureID, long memberID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = ADVENTUREMEMBERS_ADVENTUREID_FIELD + " = " + adventureID + " AND " +
					ADVENTUREMEMBERS_ACCOUNTID_FIELD + " = " + memberID;
			
			numRows = db.delete(ADVENTUREMEMBERS_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}

	public boolean deleteAdventureMembers(long adventureID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = ADVENTUREMEMBERS_ADVENTUREID_FIELD + " = " + adventureID;
			
			numRows = db.delete(ADVENTUREMEMBERS_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}

	public AdventureMembers [] getAdventureMembers() {
		AdventureMembers [] retRecs = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				ADVENTUREMEMBERS_ADVENTUREID_FIELD,
				ADVENTUREMEMBERS_ACCOUNTID_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			cursor = db.query(ADVENTUREMEMBERS_TABLE_NAME, DEFAULTS_FROM, null, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				retRecs = new AdventureMembers[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long adventureID = cursor.getLong(1);
					long memberId = cursor.getLong(2);

					retRecs[i] = new AdventureMembers(adventureID, memberId);
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return retRecs;
	}

	public Long [] getAdventureMemberIds(long adventureId) {
		Long memberId;
		Long [] memberIds = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				ADVENTUREMEMBERS_ADVENTUREID_FIELD,
				ADVENTUREMEMBERS_ACCOUNTID_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = ADVENTUREMEMBERS_ADVENTUREID_FIELD + " = \"" + adventureId + "\"";
			cursor = db.query(ADVENTUREMEMBERS_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				memberIds = new Long[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long adventureID = cursor.getLong(1);
					memberId = cursor.getLong(2);

					memberIds[i] = memberId;
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return memberIds;
	}

	/**
	 * This method will return the list of Adventures that
	 * are associated with the input Member ID.
	 * @param memberId User ID for the member to search for
	 * @return List of adventures the input is is a member of
	 */
	public Long [] getMemberAdventureIds(long memberId) {
		Long [] adventureIds = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				ADVENTUREMEMBERS_ADVENTUREID_FIELD,
				ADVENTUREMEMBERS_ACCOUNTID_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = ADVENTUREMEMBERS_ACCOUNTID_FIELD + " = \"" + memberId + "\"";
			cursor = db.query(ADVENTUREMEMBERS_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				adventureIds = new Long[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long adventureID = cursor.getLong(1);

					adventureIds[i] = adventureID;
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return adventureIds;
	}

	
	
	/****************************************************************************************
	 * ADVENTURES TABLE 
	 ****************************************************************************************/

	public boolean addAdventure(Adventure adventure) {
		long id;
		
		ContentValues values = new ContentValues();
		values.put(ADVENTURES_ADVENTUREID_FIELD, adventure.getId());
		values.put(ADVENTURES_OWNERID_FIELD, adventure.getCreatorID());
		values.put(ADVENTURES_VISIBILITY_FIELD, adventure.getVisibility());
		values.put(ADVENTURES_NAME_FIELD, adventure.getName());
		values.put(ADVENTURES_DESCRIPTION_FIELD, adventure.getDescription());

		Date dt = adventure.getCreatedDateTime();
		long lngDt = dt.getTime();
		values.put(ADVENTURES_CREATEDDATETIME_FIELD, lngDt);

		lngDt = getUpdateTime();
		values.put(ADVENTURES_UPDATETIME_FIELD, lngDt);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			try {
				id = db.insertOrThrow(ADVENTURES_TABLE_NAME, null, values);
			} catch (SQLException e) {
	//			Log.d(TAG, "addTag got SQLException! "+e.getMessage());
				id = -1;
			}
			
			// If the id is -1 then the insert failed.  Try to update the record instead
			if (id == -1) {
				String whereClause;
				
				whereClause = ADVENTURES_ADVENTUREID_FIELD + " = " + adventure.getId();
				int rows = db.update(ADVENTURES_TABLE_NAME, values, whereClause, null);
				if (rows > 0)
					id = 1;
				else {
					Log.e(TAG, "addAdventure could NOT add or update record");
				}
			}
			db.close();
		}
		return id >= 0;
	}

	public boolean editAdventure(Adventure adventure) {
		long id;
		
		ContentValues values = new ContentValues();
		values.put(ADVENTURES_OWNERID_FIELD, adventure.getCreatorID());
		values.put(ADVENTURES_VISIBILITY_FIELD, adventure.getVisibility());
		values.put(ADVENTURES_NAME_FIELD, adventure.getName());
		values.put(ADVENTURES_DESCRIPTION_FIELD, adventure.getDescription());

		Date dt = adventure.getCreatedDateTime();
		long lngDt = dt.getTime();
		values.put(ADVENTURES_CREATEDDATETIME_FIELD, lngDt);

		lngDt = getUpdateTime();
		values.put(ADVENTURES_UPDATETIME_FIELD, lngDt);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			String whereClause = ADVENTURES_ADVENTUREID_FIELD + " = " + adventure.getId();
			int rows = db.update(ADVENTURES_TABLE_NAME, values, whereClause, null);
			if (rows > 0)
				id = 1;
			else {
				Log.e(TAG, "addAdventure could NOT add or update record");
				id = 0;
			}
			db.close();
		}
		return id >= 0;
	}

	public boolean updateAdventureID(long curAdvID, long newAdvID) {
		boolean retValue;
		ContentValues values = new ContentValues();
		values.put(ADVENTURES_ADVENTUREID_FIELD, newAdvID);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			String whereClause;
				
			whereClause = ADVENTURES_ADVENTUREID_FIELD + " = " + curAdvID;
			int rows = db.update(ADVENTURES_TABLE_NAME, values, whereClause, null);
			if (rows > 0)
				retValue = true;
			else {
				Log.e(TAG, "updateAdventureID: could not modify the adventureID!");
				retValue = false;
			}
			db.close();
		}
		return retValue;
	}

	
	/**
	 * This method will return the method record associated with the input adventure ID
	 * @param advID The adventure ID to use for the database query
	 * @return The retrieved record or null if not found
	 */
	public Adventure getAdventure(long advID) {
		Adventure adventure = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				ADVENTURES_ADVENTUREID_FIELD,
				ADVENTURES_OWNERID_FIELD,
				ADVENTURES_VISIBILITY_FIELD,
				ADVENTURES_NAME_FIELD,
				ADVENTURES_DESCRIPTION_FIELD,
				ADVENTURES_CREATEDDATETIME_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = ADVENTURES_ADVENTUREID_FIELD + " = \"" + advID + "\"";
			cursor = db.query(ADVENTURES_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords > 0) {
				cursor.moveToPosition(0);
						
				long recID = cursor.getLong(0);
				long adventureID = cursor.getLong(1);
				int owner = cursor.getInt(2);
				int vis = cursor.getInt(3);
				String name = cursor.getString(4);
				String desc = cursor.getString(5);
		
				long createddate = cursor.getLong(6);
				Date ts = new Date(createddate);
						
				adventure = new Adventure(adventureID, vis, owner, name, desc, ts);
						
				// If the record was found then update the updatetime for the record
				if (recID != -1) {
					String where = _ID + " = " + recID;
					updateRecord(ADVENTURES_TABLE_NAME, where, null);
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return adventure;
	}
	
	/**
	 * This method will retrieve all of the Adventure class objects from the Geotagger
	 * cache database for a specific user.
	 * 
	 * @param uID The ID of the user
	 * @return Returns a list of Adventure class objects
	 */
	Adventure [] getAllUserAdventures(long uID) {
		return null;
	}
	

	public Adventure [] getAdventures() {
		Adventure adventure = null;
		Adventure [] adventures = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				ADVENTURES_ADVENTUREID_FIELD,
				ADVENTURES_OWNERID_FIELD,
				ADVENTURES_VISIBILITY_FIELD,
				ADVENTURES_NAME_FIELD,
				ADVENTURES_DESCRIPTION_FIELD,
				ADVENTURES_CREATEDDATETIME_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			cursor = db.query(ADVENTURES_TABLE_NAME, DEFAULTS_FROM, null, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				adventures = new Adventure[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long adventureID = cursor.getLong(1);
					int owner = cursor.getInt(2);
					int vis = cursor.getInt(3);
					String name = cursor.getString(4);
					String desc = cursor.getString(5);
		
					long createddate = cursor.getLong(6);
					Date ts = new Date(createddate);
						
					adventure = new Adventure(adventureID, vis, owner, name, desc, ts);
					adventures[i] = adventure;
						
					// If the record was found then update the updatetime for the record
					if (recID != -1) {
						String where = _ID + " = " + recID;
						updateRecord(ADVENTURES_TABLE_NAME, where, null);
					}
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}
		return adventures;
	}

	public Adventure [] getAdventures(long ownerID) {
		Adventure adventure = null;
		Adventure [] adventures = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				ADVENTURES_ADVENTUREID_FIELD,
				ADVENTURES_OWNERID_FIELD,
				ADVENTURES_VISIBILITY_FIELD,
				ADVENTURES_NAME_FIELD,
				ADVENTURES_DESCRIPTION_FIELD,
				ADVENTURES_CREATEDDATETIME_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = ADVENTURES_OWNERID_FIELD + " = \"" + ownerID + "\"";
			cursor = db.query(ADVENTURES_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				adventures = new Adventure[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long adventureID = cursor.getLong(1);
					int owner = cursor.getInt(2);
					int vis = cursor.getInt(3);
					String name = cursor.getString(4);
					String desc = cursor.getString(5);
		
					long createddate = cursor.getLong(6);
					Date ts = new Date(createddate);
						
					adventure = new Adventure(adventureID, vis, ownerID, name, desc, ts);
					adventures[i] = adventure;
						
					// If the record was found then update the updatetime for the record
					if (recID != -1) {
						String where = _ID + " = " + recID;
						updateRecord(ADVENTURES_TABLE_NAME, where, null);
					}
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return adventures;
	}
	
	/**
	 * Delete the Adventure record associated with the input Adventure ID
	 * @param advID
	 * @return
	 */
	public boolean deleteAdventure(long advID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = ADVENTURES_ADVENTUREID_FIELD + " = " + advID;
			
			numRows = db.delete(ADVENTURES_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}
	
	/**
	 * This method will return the next "cache" id to use for the Adventure record type.
	 * Cache IDs are negative values and start at CACHE_ID_START and decrease from there.
	 * @return A value of CACHE_ID_START and lower is returned
	 */
	public long getnextAdventureCacheID() {
		long id;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			Cursor c = db.query(ADVENTURES_TABLE_NAME, new String[] { "min(" + ADVENTURES_ADVENTUREID_FIELD + ")" }, null, null, null, null, null);  
			c.moveToFirst();  //ADD THIS!
			id = c.getLong(0);
			
			db.close();
		}
		if (id > CACHE_ID_START)
			return CACHE_ID_START;
		return id-1;
	}
	
	/****************************************************************************************
	 * GROUPS TABLE 
	 ****************************************************************************************/
	
	/**
	 * This method will add a Tag record into the Geotagger cache database.  If the add
	 * fails, the record already exists, in which case the existing record will be updated.
	 * The update time will be updated.
	 * @param tag The Tag class object to add into the database
	 * @return Returns true if successfully added, false if not added
	 */
	public boolean addGroup(Group group) {
		long id;
		
		ContentValues values = new ContentValues();
		values.put(GROUPS_GROUPID_FIELD, group.getId());
		values.put(GROUPS_OWNERID_FIELD, group.getOwnerId());
		values.put(GROUPS_NAME_FIELD, group.getName());
		values.put(GROUPS_DESCRIPTION_FIELD, group.getDescription());
		values.put(GROUPS_IMAGEURL_FIELD, group.getImageUrl());

		Date dt = group.getCreatedDateTime();
		long lngDt = dt.getTime();
		values.put(GROUPS_CREATEDDATE_FIELD, lngDt);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			try {
				id = db.insertOrThrow(GROUPS_TABLE_NAME, null, values);
			} catch (SQLException e) {
				Log.d(TAG, "addGROUP got SQLException! "+e.getMessage());
				id = -1;
			}
			
			// If the id is -1 then the insert failed.  Try to update the record instead
			if (id == -1) {
				String whereClause;
				
				whereClause = GROUPS_GROUPID_FIELD + " = " + group.getId();
				int rows = db.update(GROUPS_TABLE_NAME, values, whereClause, null);
				if (rows > 0)
					id = 1;
				else {
					Log.e(TAG, "addGROUP could NOT add or update record");
				}
			}
			db.close();
		}
		return id >= 0;
	}

	/**
	 * Retrieve all of the groups in the database.
	 * @return List of all of the groups
	 */
	public Group [] getGroups() {
		return getGroups((String)null);
	}
	
	/**
	 * This method will retrieve the Groups that have the input owner ID
	 * @param ownerID The owner ID associated with the groups to retrieve
	 * @return array of groups with the input owner ID
	 */
	public Group [] getGroups(long ownerID) {
		String selString = GROUPS_OWNERID_FIELD + " = \"" + ownerID + "\"";
		return getGroups(selString);
	}
	
	/**
	 * This is a private method that will retrieve Groups based on the input
	 * select string.
	 * @param selectString
	 * @return
	 */
	private Group [] getGroups(String selectString) {
		Group group = null;
		Group []groups = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				GROUPS_GROUPID_FIELD,
				GROUPS_OWNERID_FIELD,
				GROUPS_NAME_FIELD,
				GROUPS_DESCRIPTION_FIELD,
				GROUPS_IMAGEURL_FIELD,
				GROUPS_CREATEDDATE_FIELD,
				 };

		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			cursor = db.query(GROUPS_TABLE_NAME, DEFAULTS_FROM, selectString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				groups = new Group[numRecords];
				for (int i=0; i<numRecords; i++) {
			 		cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					int groupID = cursor.getInt(1);
					int owner = cursor.getInt(2);					
					String name = cursor.getString(3);
					String desc = cursor.getString(4);
					String img = cursor.getString(5);
					long createddate = cursor.getLong(6);
					Date ts = new Date(createddate);

					group = new Group(groupID, owner, name, desc, img, ts);
					groups[i] = group;
						
					// If the record was found then update the updatetime for the record
					if (recID != -1) {
						String where = _ID + " = " + recID;
						updateRecord(GROUPS_TABLE_NAME, where, null);
					}
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return groups;
	}
	
	/**
	 * Delete a specific tag, using the input tagID
	 * @param tagID tagID of the tag to remove
	 * @return true if successful, false oherwise
	 */
	public boolean deleteGroup(long groupID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = GROUPS_GROUPID_FIELD + " = " + groupID;
			
			numRows = db.delete(GROUPS_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}
	
	/**
	 * This method will return the next "cache" id to use for the Group record type.
	 * Cache IDs are negative values and start at CACHE_ID_START and decrease from there.
	 * @return A value of CACHE_ID_START and lower is returned
	 */
	public long getnextGroupCacheID() {
		long id;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			Cursor c = db.query(GROUPS_TABLE_NAME, new String[] { "min(" + GROUPS_GROUPID_FIELD + ")" }, null, null, null, null, null);  
			c.moveToFirst();  //ADD THIS!
			id = c.getLong(0);
			
			db.close();
		}
		if (id >= CACHE_ID_START)
			return CACHE_ID_START;
		return id-1;
	}
	
	/****************************************************************************************
	 * GROUP MEMBERS TABLE 
	 ****************************************************************************************/

	public boolean addGroupMember(long groupId, long memberId) {
		long id;
		
		ContentValues values = new ContentValues();
		values.put(GROUPMEMBERS_GROUPID_FIELD, groupId);
		values.put(GROUPMEMBERS_USERID_FIELD, memberId);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			try {
				id = db.insertOrThrow(GROUPMEMBERS_TABLE_NAME, null, values);
			} catch (SQLException e) {
	//			Log.d(TAG, "addTag got SQLException! "+e.getMessage());
				id = -1;
			}
			
			db.close();
		}
		return id >= 0;
	}

	public boolean deleteGroupMember(long groupID, long memberID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = GROUPMEMBERS_GROUPID_FIELD + " = " + groupID + " AND " +
					GROUPMEMBERS_USERID_FIELD + " = " + memberID;
			
			numRows = db.delete(GROUPMEMBERS_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}

	public boolean deleteGroupMembers(long groupID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = GROUPMEMBERS_GROUPID_FIELD + " = " + groupID;
			
			numRows = db.delete(GROUPMEMBERS_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}

	public AdventureMembers [] getGroupMembers() {
		AdventureMembers [] retRecs = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				GROUPMEMBERS_GROUPID_FIELD,
				GROUPMEMBERS_USERID_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			cursor = db.query(GROUPMEMBERS_TABLE_NAME, DEFAULTS_FROM, null, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				retRecs = new AdventureMembers[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long adventureID = cursor.getLong(1);
					long memberId = cursor.getLong(2);

					retRecs[i] = new AdventureMembers(adventureID, memberId);
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return retRecs;
	}

	public Long [] getGroupMemberIds(long groupId) {
		Long memberId;
		Long [] memberIds = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				GROUPMEMBERS_GROUPID_FIELD,
				GROUPMEMBERS_USERID_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = GROUPMEMBERS_GROUPID_FIELD + " = \"" + groupId + "\"";
			cursor = db.query(GROUPMEMBERS_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				memberIds = new Long[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long adventureID = cursor.getLong(1);
					memberId = cursor.getLong(2);

					memberIds[i] = memberId;
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return memberIds;
	}
	
	public UserAccount [] getGroupMembers(long groupId) {
		UserAccount [] members = null;
		
		Long [] memberIDs;
		
		memberIDs = getGroupTagIds(groupId);
		if (memberIDs == null || memberIDs.length == 0)
			return null;
		
		// Allocate an array of tags
		members = new UserAccount [memberIDs.length];
		
		for (int i=0; i<memberIDs.length; i++) {
			members[i] = getAccount(memberIDs[i]);
		}
		return members;
	}


	/****************************************************************************************
	 * GROUP TAGS TABLE 
	 ****************************************************************************************/

	public boolean addGroupTag(long groupId, long tagID) {
		long id;
		
		ContentValues values = new ContentValues();
		values.put(GROUPTAGS_GROUPID_FIELD, groupId);
		values.put(GROUPTAGS_TAGID_FIELD, tagID);

		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			try {
				id = db.insertOrThrow(GROUPTAGS_TABLE_NAME, null, values);
			} catch (SQLException e) {
				id = -1;
			}
			
			db.close();
		}
		return id >= 0;
	}

	public boolean deleteGroupTag(long groupID, long tagID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = GROUPTAGS_GROUPID_FIELD + " = " + groupID + " AND " +
					GROUPTAGS_TAGID_FIELD + " = " + tagID;
			
			numRows = db.delete(GROUPTAGS_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}

	public boolean deleteGroupTags(long groupID) {
		int numRows;
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
	
			String whereClause = GROUPTAGS_GROUPID_FIELD + " = " + groupID;
			
			numRows = db.delete(GROUPTAGS_TABLE_NAME, whereClause, null);
	
			db.close();
		}
		if (numRows != 1)
			return false;
		return true;
	}

	public Relations [] getGroupTags() {
		Relations [] retRecs = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				GROUPTAGS_GROUPID_FIELD,
				GROUPTAGS_TAGID_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			cursor = db.query(GROUPTAGS_TABLE_NAME, DEFAULTS_FROM, null, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				retRecs = new Relations[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long adventureID = cursor.getLong(1);
					long memberId = cursor.getLong(2);

					retRecs[i] = new Relations(adventureID, memberId);
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return retRecs;
	}

	public Long [] getGroupTagIds(long groupId) {
		Long memberId;
		Long [] memberIds = null;
		Cursor cursor;

		String[] DEFAULTS_FROM = {
				_ID,
				GROUPTAGS_GROUPID_FIELD,
				GROUPTAGS_TAGID_FIELD, };
		
		synchronized (dbLock) {
			// Perform a managed query. The Activity will handle closing
			// and re-querying the cursor when needed.
			SQLiteDatabase db = this.getReadableDatabase();
	
			String selString = GROUPTAGS_GROUPID_FIELD + " = \"" + groupId + "\"";
			cursor = db.query(GROUPTAGS_TABLE_NAME, DEFAULTS_FROM, selString, null, null, null, null);
			
			m_thisActivity.startManagingCursor(cursor);
		
			int numRecords = cursor.getCount();
			if (numRecords >= 0) {
				memberIds = new Long[numRecords];
				for (int i=0; i<numRecords; i++) {
					cursor.moveToPosition(i);
						
					long recID = cursor.getLong(0);
					long adventureID = cursor.getLong(1);
					memberId = cursor.getLong(2);

					memberIds[i] = memberId;
				}
			}
			m_thisActivity.stopManagingCursor(cursor);
			cursor.close();
			db.close();
		}

		return memberIds;
	}
	
	public Tag [] getGroupTags(long groupId) {
		Tag [] tags = null;
		
		Long [] tagIDs;
		
		tagIDs = getGroupTagIds(groupId);
		if (tagIDs == null || tagIDs.length == 0)
			return null;
		
		// Allocate an array of tags
		tags = new Tag [tagIDs.length];
		
		for (int i=0; i<tagIDs.length; i++) {
			tags[i] = getTag(tagIDs[i]);
		}
		return tags;
	}




		
}