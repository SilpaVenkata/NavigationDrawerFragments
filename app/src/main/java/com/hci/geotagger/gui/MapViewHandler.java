//package com.teampterodactyl.fragments;
package com.hci.geotagger.gui;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;

import com.hci.geotagger.objects.GeoLocation;
import com.hci.geotagger.objects.Tag;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.MapFragment;

import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

/**
 * TODO: DOCUMENTME
 * 
 * @author Paul Cushman
 *
 */
public class MapViewHandler {
	private static final String TAG = "MapViewHandler";

	private Context context;
	private Fragment mapFragment;
	
	private MapMarkerInfo [] locations;
	
	private String locationName;
	private View mapView; // view that is returned for onCreateView
	private GoogleMap mMap;
	
	private LatLngBounds.Builder builder = null;
	
	public void show() {
		// Initialize the location points builder
		builder = null;
		
		// Get a handle to the Map Fragment
		mMap = ((MapFragment)mapFragment).getMap();
		if (mMap != null) {
			mMap.setMyLocationEnabled(true);
//			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			
			if (locations != null) {
				for (MapMarkerInfo location : locations) {
					Log.d(TAG, "location.title="+location.title);
					Log.d(TAG, "location="+location.toString());
					
					if (location.position != null && (location.position.latitude != 0.0 &&
							location.position.longitude != 0.0)) {
						
					    if (builder == null)
					    	builder = new LatLngBounds.Builder();
						
					    builder.include(location.position);
						
//						CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location.position, 16);
						Marker mark = mMap.addMarker(new MarkerOptions().position(location.position).title(location.title));
						mark.showInfoWindow();
					
						location.mark = mark;

					}
				}
			} else {
				builder = null;
			}

			mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
				@Override
				public void onMapLoaded() {
					if (builder != null) {
						LatLngBounds tmpBounds = builder.build();
						CameraUpdate update = CameraUpdateFactory.newLatLngBounds(tmpBounds, 200);
						mMap.animateCamera(update, 250, null);
					}
				}
			});

			mMap.setOnInfoWindowClickListener(infoClickListener);
		}
	}
	
	GoogleMap.OnInfoWindowClickListener infoClickListener = new GoogleMap.OnInfoWindowClickListener() {
		@Override
		public void onInfoWindowClick(Marker arg0) {
			// TODO Need a way to find the associated location entry for this Marker
			// TODO Maybe create a string id that can be set on the Marker and used as a key to find the Location
			String gps = "geo:0,0?q=" + arg0.getPosition().latitude + ","
					+ arg0.getPosition().longitude + " (" 
					+ arg0.getTitle() + ")";
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(gps));
			intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
			context.startActivity(intent);
		}
	};
	
	/**
	 * Set the geolocation for map to zoom in on.
	 * 
	 * @param geo
	 *            The geographic location the map should zoom in on.
	 */
	public MapViewHandler(Fragment mapFragment, Context context) {
		this.locations = null;
		this.mapFragment = mapFragment;
		this.context = context;
	}

	public void addLocation(Tag tag) {
		if (tag == null)
			return;
		
		locations = new MapMarkerInfo[1];
		locations[0] = new MapMarkerInfo(tag.getName(), tag.getLocation());
	}
	
	public void addLocations(GeoLocation [] geos) {
		locations = new MapMarkerInfo[geos.length];
		for (int i=0; i<geos.length; i++) {
			locations[i] = new MapMarkerInfo(geos[i]);
		}
	}

	public void addLocations(ArrayList<Tag> tags) {
		locations = new MapMarkerInfo[tags.size()];
		for (int i=0; i<tags.size(); i++) {
			Tag tag = tags.get(i);
			if (tag != null) {
				locations[i] = new MapMarkerInfo(tag.getName(), tag.getLocation());
			}
		}
	}
	
	private class MapMarkerInfo {
		LatLng position;
		String title;
		public Marker mark;
		
		public MapMarkerInfo(GeoLocation geo) {
			position = new LatLng(geo.getLatitude(), geo.getLongitude());
			title = "";
		}
		
		public MapMarkerInfo(String title, GeoLocation geo) {
			position = new LatLng(geo.getLatitude(), geo.getLongitude());
			this.title = title;
		}
	}

}
