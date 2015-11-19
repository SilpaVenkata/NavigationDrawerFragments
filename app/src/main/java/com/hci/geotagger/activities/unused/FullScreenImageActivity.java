package com.hci.geotagger.activities.unused;

import java.io.File;
import java.text.DecimalFormat;
//import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.util.DisplayMetrics;
import android.util.Log;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.CommentViewFragment.ICommentViewCallBack;
import com.hci.geotagger.activities.DescriptionViewFragment.IDescriptionViewCallback;
import com.hci.geotagger.activities.common.BaseActivity;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.ImageHandler;
import com.hci.geotagger.connectors.ReturnInfo;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageReq;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.dbhandler.DbHandlerScaledImageRsp;
import com.hci.geotagger.gui.MapViewHandler;
import com.hci.geotagger.gui.ScaleImageView;
import com.hci.geotagger.gui.ScaleImageView.ScaleImageCallbacks;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.Comment;
import com.hci.geotagger.objects.GeoLocation;
import com.hci.geotagger.objects.Tag;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.widget.FrameLayout;

public class FullScreenImageActivity extends Activity implements DbMessageResponseInterface,
	ScaleImageCallbacks {
	private static final String TAG = "FullScreenImageActivity";

	ScaleImageView img_Image;
	GeotaggerApplication mApp;
	ImageHandler imageHandler;
	DbHandlerResponse rspHandler;
	
	int imageGetID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.full_screen_image);
		
		mApp = (GeotaggerApplication)getApplication();
		if (mApp != null) {
			rspHandler = new DbHandlerResponse(TAG, this, this);
			mApp.addResponseHandler(rspHandler);
		}
		
		// first init tag and image handlers
		imageHandler = new ImageHandler(this);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle.containsKey(Constants.EXTRA_IMAGEID)) {
			String imageID = bundle.getString(Constants.EXTRA_IMAGEID);
			loadImage(imageID);
		} else {
			finish();
		}
		
		img_Image = (ScaleImageView)findViewById(R.id.imageView);
		img_Image.setCallbacks(this);
		img_Image.setHandleTouchEvents(false);
		
		if (savedInstanceState != null) {
			
		} else {
			
		}
	}
	
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		Log.d(TAG, "Entered DBGetCallback");
		if (action == DbHandlerConstants.DBMSG_GET_SCALED_IMAGES) {
			if (msgID == imageGetID) {
				if (success) {
					DbHandlerScaledImageRsp sir = (DbHandlerScaledImageRsp)response;
					img_Image.setImageBitmap(sir.bitmap);
					img_Image.setHandleTouchEvents(true);
				} else {
					finish();
				}
			}
		}
	}

	/**
	 * Load the tag's image from the URL and into the ImageView
	 */
	private void loadImage(String imgUrl) {
		DbHandlerScaledImageReq gsi = new DbHandlerScaledImageReq();
		DisplayMetrics metrics = new DisplayMetrics();        
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		gsi.width = metrics.widthPixels;
		gsi.height = metrics.heightPixels;
		gsi.urls = new String[1];
		gsi.urls[0] = imgUrl;
		imageGetID = mApp.sendMsgToDbHandler(rspHandler, this, DbHandlerConstants.DBMSG_GET_SCALED_IMAGES, gsi);
	}


	@Override
	public boolean doubleClickCallback() {
		finish();
		return true;
	}

}
