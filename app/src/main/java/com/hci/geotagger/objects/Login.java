package com.hci.geotagger.objects;

public class Login extends GeotaggerObject {
	private static final long serialVersionUID = 1L;

	public String username;
	public String password;
	public String access_token;
	public String refresh_token;
	
	public Login(String username, String password) {
		this.username = username;
		this.password = password;
	}
}
