//package com.teampterodactyl.fragments;
package com.hci.geotagger.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.dbhandler.DbHandlerResponse.DbMessageResponseInterface;
import com.hci.geotagger.objects.Comment;
import com.hci.geotagger.objects.Tag;
import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.common.BaseFragment;
import com.hci.geotagger.common.Constants;

import android.app.*;
 

/**
 * CommentViewFragment implements the fragment that allows for the viewing of
 * comments in the TagViewActivity. When the user clicks on the Comments button in
 * the TagView, this fragment will be displayed. The comments are displayed in a 
 * ListView and can be clicked on for more information.
 * 
 * @author: Spencer Kordecki
 * @version: January 20, 2014
 */
public class CommentViewFragment extends BaseFragment implements DbMessageResponseInterface {
	private final static String TAG = "CommentViewFragment";
	
	private View fragmentView; //view that is returned for onCreateView
	private ListView commentList; //ListView to display comments
	//private String[] comments; //a dummy array of comments
	private ArrayList<Comment> comments;

	private Context context; //context

	private CommentAdapter commentAdapter;
	private Intent extendedComment;
	private ICommentViewCallBack callback;
	
	private ImageView img_commentImage;
	private EditText commentTxt;
	private Button commentBtn;
	private Drawable icon;
	
	private Tag currentTag;
	Integer position = null; //Is this necessary? -SK 9/2
	
	private static DbHandlerResponse rspHandler;
	private GeotaggerApplication mApp = null;

	/**
	 * ICommentViewCallBack Interface should be used by classes instantiating the commentViewFragment class. 
	 * This allows the class to fill in text, images, and other information once the CommentView has actually been 
	 * created. Trying to add text and other data to a newly instantiated commentView may cause a crash if the view has 
	 * yet been fully created. Callback methods are called in this classes onCreateView method and should be 
	 * implemented in the class that instantiated the view to add information to the newly created class.
	 *
	 */
	public interface ICommentViewCallBack
	{
		public void onCreateCommentViewCallback(boolean refresh);

		void onCreateDescriptionViewCallback();
	}
	
	/**
	 * Creates the view that the user will see when the fragment is created, similar 
	 * to onCreate methods seen in activities.
	 */
	public CommentViewFragment(ICommentViewCallBack callback)
	{
		this.callback = callback;
	}
	
	public CommentViewFragment() {		
	}
	
	public void setTagIndex(Tag tag) {
		currentTag = tag;
	}
	
	public void setCommentThumbCache(HashMap<String, Bitmap> thumbCache) {
		if (commentAdapter != null)
			commentAdapter.setThumbCache(thumbCache);
	}

	/**
	 * Overrided onCreateView method that creates the CommentViewFragment. Callback occurs at bottom
	 * to instantiating class to add contents to newly created view
	 */
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Log.d(TAG, "Entering onCreateView");
    	
        fragmentView = inflater.inflate(R.layout.fragment_comment_view, container, false);
        
        commentList = (ListView) fragmentView.findViewById(R.id.comment_list);
        
        context = this.fragmentView.getContext();  
        
        comments = new ArrayList<Comment>();
        
        commentAdapter = new CommentAdapter(context,com.hci.geotagger.R.layout.commentrow,comments);
        
        commentList.setAdapter(commentAdapter); 
        commentList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> av, View v, int position, long id)
			{
				Comment comment = comments.get(position);
				extendedComment = new Intent(context,CommentViewActivity.class);
				
				extendedComment.putExtra("commentText", comment.getText().toString());

				if(comment.getImageURL() != null)
				{
					Log.d(TAG, "getImage: " + comment.getImageURL());
					extendedComment.putExtra("imgURL", comment.getImageURL());
				}
				else
				{
					extendedComment.putExtra("imgURL", "");
				}
				startActivity(extendedComment);
			}
		});
        
        
		commentBtn = (Button) fragmentView.findViewById(R.id.tagView_btnNewComment);
		//add comment when comment button is pressed
		commentBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View view0) {
				Intent intent = new Intent(context, AddCommentActivity.class);
				intent.putExtra(Constants.EXTRA_TAGID, currentTag.getId());
				startActivityForResult(intent, Constants.ADD_COMMENT_ACTIVITY_RESULT);
			}
		});

		icon = getResources().getDrawable( R.drawable.icon );
        
        //now that view is created, we can start retrieving and populating comments
                
        callback.onCreateCommentViewCallback(false);
        
		mApp = (GeotaggerApplication)getActivity().getApplication();
		if (mApp != null) {
			rspHandler = new DbHandlerResponse(TAG, getActivity(), this);
			mApp.addResponseHandler(rspHandler);
		}

    	Log.d(TAG, "Leaving onCreateView");

        return fragmentView; //view must be returned
    }
    
	@Override
	public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
		Log.d(TAG, "Entered DBGetCallback");
		switch (action) {
		case DbHandlerConstants.DBMSG_ADD:
			if (response != null && response instanceof Comment) {
				Comment comm = (Comment)response;
				comments.add(comm);
				position = comments.size()-1;
				
				if (position != null) {
					commentAdapter.add(comments.get(position));
					commentAdapter.notifyDataSetChanged();
					commentList.setSelection(position);
				}
				position = null;
				commentTxt.setText("");
				stopProgress();
				String msg = this.getResources().getString(R.string.toast_addcomment_success);
				Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
				img_commentImage.setImageDrawable(icon);
			}
			break;
		}
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState) 
    {
    	super.onSaveInstanceState(outState);
        //outState.putInt("curChoice", mCurCheckPosition);
    }

    public void notifyCommentAdapterDataChanged()
    {	
    	this.commentAdapter.notifyDataSetChanged();
    }

    public void addComment(Comment c)
    {
    	this.commentAdapter.add(c);
    }
    
    public void clearComments() {
    	this.commentAdapter.clear();
    }
    
    public void setCommentListSelection(int position)
    {
    	this.commentList.setSelection(position);
    }
    
    
    private class CommentAdapter extends ArrayAdapter<Comment> {
    	
		private ArrayList<Comment> comments;
		Context c;
		private HashMap<String, Bitmap> thumbCache;

		/**
		 * Constructor for the CommentAdapter
		 */
		public CommentAdapter(Context context, int textViewResourceId, ArrayList<Comment> comments) {
			super(context, textViewResourceId, comments);
			this.comments = comments;
			this.c = context;
			thumbCache = new HashMap<String, Bitmap>();
		}
		
		public void setThumbCache(HashMap<String, Bitmap> thumbCache) {
			this.thumbCache = thumbCache;
		}

		/*
		 * Returns the id of the item at a specified position
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater vi = ((Activity)this.c).getLayoutInflater();
						//(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = vi.inflate(R.layout.commentrow, null);
			}

			Comment comment = comments.get(position);
			if (comment != null) {
				TextView nameTxt = (TextView) row.findViewById(R.id.commentrow_txtName);
				TextView timeTxt = (TextView) row.findViewById(R.id.commentrow_txtTime);
				TextView commentTxt = (TextView) row.findViewById(R.id.commentrow_txtdesc);
				ImageView commentImg = (ImageView) row.findViewById(R.id.commentrow_thumbnail);

				if (nameTxt != null) 
					nameTxt.setText(comment.getUsername()); 

				if (timeTxt != null) {
					Date date = comment.getCreatedDateTime();
					SimpleDateFormat df = new SimpleDateFormat(Constants.DATETIME_FORMAT);
					String formatted = df.format(date);
					timeTxt.setText(formatted);
				}

				if (commentTxt != null)
					commentTxt.setText(comment.getText().toString());

				if (commentImg != null) {
					Bitmap bitmap2show = null;
					if (comment.getImageURL() != null) {
						String url = comment.getImageURL();
						// first try to get image from cache
						Log.d(TAG, "loadImageNull: URL: " + url);

						if(!url.equals("")) {							
							if (thumbCache.containsKey(url)) {
								bitmap2show = thumbCache.get(url);
								Log.d(TAG, "Got image from cache!");
							}	
						}
					}
					
					if (bitmap2show == null) {
						bitmap2show = BitmapFactory.decodeResource(c.getResources(), R.drawable.icon);
					}
					commentImg.setImageBitmap(bitmap2show);
				} // end if imgview
			}
			
			return row;
		}
	}

	/**
	 * When the image is selected in the gallery, show it in the ImageView
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch(requestCode)
			{
			//if new picture is taken, show that in the image view
			case Constants.ADD_COMMENT_ACTIVITY_RESULT:
		        callback.onCreateCommentViewCallback(true);
				break;
			}
		}
	}
}
