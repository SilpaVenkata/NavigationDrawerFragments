package com.hci.geotagger.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.hci.geotagger.R;
import com.hci.geotagger.activities.common.BaseFragment;
import com.hci.geotagger.common.Constants;
import com.hci.geotagger.common.UserSession;
import com.hci.geotagger.connectors.ImageHandler;
import com.hci.geotagger.gui.GenericEntryListAdapter;
import com.hci.geotagger.gui.ScaleImageView;
import com.hci.geotagger.objects.Adventure;
import com.hci.geotagger.objects.Tag;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class AddImageFragment extends BaseFragment {
	private static final String TAG = "AddImageFragment";
	
	private View fragmentView; //view that is returned for onCreateView

	private File CURRENT_IMAGE, TEMP_IMAGE;
	private Uri CUR_IMGURI, TMP_IMGURI;
	private boolean HAS_IMAGE = false;
	private boolean IMG_CHANGED = false;
	
	private LinearLayout imageButton_camera;
	private LinearLayout imageButton_gallery;
	private LinearLayout imageButton_rotate;
	private ScaleImageView imgView;
	
	ImageHandler imageHandler;

	public interface UpdateDatabaseListener {
	    public void onDatabaseChanged();
	}
	
	public AddImageFragment() {
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_add_image, container, false);
	
		imageHandler = new ImageHandler(getActivity());
		
		//get form control IDs
		imgView = (ScaleImageView) fragmentView.findViewById(R.id.addimage_imgView);
		imgView.setHandleTouchEvents(false);
		registerForContextMenu(imgView);
		
		imageButton_camera = (LinearLayout) fragmentView.findViewById(R.id.addimage_imageCamera);
		imageButton_gallery = (LinearLayout) fragmentView.findViewById(R.id.addimage_imageGallery);
		imageButton_rotate = (LinearLayout) fragmentView.findViewById(R.id.addimage_imageRotate);
		
		imageButton_rotate.setVisibility(View.GONE);
		imageButton_camera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
     		   openCamera();
			}
		});
		imageButton_gallery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
     		   openGallery();
			}
		});
		imageButton_rotate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bitmap bitmap;
				if (CUR_IMGURI != null) {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 1;
					bitmap = null;
					while (options.inSampleSize < 16) {
						try {
							bitmap = BitmapFactory.decodeFile(CUR_IMGURI.getPath(), options);
							break;
						} catch (OutOfMemoryError e) {
							Log.e(TAG, "Out of memory, sample size="+options.inSampleSize+"!");
						}
						options.inSampleSize *= 2;
					}
// TODO: Need way to get activities bitmap
//				} else if (comment != null) {
//					bitmap = comment.getBitmap();
				} else {
					return;
				}
				if (bitmap == null)
					return;
				setupProgress(getActivity().getResources().getString(R.string.progress_rotate_image));
				showProgress();

				new RotateImage().execute(bitmap);
			}
		});
		
		// Image selection button action
		imgView.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				//items for the dialog
				String[] items = new String[] {"Camera","Gallery"};
				//create dialog with onClick listener for the list items
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			    builder.setTitle(R.string.dlg_tagimg_title).setItems(items, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int which) {
			    		//if camera was clicked, open camera
			    		if (which == 0) {
			    			openCamera();
			    		}
			    		//if gallery was clicked, open gallery. 
			    		if (which == 1) {
			    			openGallery();
			    		}
			    	}
			    });
				builder.show();
			}
		});
		
		//show context menu to delete image on long press of imgView
		imgView.setOnLongClickListener(new OnLongClickListener() {	
		    @Override
		    public boolean onLongClick(View v) {
		    	getActivity().openContextMenu(imgView);
		        return true;
		    }
		});
		

		


		return fragmentView;
	}
	
	UpdateDatabaseListener mCallback = null;
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	}
	
	/**
	 * When the image is selected in the gallery, this method shows it in ImageView
	 */
	/**
	 * This method is handle the result codes returned from the Camera or Gallery activities.
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK)
        {
        	switch(requestCode)
            {
        		//if image is selected from gallery, show it in the image view
        		case Constants.SELECT_IMG:
        			Uri selectedImageUri = data.getData();
                
        			//update current image file from uri
        			//String realImgPath = imageHandler.getRealPathFromURI(selectedImageUri);
        			String realImgPath = imageHandler.getFileNameByUri(getActivity().getBaseContext(), selectedImageUri);//for kit kat and newer devices

        			CUR_IMGURI = Uri.parse(realImgPath);
        			CURRENT_IMAGE = new File(CUR_IMGURI.getPath());
				
        			//display image in imageview
        			imgView.setImageURI(CUR_IMGURI);
        			HAS_IMAGE = true;
        			break;
                //if new picture is taken, show that in the image view
        		case Constants.CAPTURE_IMG:
        			//if the image was saved to the device use the URI to populate image view and set the current image
        			if (TMP_IMGURI != null)
        			{        				
        				CUR_IMGURI = TMP_IMGURI;
        				CURRENT_IMAGE = new File(CUR_IMGURI.getPath());
        				TMP_IMGURI = null;
        				
        				imgView.setImageURI(CUR_IMGURI);
        				HAS_IMAGE = true;
        			}
        			break;
            }
        	if (HAS_IMAGE) {
				imageButton_rotate.setVisibility(View.VISIBLE);
			} else {
				imageButton_rotate.setVisibility(View.GONE);
			}
        }
        //if user backed out of the camera without saving picture, discard empty image file
        else
        {
        	if (TEMP_IMAGE != null)
        		TEMP_IMAGE.delete();
        	
        	TMP_IMGURI = null;
        }
    }
	
	public void clearImage() {
		HAS_IMAGE = false;
		CURRENT_IMAGE = null;
		CUR_IMGURI = null;
		imgView.setImageResource(R.drawable.no_image);
		
		imageButton_rotate.setVisibility(View.GONE);
	}
	
	public boolean setImage(Uri imgUri) {
		CUR_IMGURI = imgUri;
		CURRENT_IMAGE = new File(imgUri.getPath());

		try {
			imgView.setImageURI(CUR_IMGURI);
			HAS_IMAGE = true;
		} catch(Exception ex) {
			clearImage();
		}
		return HAS_IMAGE;
	}
	
	public boolean setImage(Bitmap bitmap) {
		imgView.setImageBitmap(bitmap);
		HAS_IMAGE = true;
		imageButton_rotate.setVisibility(View.VISIBLE);
		return HAS_IMAGE;
	}
	
	public Uri getImageURI() {
		return CUR_IMGURI;
	}
	
	public void setImageURI(Uri imageURI) {
		CUR_IMGURI = imageURI;
	}
	
	public File getCurrentImage() {
		return CURRENT_IMAGE;
	}
	
	public void setCurrentImage(File curImage) {
		CURRENT_IMAGE = curImage;
		if (CURRENT_IMAGE == null) {
			imageButton_rotate.setVisibility(View.GONE);
		} else {
			imageButton_rotate.setVisibility(View.VISIBLE);
		}
	}

	public boolean getHasImage() {
		return HAS_IMAGE;
	}
	
	public void setHasImage(boolean hasImage) {
		HAS_IMAGE = hasImage;
	}
	
	public boolean hasImageChanged() {
		return IMG_CHANGED;
	}
	
	/**
	 * Opens the camera to allow user to take a picture that will be associated with a Tag
	 */
	private void openCamera()
	{
		Intent i_Cam = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		//create a file to save an image in
		File f = imageHandler.makeImageFile();
		if (f != null)
		{
			TMP_IMGURI = Uri.fromFile(f);
			//if file was created, pass the URI to the camera app
			i_Cam.putExtra(MediaStore.EXTRA_OUTPUT, TMP_IMGURI);
		}
		//open camera to take pic when camera button is clicked				
        startActivityForResult(i_Cam, Constants.CAPTURE_IMG);
	}
	
	/**
	 * Opens the device's gallery to allow the user to select an image from the gallery that will be associated
	 * with a Tag
	 */
	private void openGallery()
	{
	   	Intent i = new Intent();
		i.setType("image/*");
		i.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(i, "Select Picture"), Constants.SELECT_IMG);
	}

	/*
	 * Async class used to rotate the image.  Need this to get the progress dialog up.
	 * TODO: This is in AddTagActivity.java as well.  Need to make one of these!
	 */
	private class RotateImage extends AsyncTask<Bitmap, Void, Bitmap> {		
		@Override
		protected Bitmap doInBackground(Bitmap... bitmaps) {
			Bitmap bitmap = bitmaps[0];
			
			Matrix matrix = new Matrix();
			matrix.postRotate(90);

			Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

			return rotatedBitmap;
		}
		
		protected void onPostExecute(Bitmap rotatedBitmap) {
// TODO: Need to add callback method to access Activity
//			if (comment != null) {
//				comment.setBitmap(rotatedBitmap);
//				updateContents();
//			} else {
				imgView.setImageBitmap(rotatedBitmap);
				
				//Save the bitmap to a temporary file
		        File tempFile=new File(Environment.getExternalStorageDirectory()+"/Geotagger/");
		        if(!tempFile.exists()){
		        	tempFile.mkdirs();
		        }       
		        OutputStream outStream = null;
		        File file = new File(Environment.getExternalStorageDirectory() + "/Geotagger/"+"tempImage"+".png");
			    try {
			        outStream = new FileOutputStream(file);
			        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
			        outStream.flush();
			        outStream.close();

					CUR_IMGURI = Uri.parse(file.getPath());
					CURRENT_IMAGE = new File(CUR_IMGURI.getPath());
			    } catch (Exception e) {
			    	e.printStackTrace();
					Toast.makeText(getActivity(), getString(R.string.toast_problem_savingimage), Toast.LENGTH_SHORT).show();
			    } 
//			}

			stopProgress();
		}
	}

}
