package com.hci.geotagger.activities;

import com.hci.geotagger.R;
import com.hci.geotagger.connectors.ImageHandler;
import com.hci.geotagger.gui.ScaleImageView;
import com.hci.geotagger.gui.ScaleImageView.ScaleImageCallbacks;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This class implements the extended view of the comments and allows the user to 
 * click on a comment in the TagViewActivity and bring up this activity. This activity
 * will enlarge the photo from the comment drawer and will also enlarge the comment 
 * string itself.
 * 
 * @author: Spencer Kordecki
 * @date: 10/23/13
 */

public class CommentViewActivity extends Activity implements ScaleImageCallbacks {
	private TextView tagComment;	//default comment
	private ScaleImageView tagPicture;	//picture attached to the comment
	private RelativeLayout layout;
	private LinearLayout background;

	private Intent intent;			//intent used for getting extras
	private String commentText;		//text of the comment
	private String imgURL;			//the url of the comment image
	
	private int width;
	private int height;
	private boolean zoomed = false;
	
	/**
	 * Overidded onCreate method that sets the comment and image for the associated tag that was selected.
	 * This class is instantiated and used from TagViewActivity so user can view tag close up. Possibly crashes app if
	 * tag comment is too big to be contained in this view's bounds, please be careful of this fact.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_comment_extended_view);
		
		layout = (RelativeLayout) findViewById(R.id.commentExtended_layout);
		/*
		Animation fadeIn;
		fadeIn = AnimationUtils.loadAnimation(this, R.anim.grow_fade_in_center);
		layout.setAnimation(fadeIn);
		*/
		intent = getIntent();
		
		tagComment = (TextView) findViewById(R.id.commentExtended_commentTag);
		tagPicture = (ScaleImageView) findViewById(R.id.commentExtended_commentImg);
		tagPicture.setCallbacks(this);
		tagPicture.setHandleTouchEvents(false);

		layout.setVisibility(View.GONE);
		
		background = (LinearLayout) findViewById(R.id.commentExtended_background);
		background.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});


		commentText = intent.getStringExtra("commentText");
		imgURL = intent.getStringExtra("imgURL");
		
		if(!commentText.equals(""))
		{			
			Log.d("Before", commentText);
			tagComment.setText(intent.getStringExtra("commentText"));
			Log.d("After", tagComment.getText().toString());
		}
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;
		
		if(!imgURL.equals("")) {
			new DownloadImage().execute(imgURL);
		} else {
			showDialog();
		}
	}
	
	
	private void showDialog() {
		layout.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onBackPressed() {
		if (zoomed) {
			RelativeLayout.LayoutParams lp;
			
			zoomed = ! zoomed;
			lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			tagPicture.setLayoutParams(lp);
		} else {
			finish();
		}
	}

	/*
	 * Async class used to download the image of the comment and populate the ImageView
	 */
	private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
		ImageHandler handler = new ImageHandler(CommentViewActivity.this);
		
		@Override
		protected Bitmap doInBackground(String... urls) {
//			return handler.getScaledBitmapFromUrl(urls[0], R.dimen.image_width, R.dimen.image_height);
			int dimen = Math.max(width, height);
			
			return handler.getScaledBitmapFromUrl(urls[0], dimen, dimen);
		}
		
		protected void onPostExecute(Bitmap result) {
			if (tagPicture != null) {
//				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//				tagPicture.setLayoutParams(lp);
				tagPicture.setImageBitmap(result);
				tagPicture.setHandleTouchEvents(true);
			}
			showDialog();
		}
	}
	
	@Override
	public boolean doubleClickCallback() {
		if (imgURL == null || imgURL.length() == 0)
			return false;

		RelativeLayout.LayoutParams lp;
		
		zoomed = ! zoomed;
		if (zoomed) {
			lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		} else {
			lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
		tagPicture.setLayoutParams(lp);
		/*
		Intent intent = new Intent(this, FullScreenImageActivity.class);
		intent.putExtra(Constants.EXTRA_IMAGEID, imgURL);
		startActivity(intent);
		*/
		return true;
	}
}