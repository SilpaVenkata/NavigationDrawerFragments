package com.hci.geotagger.objects;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.hci.geotagger.R;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.connectors.TagHandler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Tag extends GeotaggerObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name, description, locationString, category, ownerName;
	private int ratingScore, visibility;
	private long ownerId;
	private UserAccount owner;
	private GeoLocation location = null;
	private Date createdDateTime;
	
	private long imageId = -1;
	private String imageUrl = "";
	private File imageUploadFile = null;
	private String imageData = null;
	
	private Bitmap bitmap = null;
	private Bitmap thumbnail = null;
	
	public Tag(long id) {
		this.setId(id);
	}
	
	/**
	 * Tag constructor when user account information is available. This is mostly used when a tag is first created locally 
	 * and no id is available.
 	 * @param name Tag name.
	 * @param desc Tag Description.
	 * @param url url of the image for the tag located on the server.
	 * @param locStr String representing user defined location.
	 * @param rating rating associated with the Tag.
	 * @param own  UserAccount for the Tag.
	 * @param loc The actual geographic location(latitude and longitude) of the Tag.
	 * @param vis The visibility of the Tag.
	 */
	public Tag(String name, String desc, String url, String locStr,
			int rating, UserAccount own, GeoLocation loc, int vis)
	{
		this.setName(name);
		this.setDescription(desc);
		this.setImageUrl(url);
		this.setLocationString(locStr);
		this.setRatingScore(rating);
		this.setOwner(own);
		this.setLocation(loc);
		this.setVisibility(vis);
	
		checkBoxID = R.id.row_checkbox;
	}
		
	/**
	 * Tag constructor when user id is availble. This is mostly used when the tag id can be retrieved from the server 
	 * @param tId Tag Id.
	 * @param name Tag name.
	 * @param desc Tag Description.
	 * @param url url of the image for the tag located on the server
	 * @param locStr locStr String representing user defined location.
	 * @param cat Category for the Tag.
	 * @param rating rating associated with the Tag.
	 * @param oId User id associated with Tag.
	 * @param oName User name associated with Tag.
	 * @param loc The actual geographic location(latitude and longitude) of the Tag.
	 * @param vis The visibility of the Tag.
	 * @param ts Date of creation for the Tag.
	 */
	public Tag(long tId, String name, String desc, String url, String locStr, String cat,
			int rating, long oId, String oName, GeoLocation loc, int vis, Date ts)
	{
		this.setId(tId);
		this.setName(name);
		this.setDescription(desc);
		this.setImageUrl(url);
		this.setLocationString(locStr);
		this.setCategory(cat);
		this.setRatingScore(rating);
		this.setOwnerId(oId);
		this.setOwnerName(oName);
		this.setLocation(loc);
		this.setVisibility(vis);
		this.createdDateTime = ts;
		
		checkBoxID = R.id.row_checkbox;
	}
	

	//use this when getting tag object from db and ID is accessible. Also uses values for
	//owner id and name so a user account object does not need to be created at this time.
	public Tag(long tId, String name, String desc, String url, String locStr, String cat,
			int rating, long oId, GeoLocation loc, int vis, Date ts)
	{
		this.setId(tId);
		this.setName(name);
		this.setDescription(desc);
		this.setImageUrl(url);
		this.setLocationString(locStr);
		this.setCategory(cat);
		this.setRatingScore(rating);
		this.setOwnerId(oId);
		this.setLocation(loc);
		this.setVisibility(vis);
		this.createdDateTime = ts;
		
		checkBoxID = R.id.row_checkbox;
	}
	
	public Tag(long tId, String name, String desc, String url, String locStr,
			int rating, int oId, GeoLocation loc, Date ts, int vis)
	{
		this.setId(tId);
		this.setName(name);
		this.setDescription(desc);
		this.setImageUrl(url);
		this.setLocationString(locStr);
		this.setRatingScore(rating);
		this.setOwnerId(oId);
		this.setLocation(loc);
		this.createdDateTime = ts;
		this.setVisibility(vis);
		
		checkBoxID = R.id.row_checkbox;
	}

	public Tag(Long tId, String name) {
		this.setId(tId);
		this.setName(name);
		checkBoxID = R.id.row_checkbox;
	}

	@Override
	public TagHandler getHandler(Context context) {
		return new TagHandler(context);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getImageId() {
		return imageId;
	}

	public void setImageId(long id) {
		this.imageId = id;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public File getImageUploadFile() {
		return imageUploadFile;
	}

	public void setImageUploadFile(File imageUploadFile) {
		this.imageUploadFile = imageUploadFile;
	}

	public String getImageData() {
		return imageData;
	}

	public void setImageData(String imageData) {
		this.imageData = imageData;
	}

	public String getLocationString() {
		return locationString;
	}

	public void setLocationString(String locationString) {
		this.locationString = locationString;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getRatingScore() {
		return ratingScore;
	}

	public void setRatingScore(int ratingScore) {
		this.ratingScore = ratingScore;
	}

	public UserAccount getOwner() {
		return owner;
	}

	public void setOwner(UserAccount owner) {
		this.owner = owner;
	}

	public GeoLocation getLocation() {
		return location;
	}

	public void setLocation(GeoLocation location) {
		this.location = location;
	}

	public int getVisibility() {
		return visibility;
	}

	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	public Bitmap getThumbnail() {
		return thumbnail;
	}
	
	public void setThumbnail(Bitmap bitmap) {
		thumbnail = bitmap;
	}
	
	// define static values to instantiated object not needed
	public static final int LIST_LAYOUT_ID = R.layout.row;
	public static final int CHECK_LAYOUT_ID = R.layout.row_with_checkbox;
	public static final int CHECKBOX_ID = R.id.row_checkbox;

	@Override
	public int getListLayoutID() {
		return LIST_LAYOUT_ID;
	}
	
	@Override
	public View updateListView(View view) {
		TextView nameTxt = (TextView) view.findViewById(R.id.row_txtName);
		TextView descTxt = (TextView) view.findViewById(R.id.row_txtdesc);
		TextView timeTxt = (TextView) view.findViewById(R.id.row_txtTime);
		ImageView imgView = (ImageView) view.findViewById(R.id.row_thumbnail);
		
		if (nameTxt != null)
			nameTxt.setText(getName());
		if (descTxt != null)
			descTxt.setText(getDescription());
		if (timeTxt != null) {
			Date date = getCreatedDateTime();
			SimpleDateFormat df = new SimpleDateFormat(
					Constants.DATETIME_FORMAT);
			String formatted = df.format(date);
			timeTxt.setText(formatted);
		}
		// Set thumbnail of tag image to imageview
		if (imgView != null) {
			if (thumbnail != null) 
				imgView.setImageBitmap(thumbnail);
			else if (bitmap != null)
				imgView.setImageBitmap(bitmap);
			else {
				Bitmap default_bitmap = BitmapFactory.decodeResource(view.getResources(), R.drawable.icon);
				imgView.setImageBitmap(default_bitmap);
			}
		}
		return super.updateListView(view);
	}
	
	@Override
	public int getCheckableListLayoutID() {
		return CHECK_LAYOUT_ID;
	}
	
	@Override
	public int getCheckableListCheckBoxID() {
		return CHECKBOX_ID;
	}

	@Override
	public View updateCheckableView(View view) {
		TextView nameTxt = (TextView) view.findViewById(R.id.row_txtName);
		TextView descTxt = (TextView) view.findViewById(R.id.row_txtdesc);
		TextView timeTxt = (TextView) view.findViewById(R.id.row_txtTime);
		
		if (nameTxt != null)
			nameTxt.setText(getName());
		if (descTxt != null)
			descTxt.setText(getDescription());
		if (timeTxt != null) {
			Date date = getCreatedDateTime();
			SimpleDateFormat df = new SimpleDateFormat(
					Constants.DATETIME_FORMAT);
			String formatted = df.format(date);
			timeTxt.setText(formatted);
		}
		return super.updateCheckableView(view);
	}
	
	@Override
	public boolean inList(ArrayList<?> objList) {
		ArrayList<Tag> list = (ArrayList<Tag>)objList;
		for (Tag tag : list) {
			if (id == tag.getId())
				return true;
		}
		return false;
	}

}
