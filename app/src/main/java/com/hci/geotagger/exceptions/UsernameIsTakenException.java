package com.hci.geotagger.exceptions;

import com.hci.geotagger.common.Constants;

public class UsernameIsTakenException extends Exception{

	private static final long serialVersionUID = 1L;

	public UsernameIsTakenException()
	{
		super(Constants.USERNAME_IS_TAKEN);
	}
}
