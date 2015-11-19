package com.hci.geotagger.objects;

import java.io.Serializable;

/**
 * This class us used to create and modify a geographic location. This class is often used
 * when instantiating tags, and can be used later to display location that tag was created on a MapView
 * 
 *
 */
public class GeoLocation implements Serializable {

	private double latitude, longitude;
	
	public GeoLocation(double lat, double lon)
	{
		this.setLatitude(lat);
		this.setLongitude(lon);
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
