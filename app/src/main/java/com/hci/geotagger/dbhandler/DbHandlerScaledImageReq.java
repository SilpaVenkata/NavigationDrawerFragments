package com.hci.geotagger.dbhandler;

/**
 * This class is used for requests for scaled images.
 * 
 * @author Paul Cushman
 */
public class DbHandlerScaledImageReq {
	public String [] urls;
	public int width;
	public int height;
	
	public DbHandlerScaledImageReq() {
	}
	public DbHandlerScaledImageReq(int width, int height, String [] urls) {
		this.urls = urls;
		this.width = width;
		this.height = height;
	}
}
