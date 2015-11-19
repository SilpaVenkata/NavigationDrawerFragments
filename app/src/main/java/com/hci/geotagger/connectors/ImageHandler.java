package com.hci.geotagger.connectors;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.hci.geotagger.cache.CacheHandler;
import com.hci.geotagger.cache.CachePostAction;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.NetworkUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

/**
 * ImageHandler class takes care of actions relating to images
 * such as hosting them on the webserver and retrieving them
 * when needed.
 * 
 * Chris Loeschorn
 * Spring 2013
 */
public class ImageHandler extends GeotaggerHandler {
	JSONParser jsonParser;
	Context c;
	CacheHandler cache;
	private static final String TAG = "ImageHandler";
	public static final String NAME = "ImageHandler";
	protected String [] mActionsSupported = {
			WebAPIConstants.OP_UPLOAD_IMG
	};
	
	public ImageHandler(Context context) {
		super(context, WebAPIConstants.IMAGE_URL);
		setActionList(mActionsSupported);
		
		this.c = context;
		jsonParser = new JSONParser();
		cache = new CacheHandler(context);
	}
	
	/**
	 * The Tag Handler supports the server database Add Operation.
	 * This is a GeotaggerHandler method that identifies if the input operation is a
	 * Tag Handler Add operation.
	 */
	@Override
	protected boolean isAddOperation(String operation) {
		return operation.equals(WebAPIConstants.OP_UPLOAD_IMG);
	}

	private int indexOfNVPEntry(List<NameValuePair> params, String key) {
		for (int i=0; i<params.size(); i++) {
			NameValuePair nvp = params.get(i);
			if (nvp.getName().equalsIgnoreCase(key))
				return i;
		}
		return -1;
	}
	
	/**
	 * Internal method to add a Tag to the server.  This method makes the specific JSON calls
	 * to add the record to the database.
	 * {
  	 *   "upload": 
  	 *   {
  	 *     "file": "<base 64 content here>",
  	 *     "media": "image"
  	 *   }
  	 * }
	 * @param params
	 * @return
	 */
//	@Override
	public ReturnInfo addToServerDB(JSONObject params) {
		ReturnInfo result;
		Log.d(TAG, "Entering addToServerDB");
		JSONObject uploadObject;
		JSONObject addObject;
		
		// If this is a cached entry then need to get the bitmap from the cached file
		if (params.has("cacheImageURL")) {
			String urlFileName;
			try {
				urlFileName = params.getString("cacheImageURL");
			} catch (Exception e) {
				return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
			}
			
			if (!cache.imageExists(urlFileName)) {
				return null;
			}
			Bitmap pic = cache.decodeCacheBitmap(urlFileName);

			//encode image to base64
			String encodedImg = encodeImage(pic);
				
			// Building Parameters
			addObject = new JSONObject();
			uploadObject = new JSONObject();
			try {
				uploadObject.put("file", encodedImg);
				uploadObject.put("media", "image");
				addObject.put("upload", uploadObject);
			} catch (Exception e) {
			}

		} else {
			addObject = params;
		}

		try {
			String url = String.format(WebAPIConstants.ACC_FORMAT_UPLOAD, WebAPIConstants.BASE_URL_GTDB);
			JSONObject json = jsonParser.postToServer(url, addObject);
			
			Log.d(TAG, "addToServerDB: JSON Response from PHP: " + json.toString());
			result = new ReturnInfo(json);
			Log.d(TAG, "addToServerDB: Result is " + result.success);
			if (result.success) {
				Long documentID = json.getLong("documentID");
				result.object = documentID;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			result = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
		
		Log.d(TAG, "Leaving addToServerDB");
		return result;
	}
	
	/**
	 * Uploads an image to the server associated with a tag
	 * @param b the bitmap object to upload to server
	 * @return the return info object returning result of upload operation, including success or failure as well as image url that is used throughout application
	 */
	public ReturnInfo uploadImageToServer(Bitmap b) {
		ReturnInfo retValue = null;
		Log.d(TAG, "Entering uploadImageToServer");

		//encode image to base64
		String encodedImg = encodeImage(b);
			
		// Building Parameters
		JSONObject params = new JSONObject();
		JSONObject uploadObject = new JSONObject();
		try {
			uploadObject.put("file", encodedImg);
			uploadObject.put("media", "image");
			params.put("upload", uploadObject);
		} catch (Exception e) {
			return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
		}
	        
		// perform cached actions before this action, also returns false if network is down
		if (cache.performCachedActions()) {
			retValue = addToServerDB(params);
		} else {
			String url = "";
			
			// Add the bitmap image to the cache, and use the temporary key returned
			url = cache.add2Cache(b);

			if (url != null) {
				retValue = new ReturnInfo();
				retValue.url = url;
				retValue.detail = ReturnInfo.FAIL_NONETWORK;

				// Building Parameters
				params = new JSONObject();
				try {
					params.put("operation", WebAPIConstants.OP_UPLOAD_IMG);
					params.put("cacheImageURL", url.toString());
				} catch (Exception e) {
					e.printStackTrace();
					return new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
				}

				// TODO: Create post commands for the Action cache record.
		        List<CachePostAction> postParams = new ArrayList<CachePostAction>();
		        postParams.add(new CachePostAction(CacheHandler.ACTION_UPDATE_POSTOP, CacheHandler.ACTION_IMAGEURL_POSTID, url.toString()));
				
				cache.cacheAction(NAME, WebAPIConstants.OP_UPLOAD_IMG, params, postParams);

			} else {
				retValue = new ReturnInfo(ReturnInfo.FAIL_NONETWORK);
			}
		}
		
		Log.d(TAG, "Leaving uploadImageToServer: retValue.url(Bitmap)="+retValue.url);
		return retValue;
	}
	
	/**
	 * Encodes bitmap image as base64 encoded string for uploading and saving to server
	 * @param b bitmap to save to server
	 * @return String base64 representation of bitmap
	 */
	private String encodeImage(Bitmap b)
	{
		// Compress the image to JPEG to make size smaller,
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// store bytes in output stream
		b.compress(Bitmap.CompressFormat.PNG, Constants.IMAGE_QUALITY, os);
		// encode image bytes to base 64
		byte[] bytes = os.toByteArray();
		String encodedImg = Base64.encodeToString(bytes, Base64.DEFAULT);
		
		return encodedImg;
	}
	
	
	public String encodeImage(File f) {
		//first check the size of the image file without getting pixels
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
		
		int height = options.outHeight;
		int width = options.outWidth;
		Log.d("Image Size", "H, W = " + height + ", " + width);
		//resize image if it is very large to avoid out of memory exception
		if (height > 2048 || width > 2048)
			options.inSampleSize = 4;
		else if(height > 1024 || width > 1024)
			options.inSampleSize = 2;
		
		//get bitmap pixels
		options.inJustDecodeBounds = false;
		b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
		height = b.getHeight();
		width = b.getWidth();
		Log.d("New Image Size", "H, W = " + height + ", " + width);
		if (height > 0 && width > 0) {
			return encodeImage(b);
		}
		return "";
	}	
	
	/**
	 * Calculates the closest value to desired image size for inSampleSize option. This is to save and modify
	 * bitmap without using up too much memory which may cause a crash on older phones.
	 * @param o BitmapFactory.Options object to use for scaling including decoding bounds and sample size
	 * @param newWidth new desired width of bitmap
	 * @param newHeight new desired height of bitmap
	 * @return returns sample size of bimtap
	 */
	private int getSampleSize(BitmapFactory.Options o, int newWidth, int newHeight)
	{
		//get image width and height
		int sampleSize = 1;
		int imgWidth = o.outWidth;
		int imgHeight = o.outHeight;
		
		
		if(imgHeight > newHeight || imgWidth > newWidth)
		{
			if (imgWidth > imgHeight)
				sampleSize = Math.round((float) imgHeight / (float) newHeight);
			else
				sampleSize = Math.round((float) imgWidth / (float) newWidth);
		}
		
		return sampleSize;
	}
	
	/**
	 * retrieve a bitmap from the given URL, scaled to fit the given max dimensions.
	 * @param imgUrl url of the image previously saved to the server. The url of image is returned from server
	 * @param maxWidth max width desired for scaled bitmap
	 * @param maxHeight max height desired for the scaled bitmap
	 * @return the newly scaled bitmap
	 */
	public Bitmap getScaledBitmapFromUrl(String imgUrl, int maxWidth, int maxHeight)
	{
		Bitmap pic = null;
		Log.d(TAG, "Entering getScaledBitmapFromUrl: Bitmap="+imgUrl);

		if (imgUrl == null || imgUrl.length() == 0) {
			Log.d(TAG, "Leaving getScaledBitmapFromUrl");
			return null;
		}
		
		try {
			//create a url for the image url
			URL url = new URL(imgUrl);
			InputStream stream;
			
			// If there is not cache then get the image from the server
			if (cache == null) {
				// If the Network is UP then upload
				if (NetworkUtils.isNetworkUp(c)) {
					//options for decoding the image
					BitmapFactory.Options o = new BitmapFactory.Options();
					//get the image dimensions (without the data) to calcualte samplesize
					o.inJustDecodeBounds = true;
				    BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, o);
				    o.inSampleSize = getSampleSize(o, maxWidth, maxHeight);
				    //reset options to decode whole image
				    o.inJustDecodeBounds = false;
				    pic = BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, o);
				}
			} else {
//				String imageCacheKey = url.toString() + "." + maxWidth + "." + maxHeight;
				String imageCacheKey = url.toString();
				
				// If the image exists in the cache then decode and use it
				if (cache.imageExists(imageCacheKey)) {
					// TODO: need to compare image version number with that on the server
					
					BitmapFactory.Options o = new BitmapFactory.Options();
				    o.inSampleSize = getSampleSize(o, maxWidth, maxHeight);
				    o.inJustDecodeBounds = false;

					pic = cache.decodeCacheBitmap(imageCacheKey, o);
				} else {
					// If the URL is NOT cached then get the URL stream and cache it
					if (NetworkUtils.isNetworkUp(c)) {
						// Get the default sized image from the server
						Bitmap serverpic = BitmapFactory.decodeStream(url.openConnection().getInputStream());
						cache.add2Cache(imageCacheKey, serverpic);

						BitmapFactory.Options o = new BitmapFactory.Options();
					    o.inSampleSize = getSampleSize(o, maxWidth, maxHeight);
					    o.inJustDecodeBounds = false;

						pic = cache.decodeCacheBitmap(imageCacheKey, o);

						/*
						//options for decoding the image
						BitmapFactory.Options o = new BitmapFactory.Options();
						//get the image dimensions (without the data) to calcualte samplesize
						o.inJustDecodeBounds = true;
					    BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, o);
	
					    o.inSampleSize = getSampleSize(o, maxWidth, maxHeight);
					    //reset options to decode whole image
					    o.inJustDecodeBounds = false;
					    
					    pic = BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, o);
						*/
					}
					
				}
				
				/*
				if (stream != null) {
					//options for decoding the image
					BitmapFactory.Options o = new BitmapFactory.Options();
					//get the image dimensions (without the data) to calcualte samplesize
					o.inJustDecodeBounds = true;
				    BitmapFactory.decodeStream(stream, null, o);
				    o.inSampleSize = getSampleSize(o, maxWidth, maxHeight);
				    //reset options to decode whole image
				    o.inJustDecodeBounds = false;
				    
				    pic = BitmapFactory.decodeStream(cache.getCacheInputStream(imageCacheKey), null, o);
				} else {
					//options for decoding the image
					BitmapFactory.Options o = new BitmapFactory.Options();
					//get the image dimensions (without the data) to calcualte samplesize
					o.inJustDecodeBounds = true;
				    BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, o);
				    o.inSampleSize = getSampleSize(o, maxWidth, maxHeight);
				    //reset options to decode whole image
				    o.inJustDecodeBounds = false;
				    pic = BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, o);
				}
				*/
			}
		} 
		catch (MalformedURLException e) {
			Log.e(TAG, "getScaledBitmapFromUrl: Error parsing image URL.");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "getScaledBitmapFromUrl: Error decoding bitmap from URL.");
			e.printStackTrace();
		}
		
		Log.d(TAG, "Leaving getScaledBitmapFromUrl");
		return pic;
	}
	
	/**
	 *  get absolute image path from MediaStore URI for gallery images
	 * @param contentUri the uri for the bitmap image
	 * @return returns the string of the fully qualified path to the image file
	 */
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = c.getContentResolver().query(contentUri, proj, null,
				null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
	/**
	 * Added March 2014 works on newer android devices, such as Nexus 7.
	 * @param context the desired android context
	 * @param uri for the bitmap image
	 * @return returns the string of the fully qualified path to the image file
	 */
	public  String getFileNameByUri(Context context, Uri uri)
	{
		 Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		   cursor.moveToFirst();
		   String document_id = cursor.getString(0);
		   document_id = document_id.substring(document_id.lastIndexOf(":")+1);
		   cursor.close();

		   cursor = context.getContentResolver().query( 
		   android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		   null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
		   cursor.moveToFirst();
		   String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
		   cursor.close();
		   
		   	return path;
	}

	/**
	 *  Return the path of the directory to store Images on the device (as a file)
	 * @return the file object of the image
	 */
	public File getImageAlbum() {
		Log.d(TAG, "Entering getImageAlbum");

		File imageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				Constants.ALBUM_NAME);

		// create the dir tree if it does not exist
		if (!imageDir.exists()) {
			if (! imageDir.mkdirs())
				imageDir = null;
		}

		Log.d(TAG, "Leaving getImageAlbum");
		return imageDir;
	}
	
	/**
	 * Make a file to save the captured image to the device
	 * @return the file of the saved capture image
	 */
	public File makeImageFile() {
	    // Create an image file name
	    String timeStamp = 
	        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = Constants.IMAGE_PREFIX + timeStamp + Constants.IMAGE_EXT;
	    File image;

		if(getImageAlbum() != null)
		{
			image = new File(getImageAlbum(), imageFileName);
			return image;
		}
	    return null;   
	}
	
}
