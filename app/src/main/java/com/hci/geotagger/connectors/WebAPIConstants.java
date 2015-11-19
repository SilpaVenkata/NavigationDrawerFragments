package com.hci.geotagger.connectors;

/**
 * This class defines constants that are used to interface with the Web API
 * @author PaulCushman
 *
 */
public final class WebAPIConstants {
	public static final String OP_ADD_GROUP = "createGroup";

	//added by syed m shah
	public static final String OP_GETGROUPS_BYID = "getGroupsById";
	
	//added by syed m shah
	public static final String OP_GETGROUPS_BYADVID = "getGroupsByAdvId";
	
	//added by Syed M Shah
	public static final String OP_DELETE_GROUP = "deleteGroup";

	/**
	 * Added By Syed M Shah
	 * New Operations for new database OAuth
	 *
	 */
	//FOR AUTHENTICATION
	public static final String CLIENT_ID="1_1wmi7q6p05i8kw8o80wksk0ccg040ggcwk8wssc8wk8s8g88ow";
	public static final String CLIENT_SECRET="4ffky4dy55s0o808g0kowo8ocwc8oggwks88koss4ss4sookgk";
	public static String ACCESS_TOKEN;
	public static String REFRESH_TOKEN;
	//Operations for Account
	public static final String LOGIN_TOKEN = "/token";
	public static final String ACC_USERS = "/users/";

	public static final String ACC_USERADVENTURE = "/adventures/";
	public static final String ACC_REGISTER = "/users/";
	public static final String ACC_LOGIN = "/login";
	public static final String ACC_ADDFRIEND = "/friends";
	
	public static final String ACC_TAGS = "/tags/";
	public static final String ACC_COMMENTS = "/comments";
	
	public static final String ACC_FORMAT_ADDCOMMENT = "%s/comments/";
	public static final String ACC_FORMAT_ADDTAG = "%s/tags/";
	public static final String ACC_FORMAT_EDITTAG = "%s/tags/%d";
	public static final String ACC_FORMAT_ADDADVENTURE = "%s/adventures/";
	public static final String ACC_FORMAT_EDITADVENTURE = "%s/adventures/%d";
	public static final String ACC_FORMAT_DELETEADVENTURE = "%s/adventures/%d?access_token=%s";
	public static final String ACC_FORMAT_EDITUSER = "%s/users/%d";

	public static final String ACC_FORMAT_UPLOAD = "%s/uploads/";
	
	public static final String ACC_FORMAT_GETTAG = "%s/tags/%d";

	public static final String ACC_FORMAT_GETTAGCOMMENTS = "%s/tags/%d/comments";

	public static final String ACC_FORMAT_GETUSERFRIENDS = "%s/users/%d/friends";
	public static final String ACC_FORMAT_GETADVENTUREMEMBERS = "%s/adventures/%d/members";

	public static final String ACC_FORMAT_GETMYADVENTURES = "%s/profile/myadventures";
	public static final String ACC_FORMAT_GETADVENTURESMEMBEROF = "%s/profile/adventures";
	
	public static final String ACC_FORMAT_GETGROUPMEMBERS = "%s/groups/%d/members";
	public static final String ACC_FORMAT_EDITGROUP = "%s/groups/%d";

	public static final String ACC_FORMAT_RELATION = "%s/relations/?access_token=%s";
	
	// <host>/<object type>/<id>
	public static final String ACC_FORMAT_DELETEOBJECT = "%s/%s/%d";

	//------------------------Filters-----------------------------------
	public static final String FILTER_OFFSET="offset";
	public static final String FILTER_LIMIT="limit";
	
	//------------------------Profile-----------------------------------
	public static final String ACC_PROFILE = "/profile";
	public static final String ACC_PROFILE_TAGS = "/profile/tags";
	public static final String ACC_PROFILE_FRIENDS = "/profile/friends";
	public static final String PROFILE_GROUPS = "/profile/groups";
	public static final String PROFILE_MYGROUPS = "/profile/mygroups";
	//------------------------Groups----------------------------------
	public static final String GROUP_DELETE = "/groups/";
	public static final String GROUPS = "/groups/";
	//Operations for Tag
	public static final String TAG_ALLTAGS = "/tags/";
	//Operations for Adventures
	public static final String ADV_ADVENTURE = "/adventures/";
	public static final String ADV_ADD = "/adventures";
	//------------------------Relations----------------------------------------
	public static final String RELATIONS = "/relations/";
	
	//Operations
	public static final String OP_LOGIN = "login";
	public static final String OP_LOGIN_ID = "loginById";
	public static final String OP_REGISTER = "register";
	public static final String OP_ADD_TAG = "addTag";
	public static final String OP_EDIT_TAG = "editTag";
	public static final String OP_ADD_PERSON = "addUserToAdventureById";
	public static final String OP_REMOVE_PERSONFROMADV = "removeUserFromAdventure";
	public static final String OP_ADD_ADV = "addAdventure";
	public static final String OP_EDIT_ADV = "editAdventure";
	public static final String OP_UPLOAD_IMG = "uploadImage";
	public static final String OP_GETTAGS_BYID = "getTagsById";
	public static final String OP_GETTAGS_BYADVID = "getTagsByAdvId";
	public static final String OP_ADD_TAG2ADV = "addTagToAdventure";
	public static final String OP_REMOVE_TAGFROMADV = "removeTagFromAdventure";
	public static final String OP_GETADVS_BYID = "getAllAdventuresUserPartOf";
	public static final String OP_GETPEOPLE_BYID = "getAllAdventurePeople";
	public static final String OP_GETNAME_FROMID = "getNameFromId";
	public static final String OP_DELETE_TAG = "deleteTag";
	public static final String OP_DELETE_ADV = "deleteAdventure";
	public static final String OP_ADD_FRIEND = "addFriend";
	public static final String OP_GETFRIENDS = "getFriends";
	public static final String OP_GETUSER = "getUser";
	public static final String OP_DELETE_FRIEND = "removeFriend";
	public static final String OP_EDIT_PROFILE = "editProfile";
	public static final String OP_ADD_TAGCOMMENT = "addTagComment";
	public static final String OP_GET_TAGCOMMENTS = "getTagComments";
	public static final String OP_DELETE_TAGCOMMENT = "deleteTagComment";
	
	public static final String OP_ADD_TAG2GROUP = "addTagToGroup";
	public static final String OP_REMOVE_TAGFROMGROUP = "removeTagFromGroup";
	public static final String OP_ADD_MEMBER2GROUP = "addMemberToGroup";
	public static final String OP_REMOVE_MEMBERFROMGROUP = "removeMemberFromGroup";

	//URLs for php operations (in case main script is split up in the future)
	/* Local URLs for Debug
	public static final String LOGIN_URL = "http://10.0.2.2/geotagger/index.php";
	public static final String REGISTER_URL = "http://10.0.2.2/geotagger/";
	public static final String TAGOP_URL = "http://10.0.2.2/geotagger/";
	public static final String IMAGE_URL = "http://10.0.2.2/geotagger/";
	*/
	
	//Live URLs for the server
	//currently just hardcoding the geotaggerdev for the base url
//	public static final String BASE_URL = "http://hci.montclair.edu/geotagger/";
//	public static final String BASE_URL_DEV = "http://hci.montclair.edu/geotagger/";
	public static final String BASE_URL = "http://mobsci.montclair.edu/geotagger/";
	public static final String BASE_URL_DEV = "http://mobsci.montclair.edu/geotagger/";
    public static final String BASE_URL_OAUTH = "http://mobsci.montclair.edu/geotagger/oauth/v2";
    public static final String BASE_URL_GTDB = "http://mobsci.montclair.edu/geotagger/api/v1";
    
	public static final String LOGIN_URL = BASE_URL + "index.php";
	public static final String REGISTER_URL = BASE_URL + "index.php";
	public static final String TAGOP_URL = BASE_URL + "index.php";
	public static final String IMAGE_URL = BASE_URL + "index.php";
	public static final String ADV_URL = BASE_URL + "index.php";
	public static final String GROUP_OP_URL = BASE_URL + "index.php";
	
}
