package com.hci.geotagger.objects;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hci.geotagger.R;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.CommentHandler;

public class Comment extends GeotaggerObject implements Serializable{

	private static final long serialVersionUID = 2L;
	private long parentTagId;
	private String text, Username;
	
	private Date createdDateTime;
	
	private long imageId = -1;
	private String imageUrl = "";
	private File imageUploadFile = null;
	private String imageData = null;

	private Bitmap bitmap = null;
	
	public Comment(long id) {
		this.id = id;
		text = "";
		imageUrl = null;
		Username = UserSession.CURRENT_USER.getuName();
	}

	public Comment(String comment) {
		text = comment;
		imageUrl = null;
		Username = UserSession.CURRENT_USER.getuName();
	}

	public Comment(long tagID, String comment, String imageUrl) {
		setParentTagId(tagID);
		text = comment;
		this.imageUrl = imageUrl;
		Username = UserSession.CURRENT_USER.getuName();
	}

	/**
	 * Standard comment constructor
	 * @param cID The comment id
	 * @param tagID The tag id the comment is associated with
	 * @param txt The comment as a string
	 * @param uName The username associated with the comment
	 * @param ts The date of creation for the comment
	 */
	public Comment(long cID, long tagID, String txt, String uName, Date ts)
	{
		this.setId(cID);
		this.setParentTagId(tagID);
		this.setText(txt);
		this.setUsername(uName);
		this.setCreatedDateTime(ts);
	}
	
	/**
	 * Comment constructor for use with images
	 * @param cID The comment id
	 * @param tagID The tag id the comment is associated with
	 * @param txt The comment as a string
	 * @param uName The username associated with the comment
	 * @param ts The date of creation for the comment
	 * @param imgUrl the url of the image used in the tag
	 */
	public Comment(long cID, long tagID, String txt, String uName, Date ts, String imgUrl)
	{
		this.setId(cID);
		this.setParentTagId(tagID);
		this.setText(txt);
		this.setUsername(uName);
		this.setCreatedDateTime(ts);
		this.setImageURL(imgUrl);
	}
	
	public Comment(long cID, String txt, Date ts)
	{
		this.setId(cID);
		this.setText(txt);
		this.setCreatedDateTime(ts);
		Username = UserSession.CURRENT_USER.getuName();
	}

	
	@Override
	public CommentHandler getHandler(Context context) {
		return new CommentHandler(context);
	}


	public long getTagId() {
		return parentTagId;
	}

	public void setTagId(long id) {
		parentTagId = id;
	}

	public long getParentTagId() {
		return parentTagId;
	}

	public void setParentTagId(long parentTagId) {
		this.parentTagId = parentTagId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUsername() {
		return Username;
	}

	public void setUsername(String username) {
		Username = username;
	}

	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	
	public long getImageId() {
		return imageId;
	}

	public void setImageId(long id) {
		this.imageId = id;
	}

	public void setImageURL(String imgURL) {
		this.imageUrl = imgURL;
	}

	public String getImageURL() {
		return imageUrl;
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

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	
	// define static values to instantiated object not needed
	public static final int LIST_LAYOUT_ID = R.layout.commentrow;

	@Override
	public int getListLayoutID() {
		return LIST_LAYOUT_ID;
	}
	
	@Override
	public View updateListView(View view) {
		TextView nameTxt = (TextView) view.findViewById(R.id.commentrow_txtName);
		TextView timeTxt = (TextView) view.findViewById(R.id.commentrow_txtTime);
		TextView commentTxt = (TextView) view.findViewById(R.id.commentrow_txtdesc);
		ImageView imgView = (ImageView) view.findViewById(R.id.commentrow_thumbnail);
		
		if (nameTxt != null)
			nameTxt.setText(getUsername());
		if (commentTxt != null)
			commentTxt.setText(getText());
		if (timeTxt != null) {
			Date date = getCreatedDateTime();
			SimpleDateFormat df = new SimpleDateFormat(Constants.DATETIME_FORMAT);
			String formatted = df.format(date);
			timeTxt.setText(formatted);
		}
		// Set thumbnail of tag image to imageview
		if (imgView != null) {
			if (bitmap != null)
				imgView.setImageBitmap(bitmap);
		}
		return super.updateListView(view);
	}

}
