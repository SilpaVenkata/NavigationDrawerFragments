package com.hci.geotagger.dbhandler;

import android.graphics.Bitmap;

/**
 * This class is used to support the scaled image responses
 * 
 * @author Paul Cushman
 */
public class DbHandlerScaledImageRsp {
	public String url;
	public Bitmap bitmap;
	
	public DbHandlerScaledImageRsp() {
	}
	public DbHandlerScaledImageRsp(String url, Bitmap bitmap) {
		this.url = url;
		this.bitmap = bitmap;
	}
}
