package com.hci.geotagger.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import android.view.View;
import android.widget.TextView;

import com.hci.geotagger.R;

/**
 * Stores a user account object that is taken from the database
 */
public class UserAccount extends GeotaggerObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int Type, Visibility, UserRating;
	private String uName, Name, Email, Description, Location, Quote;
	private String Image;
	private Date createdDateTime;
	//empty constructor for test objects
	public UserAccount(){};
	//init only required fields
	public UserAccount(int aID, String userName, int type, int vis, Date ts )
	{
		this.id = aID;
		this.uName = userName;
		this.setType(type);
		this.setVisibility(vis);
		this.setCreatedDateTime(ts);
		
		checkBoxID = R.id.row_checkbox;
	}

	//init only required fields
	public UserAccount(int aID, String userName, String email, String img,
			String desc, String loc, String quote, int type, int vis, Date ts, int rating )
	{
		this.id = aID;
		this.uName = userName;
		this.setEmail(email);
		this.setImage(img);
		this.setDescription(desc);
		this.setLocation(loc);
		this.setQuote(quote);
		this.setType(type);
		this.setVisibility(vis);
		this.setCreatedDateTime(ts);
		this.setUserRating(rating);

		checkBoxID = R.id.row_checkbox;
	}
	public UserAccount(int aID, String userName)
	{
		this.id = aID;
		this.uName = userName;
		checkBoxID = R.id.row_checkbox;
	}
	
	public String getuName() {
		return uName;
	}

	public void setuName(String uName) {
		this.uName = uName;
	}

	public int getType() {
		return Type;
	}
	public void setType(int type) {
		Type = type;
	}
	public int getVisibility() {
		return Visibility;
	}
	public void setVisibility(int visibility) {
		Visibility = visibility;
	}
	public int getUserRating() {
		return UserRating;
	}
	public void setUserRating(int userRating) {
		UserRating = userRating;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getEmail() {
		return Email;
	}
	public void setEmail(String email) {
		Email = email;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public String getImage() {
		return Image;
	}
	public void setImage(String image) {
		Image = image;
	}
	public Date getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	public String getLocation() {
		return Location;
	}
	public void setLocation(String location) {
		Location = location;
	}
	public String getQuote() {
		return Quote;
	}
	public void setQuote(String quote) {
		Quote = quote;
	}
	
	// define static values to instantiated object not needed
	public static final int LIST_LAYOUT_ID = R.layout.peoplerow;
	public static final int CHECK_LAYOUT_ID = R.layout.peoplerow_with_checkbox;
	public static final int CHECKBOX_ID = R.id.row_checkbox;


	@Override
	public int getListLayoutID() {
		return LIST_LAYOUT_ID;
	}
	
	@Override
	public View updateListView(View view) {
		TextView nameTxt = (TextView) view.findViewById(R.id.row_name);
		nameTxt.setText(getuName());                            
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
		TextView nameTxt = (TextView) view.findViewById(R.id.row_name);

		nameTxt.setText(getuName());                            
		
		return super.updateCheckableView(view);
	}
	
	@Override
	public boolean inList(ArrayList<?> objList) {
		ArrayList<UserAccount> list = (ArrayList<UserAccount>)objList;
		for (UserAccount ua : list) {
			if (id == ua.getId())
				return true;
		}
		return false;
	}
	
}
