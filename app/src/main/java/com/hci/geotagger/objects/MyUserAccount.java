package com.hci.geotagger.objects;

import java.util.Date;

public class MyUserAccount extends UserAccount {
	private String Password;
	
	/**
	 *  User account constructor. Used to compare verify and save login information returned from server.
	 * @param aID User id
	 * @param userName Users username used for logging in
	 * @param pw Users password used for logging in
	 * @param type The type of user account
	 * @param vis The visibility of the user account
	 * @param ts The data of registration
	 */
	public MyUserAccount(int aID, String userName, String pw, int type, int vis, Date ts )
	{
		super(aID, userName, type, vis, ts);
		this.setPass(pw);	
		
	}
	//init only required fields
	public MyUserAccount(int aID, String userName, String email, String pw, String img,
			String desc, String loc, String quote, int type, int vis, Date ts, int rating )
	{
		super(aID, userName, email, img, desc, loc, quote, type, vis, ts, rating);
		this.setPass(pw);	
	}
	public MyUserAccount(int aID, String userName, String pw)
	{
		super(aID, userName);
		this.setPass(pw);		
	}

	public String getPass() {
		return Password;
	}
	public void setPass(String password) {
		Password = password;
	}
}
