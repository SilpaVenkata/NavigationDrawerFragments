/*
 * Account Handler class handles the calls to the webservice that deal
 * with user account operations, such as adding accounts, adding friends,
 * logging in, etc.
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

import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.NetworkUtils;
import com.hci.geotagger.objects.MyUserAccount;
import com.hci.geotagger.objects.UserAccount;

import android.content.Context;
import android.util.Log;

 
public class AccountHandler extends GeotaggerHandler {
    private static final String TAG = "AccountHandler";
    public static final String NAME = "AccountHandler";
	protected String [] mActionsSupported = {
			WebAPIConstants.OP_ADD_FRIEND,
			WebAPIConstants.OP_DELETE_FRIEND
	};

	private String access_token;
	private String refresh_token;
	private OAuthHandler oauthHandler;
	public static JSONObject access;

    
    // constructor
    public AccountHandler(Context context){
    	super(context, WebAPIConstants.LOGIN_URL);
        oauthHandler = new OAuthHandler(context);
		setActionList(mActionsSupported);
    }
 
    /**
     * This method will perform a login operation to the server.  If the network
     * is down then the login operation will be performed against the cache database.
     * @param uName The username used to login
     * @param password The password used to login
     * @return A ReturnInfo structure is used to identify the result of the login
     */
    public ReturnInfo login(String uName, String password) {
    	ReturnInfo retValue;
    	int success;
    	
		// If the network is up then try to get the record from the Server DB
		if (NetworkUtils.isNetworkUp(context)) {
			access = loginFromServer(uName, password);
			try {
		//Syed M Shah
		//Retrieve access token after Authentication and used it in the URL for other requests
				access_token = access.getString("access_token");
				WebAPIConstants.ACCESS_TOKEN = access_token;
				refresh_token = access.getString("refresh_token");
				WebAPIConstants.REFRESH_TOKEN = refresh_token;
				
				Log.d(TAG, "From ACCESS_TOKEN " + WebAPIConstants.ACCESS_TOKEN +"\t"+ WebAPIConstants.REFRESH_TOKEN);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "Could not Extract Access_token");
				
				e.printStackTrace();
			}
			
			//access_token used to access the user table.
			
			JSONObject response;
			response = getProfileInfo();
			if (response != null ) {
				success = 1;
			} else {
				success = 0;
			}

			retValue = new ReturnInfo(response,success);
			Log.i("retVal", retValue.toString());
			if (retValue.success) {
				retValue.object = createAccountFromJSON(response);
				//cache.addAccount((MyUserAccount)retValue.object);
			}
		} else {
			MyUserAccount mua = cache.loginByName(uName, password);
			if (mua != null) {
				retValue = new ReturnInfo();
				retValue.object = mua;
			} else
				retValue = new ReturnInfo(ReturnInfo.FAIL_NONETWORK);
		}
		return retValue;
    }
    
    /**
     * Attempt to login user with username/pw
     * If successful, returns JSON account object
     * Performs the following query on the server database:
     * SELECT AccountID, Username, EmailAddress, Password, Image, Description, Location, Quote, Type, Visibility, 
     * 		CreatedDateTime, RatingScore FROM accounts WHERE Username='".$username."' AND Password=MD5('".$password."')
     * */
    private JSONObject loginFromServer(String uName, String password) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();       
        String loginURL = WebAPIConstants.BASE_URL_OAUTH +"/token";  
        params.add(new BasicNameValuePair("grant_type","password"));
        params.add(new BasicNameValuePair("username", uName));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("client_id", WebAPIConstants.CLIENT_ID));
        params.add(new BasicNameValuePair("client_secret", WebAPIConstants.CLIENT_SECRET));        
		try {
			JSONObject json = jsonParser.getJSONFromUrlRaw(loginURL, params);		
			Log.d(TAG, "JSON Response from PHP: " + json.toString());
			Log.i("TOKEN", json.toString());
			return json;
		} catch (Exception ex) {
			Log.d(TAG, "Login: Exception occurred during login, returning null.");
			return null;
		}
    }
    
    public boolean refreshToken() {
    	boolean retVal = false;
    	
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();       
        String loginURL = WebAPIConstants.BASE_URL_OAUTH +"/token";  
        params.add(new BasicNameValuePair("grant_type","refresh_token"));
        params.add(new BasicNameValuePair("refresh_token", WebAPIConstants.REFRESH_TOKEN));
        params.add(new BasicNameValuePair("client_id", WebAPIConstants.CLIENT_ID));
        params.add(new BasicNameValuePair("client_secret", WebAPIConstants.CLIENT_SECRET));        
		try {
			JSONObject json = jsonParser.getJSONFromUrlRaw(loginURL, params);
			
			Log.d(TAG, "JSON Response from PHP: " + json.toString());
			Log.i("TOKEN", json.toString());
			
			int success = json.getInt("success");
			if (success == 1) {
				WebAPIConstants.ACCESS_TOKEN  = json.getString("access_token");
				WebAPIConstants.REFRESH_TOKEN = json.getString("refresh_token");
				Log.d(TAG, "From ACCESS_TOKEN " + WebAPIConstants.ACCESS_TOKEN +"\t"+ WebAPIConstants.REFRESH_TOKEN);
				retVal = true;
			} else {
				WebAPIConstants.ACCESS_TOKEN  = "";
				WebAPIConstants.REFRESH_TOKEN = "";
			}
		} catch (Exception ex) {
			Log.d(TAG, "Login: Exception occurred during refresh token.");
		}
		return retVal;
    }

    
    /**
     * @author Syed M Shah
     * Method for getting the profile information of the user 
     */
    public JSONObject getProfileInfo() {
		try {
			JSONObject json = jsonParser.getUserAccount();
    		JSONObject user = json.getJSONObject("user");
			Log.d(TAG, "JSON for USER: " + user.toString());
			return user;
		} catch (Exception ex) {
			Log.d(TAG, "Login: Exception occurred during login, returning null.");
			return null;
		}
    }

    /**
     * This method will perform a login operation to the server.  If the network
     * is down then the login operation will be performed against the cache database.
     * @param id The ID associated with this user
     * @param password The password used to login
     * @return A ReturnInfo structure is used to identify the result of the login
     */
    public ReturnInfo login(int id, String password) {
    	ReturnInfo retValue;
    	
		// If the network is up then try to get the record from the Server DB
		if (NetworkUtils.isNetworkUp(context)) {
			JSONObject response;
			response = loginFromServer(id, password);
			
			retValue = new ReturnInfo(response);
			if (retValue.success) {
				retValue.object = createMyAccountFromJSON(response);
				cache.addAccount((MyUserAccount)retValue.object);
			}
		} else {
			MyUserAccount mua = cache.loginById(id, password);
			if (mua != null) {
					retValue = new ReturnInfo();
					retValue.object = mua;
			} else
				retValue = new ReturnInfo(ReturnInfo.FAIL_NONETWORK);
		}
		return retValue;
    }
    
    /**
     * Attempt to login user with ID/pw
     * If successful, returns JSON account object
     * Performs following DB request on server:
     * SELECT AccountID, Username, EmailAddress, Password, Image, Description, Location, Quote, Type, Visibility, 
     * 				CreatedDateTime, RatingScore FROM accounts WHERE AccountID=".$id." AND Password='".$password."'"
     * */
    public JSONObject loginFromServer(int id, String password){
    	//Create post params
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String loginURL = WebAPIConstants.BASE_URL_GTDB +"/users";
//        params.add(new BasicNameValuePair("Authentication", "Bearer"));
//        params.add(new BasicNameValuePair("access_token",access_token));
        params.add(new BasicNameValuePair("id", Integer.toString(id)));
        params.add(new BasicNameValuePair("password", password));
        //get json response
        JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
        Log.d(TAG, "loginFromServer id Pwd: JSON Response from PHP: " + json.toString());
        return json;
    }

    //----------------------These methods for Registering a new User-------------------

    /**
     * Get a UserAccount record based on a username from the server.  If the network is not up
     * then the record will be retrieved from the cache.
     * @param username The user name of the UserAccount
     * @return The UserAccount record retrieved from the database or cache
     */
    public UserAccount getUser(String username) {
    	UserAccount ua;
    	
		// If the network is up then try to get the record from the Server DB
		if (NetworkUtils.isNetworkUp(context)) {
			ua = createAccountFromJSON(getUserFromServer(username));
			cache.addAccount(ua);
		} else {
			// TODO: If the network was not up then lets check the cache for the records
			ua = cache.getAccount(username);
		}
		return ua;
    }

    private JSONObject getUserFromServer(String username) {
    	//TODO: this FUNCTION NEEDS TO BE IMPLEMENTED CORRECTLY!!!  THE WEB SERVICE IS NOT UPDATED
    	
    	//Create post params
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("operation", WebAPIConstants.OP_GETUSER));
        params.add(new BasicNameValuePair("username", username));
        //get json response
        JSONObject json = jsonParser.getJSONFromUrl(WebAPIConstants.LOGIN_URL, params);
        Log.d(TAG, "getUserFromServer: JSON Response from PHP: " + json.toString());
        return json;
    }
    
    //Attempt to register user. If successful, returns JSON Account object
    /**
     * This method will register the input username and password.  The result is
     * returned as a ReturnInfo object.  There is no object returned within the
     * ReturnInfo object.
     * @param name a string that represents the username
     * @param password a string that represents the user's password
     * @return a ReturnInfo object that identifies the status of the operation
     */
    public ReturnInfo registerUser(String name, String password){
    	//TODO: this FUNCTION NEEDS TO BE IMPLEMENTED CORRECTLY!!!  THE WEB SERVICE IS NOT UPDATED

		// If the network is up then try to get the record from the Server DB
		if (NetworkUtils.isNetworkUp(context)) {
	        // Create post params
	    	try
	    	{
	    		List<NameValuePair> params = new ArrayList<NameValuePair>();
	    		params.add(new BasicNameValuePair("operation", WebAPIConstants.OP_REGISTER));
	    		params.add(new BasicNameValuePair("username", name));
	    		params.add(new BasicNameValuePair("password", password));
	    		//get JSON response
	    		JSONObject json = jsonParser.getJSONFromUrl(WebAPIConstants.REGISTER_URL, params);
	    		
	    		return new ReturnInfo(json);
	    	}
	    	catch (Exception ex)
	    	{
	    		ex.printStackTrace();
	    		return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
	    	}
		} else {
			//TODO: What to do if network is down???
			return new ReturnInfo(ReturnInfo.FAIL_NONETWORK);
		}
    }
    
    /**
     * Create a user account object from a JSON object
     * @in json: JSONObject
     * @out UserAccount
     */
    private MyUserAccount createMyAccountFromJSON(JSONObject json)
    {
    	Date d = new Date();
    	try 
    	{
    		//get the user object from the json object
    		JSONObject jUser = json;
    		//format the date
    		SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			d = ts.parse(jUser.getString("created_at"));
			
			String pwd = "";
			pwd = jUser.getString("password");
			//instantiate the user account object with properties from JSON
			MyUserAccount ua = new MyUserAccount(jUser.getInt("id"), jUser.getString("username"), jUser.getString("email"), 
					pwd, jUser.getString("image_url"), jUser.getString("biography"), 
					jUser.getString("location"), jUser.getString("quote"), jUser.getInt("type"),
					0, d, jUser.getInt("rating"));

			return ua;
		} 
    	catch (JSONException e) 
    	{
    		Log.d(TAG, "CreateUserAccount from JSONObject failed");
			e.printStackTrace();
		} catch (ParseException e) {
			Log.d(TAG, "Problem parsing timestamp from JSON");
			e.printStackTrace();
		}
    	return null;
    }
    
    //------------------------------------End of Register New User-----------------------------------    
    
    /**
     * Used when the user account is first registered. All user entered fields are entered into a Json object which are used and
     * parsed in User Account constructor
     * 
     */
    
    public static UserAccount createAccountFromJSON(JSONObject json) {
    	Date d = new Date();
    	String image_url;
    	String email;
    	try {
    		//get the user object from the json object
    		JSONObject jUser = json;
    		//format the date
    		Log.d("CreateAccountFromJson", jUser.getString("created_at"));
    		Log.d("CreateAccountFromJson", "Inside the Method ");
    		/**
    		 * Syed M Shah- Comment
    		 *  Zoned Date format Different from the older application Version which user older database
    		 */
    		SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
    		Log.d("CreateAccountFromJson", "Inside the Method ");
			d = ts.parse(jUser.getString("created_at"));
			
			if (!jUser.has("image_url"))
				image_url = "/res/drawable/user.jpg";
			else
				image_url = jUser.getString("image_url");
			
			if (jUser.has("email"))
				email = jUser.getString("email");
			else
				email = "";

			int vis = 1;
			String pw = null;
			Log.d("Created Date", d.toString());
			Log.d("CreateAccountFromJson", "Inside the Method ");
			//instantiate the user account object with properties from JSON
			MyUserAccount ua = new MyUserAccount(jUser.getInt("id"), jUser.getString("username"), 
					email, pw, image_url, jUser.getString("biography"), 
					jUser.getString("location"), jUser.getString("quote"), jUser.getInt("type"), 
					vis, d, jUser.getInt("rating"));
			
			if (jUser.has("name"))
				ua.setName(jUser.getString("name"));
			
			Log.d("User Account:", ua.getId()+ ua.getuName());
			return ua;
		} catch (Exception e) {
			Log.d(TAG, "Problem parsing timestamp from JSON");
			e.printStackTrace();
		}
    	return null;
    }
 
    /**
     * String GetUsernameFromId
     * @param id unique user id
     * @return username username of currently logged in user
     * */
    public String getUsernameFromId(int id) {
    	String username = null;
    	
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
            // Create post params
        	try {
        		String url = WebAPIConstants.BASE_URL_GTDB+WebAPIConstants.ACC_USERS + id;
        		Log.d("Inside getUsernameFromID", url);        		
        		//get JSON response
        		JSONObject json = jsonParser.getInfoFromGet(url);
        		JSONObject user = json.getJSONObject("user");
        		username = user.getString("username");
        		Log.d("UserName AH:", username);       		
        		//TODO: add the cached value to the database        		
        	}
        	catch (Exception ex)
        	{
        		ex.printStackTrace();
        	}
		} else {
			//TODO: Get the cached value
			username = cache.getUsernameFromAccount(id);
		}
		return username;
    }
    
    /**
     * Attempt to add friend to server and local cache database
     * @param userId unique userid
     * @param friendName unique friend id
     * @return result of adding friend to server
     */
    public ReturnInfo addFriend(long userId, String friendName) {
    	ReturnInfo result;

    	/**
    	 * Syed M Shah
    	 * Added Authentication and access_token for REST api
    	 * Creating dynamic url to add Friend
    	 */	
    	String url = WebAPIConstants.BASE_URL_GTDB+WebAPIConstants.ACC_USERS +userId+ WebAPIConstants.ACC_ADDFRIEND;
		Log.d("Inside getUserFromID", url);

        // Create post params
		JSONObject params = new JSONObject();
		try {
			params.put("userID", userId);
			params.put("fName", friendName);
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
    	
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			JSONObject json = addFriendToServer(url, params);
			result = new ReturnInfo(json);
			cache.addFriend(userId, friendName);
		} else {
			// Add the record to the Cache for now
			if (cache.addFriend(userId, friendName)) 
				result = new ReturnInfo();
			else
				result = new ReturnInfo(ReturnInfo.FAIL_NOCACHE);
			
			// add the deleteTag request to the cached list of DB transactions
			// TODO: sending the URL but this may cause problem with the action cache
			cache.cacheAction(NAME, WebAPIConstants.OP_ADD_FRIEND, params);
		}
		return result;
    }
    
    private JSONObject addFriendToServer(String url, JSONObject params) {
    	//TODO: this FUNCTION NEEDS TO BE IMPLEMENTED CORRECTLY!!!  THE WEB SERVICE IS NOT UPDATED
    	try {
    		//get JSON response
//    		JSONObject json = jsonParser.getJSONFromUrl(url, params);
//    		return json;
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
		return null;
    }
    
    /**
     * Perform the cached action to add a friend to the server database
     * @param params
     * @return
     */
	public boolean addFriendToServerDB(List<NameValuePair> params) {
		long userID = NameValuePairList.getLong(params, "userID");
		long friendID = NameValuePairList.getLong(params, "friendID");
		return addFriendToServerDB(friendID, userID);
	}
	
	/**
	 * Send a request to add a friend to the server database
	 * @param friendID
	 * @param userID
	 * @return
	 */
	private boolean addFriendToServerDB(long friendID, long userID) {
		return jsonParser.sendRelationToServerDB(false, "addfriend", "user", friendID, userID);
	}
	
	/**
	 * Perform the cached action to remove a friend from the server database
	 * @param params
	 * @return
	 */
	public boolean removeFriendFromServerDB(List<NameValuePair> params) {
		long userID = NameValuePairList.getLong(params, "userID");
		long friendID = NameValuePairList.getLong(params, "friendID");
		return removeFriendFromServerDB(friendID, userID);
	}
	
	/**
	 * Send the request to remove a specific friend
	 * @param friendID
	 * @param userID
	 * @return
	 */
	private boolean removeFriendFromServerDB(long friendID, long userID) {
		return jsonParser.sendRelationToServerDB(true, "removefriend", "user", friendID, userID);
	}
	
    
    /**
     * Loops thru Friends associated with user account and adds them to an arraylist. All information 
     * cached in cache database.
     * 
     * 	//Syed M Shah  gets all the friends of the user from /users/{id}/friends
	 * REST currently returns an empty array as Friend feature has not yet been implemented
     * TODO:Dependent on the REST call which is not correct as off now 
     * 
     * @param friendListOwnerId friend id ossociated with user account currently logged in
     * @return returns arraylist of friends as useraccount objects
     */

	public ArrayList<UserAccount> getFriends(long friendListOwnerId) {
		ArrayList<UserAccount> friends;
		
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			friends = new ArrayList<UserAccount>();
			
			JSONArray friendData = getFriendsFromServer(friendListOwnerId);
			JSONObject obj;
			if (friendData != null) {
				// loop through each JSON entry in the JSON array (tags encoded as JSON)
				for (int i = 0; i < friendData.length(); i++) {
					obj = null;
					try {
						obj = friendData.getJSONObject(i);
					} catch (JSONException e) {
						Log.d(TAG, "Error getting JSON Object from array.");
						e.printStackTrace();
					}
	
					if (obj != null) {
						UserAccount a = createAccountFromJSON(obj);
						friends.add(a);
						
						// TODO: add/update the record in the cache
						cache.addAccount(a);
						cache.addFriend(friendListOwnerId, a.getId());
					}
				}
			}
		} else {
			// TODO: If the network was not up then lets check the cache for the records
			friends = cache.getFriends(friendListOwnerId);
		}
		return friends;
	}
	
    /**
     * gets jsonarray of user account objects that represents the users friends from the server
     * @param friendListOwnerId Friends user id
     * @return JsonArray from server of friends as UserAccount objects
     */
	private JSONArray getFriendsFromServer(long friendListOwnerId) {
		String url = String.format(WebAPIConstants.ACC_FORMAT_GETUSERFRIENDS, WebAPIConstants.BASE_URL_GTDB, 
						friendListOwnerId);
        
		try {
			JSONObject jObject = jsonParser.getJSONObject(url);
			
			if (jObject.has("friends")) {
				JSONArray resultsArray = jObject.getJSONArray("friends");
				return resultsArray;
			}
			Log.d(TAG, "getFriendsFromServer: JSON Response from PHP: " + jObject.toString());
		} catch (Exception ex) {
			Log.d(TAG, "getFriendsFromServer: Exception occurred getting friends, returning null.");
		}
		return null;
	}
	
	/**
	 * deletes a friend association from the db
	 * TODO: Method needs to be fixed for new REST API
	 * @param userID Unique user id
	 * @param fId Unique friend id
	 * @return
	 */
	public boolean deleteFriend(long userID, long fId)
	{
    	//TODO: this FUNCTION NEEDS TO BE IMPLEMENTED CORRECTLY!!!  THE WEB SERVICE IS NOT UPDATED
		boolean retVal = false;

		// Building Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("operation", WebAPIConstants.OP_DELETE_FRIEND));
		params.add(new BasicNameValuePair("userID", Long.toString(userID)));
		params.add(new BasicNameValuePair("fId", Long.toString(fId)));
		
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
		       
			//make webservice call to remove friendassociation from db
			try {
				JSONObject json = jsonParser.getJSONFromUrl(WebAPIConstants.LOGIN_URL, params);
				Log.d(TAG, "getFriendsFromServer: JSON Response from PHP: " + json.toString());
				retVal = true;
				
				//TODO: remove the entry in the cache
				cache.deleteFriend(userID, fId);
			} catch (Exception ex) {
				Log.d(TAG, "getFriendsFromServer: Exception occurred removing friend, returning error.");
			}
		} else {
			// TODO: cache the database operation to be done later when the database is accessible
		}
		return retVal;
	}

	/**
	 * Edit User Profile.
	 * @param userID
	 * @param name
	 * @param imgUrl
	 * @param biography
	 * @param location
	 * @param quote
	 * @param email
	 * @return
	 */
	public boolean editProfile(long userID, String name, String imgUrl, String biography, String location, String quote, String email) {
		boolean retValue = false;
		String url = WebAPIConstants.BASE_URL_GTDB + WebAPIConstants.ACC_USERS + userID;
		Log.d("editProfile AH:",url);
		JSONObject userInfo;
		JSONObject params;
		// If the network is up then try to get the record from the Server DB
	
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			// Building JSONObject user information to edit
		try {
			userInfo = new JSONObject();
			userInfo.put("name", name);
			userInfo.put("email", email );
			userInfo.put("biography", biography);
			userInfo.put("location", location);
			userInfo.put("quote", quote);
			userInfo.put("image", imgUrl);
			
			params = new JSONObject();
			params.put("user", userInfo);
			//Make Web call to edit profile
			
				//Method in JSONParser Class to Call PUT Method in REST API 
				//TODO: Needs To be tested 
				retValue = jsonParser.restPutCall(url, params);
			
				//TODO: Update the user profile
				
				JSONObject profileInfo = getProfileInfo();
				UserAccount ua = createAccountFromJSON(profileInfo);
				if (ua != null)
					cache.addAccount(ua);
				retValue = true;
			} catch (Exception ex) {
				Log.d(TAG, "editProfile: failed. Returning false");
			}
		} else {
			// TODO: cache the database operation to be done later when the database is accessible
			
			// TODO: Modify the cache database record
		}
		return retValue;
	}
    
}
