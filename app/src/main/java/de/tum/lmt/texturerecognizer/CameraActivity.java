package de.tum.lmt.texturerecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CameraActivity extends Activity{
	private static final String TAG = CameraActivity.class.getSimpleName();
	
	//UI Elements
	private TextView mInstructions2;
	private ImageButton mButtonCamera;
	private ImageButton mButtonOkCamera;
	private ImageButton mButtonCancelCamera;
	
	//Files and Paths
	private File mLoggingDir = MainActivity.getLoggingDir();
	private String mDataToSendPath = MainActivity.getLoggingDir().getAbsolutePath() + Constants.DATA_TO_SEND_FOLDER_NAME;
	private File mDataToSendDir;
	
	//Camera, Preview and Pictures
	private Camera mCamera;
	private int mDefaultCameraId;
	private boolean mDataReady = false;
	private PictureCallback mPicture;
	private int mNumberOfPicturesTaken = 0;
	private boolean mPictureTaken = false;
	private CameraPreview mPreview;
	private int mHeight;
	private int mWidth;
	private int mFrameWidth;
	private int mFrameHeight;
	
	private Handler mTimerHandler;
	
	private String mGapFiller;
	
	private Vibrator mVibrator;

	/**
	 * taken from http://stackoverflow.com/questions/3170691/how-to-get-current-memory-usage-in-android
	 * @return
	 */
	long getUsedMemory() {
		final Runtime runtime = Runtime.getRuntime();
		final long usedMemInMB=(runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
		final long maxHeapSizeInMB=runtime.maxMemory() / 1048576L;
		Log.i(TAG, "Used in MB " + Long.toString(usedMemInMB) + ", total  " + Long.toString(maxHeapSizeInMB));
		return maxHeapSizeInMB;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		getUsedMemory();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		
		mInstructions2 = (TextView) findViewById(R.id.textview_instructions_camera_2);

		initializePictureCallback();


		mCamera = getCameraInstance();

		if(mCamera == null) {
			mInstructions2.setText(R.string.message_open_failed);
		}

		setupCameraSimple();

		mDataToSendDir = new File(mDataToSendPath);

		if(!mDataToSendDir.exists()) {
			mDataToSendDir.mkdirs();
		}

		mButtonCamera = (ImageButton) findViewById(R.id.button_camera);
		mButtonCamera.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mCamera != null) {
					takePicture();
				}
			}
		});

		mButtonOkCamera = (ImageButton) findViewById(R.id.button_ok_camera);
		mButtonOkCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				boolean mode = sharedPrefs.getBoolean(Constants.PREF_KEY_MODE_SELECT, false);
				
				if(!mode) {
					showContinueDialog();
				}
				else {
					Intent intentLogging = new Intent(CameraActivity.this, SensorLoggingActivity.class);
					startActivity(intentLogging);
					finish();
				}
			}
		});

		mButtonCancelCamera = (ImageButton) findViewById(R.id.button_cancel_camera);
		mButtonCancelCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mNumberOfPicturesTaken == 0) {
					mGapFiller = getString(R.string.gapFiller_before) + " " + getString(R.string.taking) + " " + 
							getString(R.string.gapFiller_camera_1) + " " + getString(R.string.picture); 
				}
				else if(mNumberOfPicturesTaken == 1) {
					mGapFiller = getString(R.string.gapFiller_after) + " " + getString(R.string.taking) + " " + 
							getString(R.string.gapFiller_camera_1) + " " + getString(R.string.picture); 
				}
				else if (mNumberOfPicturesTaken == 2) {
					mGapFiller = getString(R.string.gapFiller_after) + " " + getString(R.string.taking) + " " + 
							getString(R.string.gapFiller_camera_2) + " " + getString(R.string.picture); 
				}
				showCancelDialog(mGapFiller);
			}
		});
		mButtonOkCamera.setEnabled(false);
		mButtonOkCamera.setClickable(false);	
	}
	
	
	private void takePicture() {
		mCamera.takePicture(null, null, mPicture);
	}
	
	private void initializePictureCallback() {
		Log.i(TAG, "Calling initializePictureCallback");
		mPicture = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

				mButtonCamera.setClickable(false);
				mButtonCamera.setEnabled(false);
				
				//save the picture 
				File pictureFile = getOutputMediaFile();
				if (pictureFile == null){
					Log.d(TAG, "Error creating media file, check storage permissions "); //e.getMessage
					return;
				}

				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
				} catch (FileNotFoundException e) {
					Log.d(TAG, "File not found: " + e.getMessage());
				} catch (IOException e) {
					Log.d(TAG, "Error accessing file: " + e.getMessage());
				}

				mPictureTaken = true;
				mNumberOfPicturesTaken++;

				//also save the picture without Flash in the Data To Send Folder
				if(mNumberOfPicturesTaken == 1) {
					/*File pictureFileToSend = getOutputMediaFileToSend();
						if (pictureFile == null){
							Log.d(TAG, "Error creating media file, check storage permissions "); //e.getMessage
							return;
						}

						try {
							FileOutputStream fos = new FileOutputStream(pictureFileToSend);
							fos.write(data);
							fos.close();
						} catch (FileNotFoundException e) {
							Log.d(TAG, "File not found: " + e.getMessage());
						} catch (IOException e) {
							Log.d(TAG, "Error accessing file: " + e.getMessage());
						}*/
				}

				if(mNumberOfPicturesTaken == 1) {
					mInstructions2.setText(getString(R.string.message_picture_taken_1));
					activateFlash();
				}
				else if(mNumberOfPicturesTaken == 2) {
					mInstructions2.setText(getString(R.string.message_picture_taken_2));
				}

				mCamera.startPreview();

				if(mNumberOfPicturesTaken == 1) {
					
					mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

					mVibrator.vibrate(200);
					
					mTimerHandler = new Handler();
					
					mTimerHandler.postDelayed( new Runnable() {
						@Override
						public void run() {
							takePicture();;
						}
					}, Constants.DURATION_BREAK_PICTURE);
					
				} else if(mNumberOfPicturesTaken == 2) {
					
					mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

					mVibrator.vibrate(200);

					mButtonOkCamera.setEnabled(true);
					mButtonOkCamera.setClickable(true);
					mButtonCamera.setEnabled(false);					
					mButtonCamera.setClickable(false);
					
					rotatePicture(mLoggingDir.getAbsolutePath() + "/" + Constants.CAMERA_NO_FLASH_FILENAME + Constants.JPG_FILE_EXTENSION);
					rotatePicture(mLoggingDir.getAbsolutePath() + "/" + Constants.CAMERA_FLASH_FILENAME + Constants.JPG_FILE_EXTENSION);
					//						rotatePicture(dataToSendDir + "/" + pictureDisplayFilename + fileExtension);

					releaseCamera();
					if(mPreview != null)
						mPreview = null;
				}
			}
		};
	}

	public void rotatePicture(String Path) {
		getUsedMemory();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeFile(Path, options);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		final Matrix matrix = new Matrix(); 
		matrix.setRotate(90);
		final Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),matrix,false);

		rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPG_COMPRESSION_LEVEL, bytes);

		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(Path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fos.write(bytes.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(!mDataReady) {
			mCamera = getCameraInstance();

			if(mCamera == null) {
				mInstructions2.setText(R.string.message_open_failed);
			}

			setupCameraSimple();//setupCamera(); 
		}
	}

	protected void showCancelDialog(String gapFiller) {
		DialogFragment cancelDialog = new DialogCancelFragment(getBaseContext(), gapFiller, mPictureTaken);
		cancelDialog.show(getFragmentManager(), "DialogCancelFragment");
	}

	protected void showContinueDialog() {
		DialogFragment continueDialog = new DialogContinueFragment("camera");
		continueDialog.show(getFragmentManager(), "DialogContinueFragment");
	}

	@SuppressWarnings("deprecation")
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		}
		catch (Exception e){

		}
		return c;
	}

	private android.hardware.Camera.Size getTheRightSize(Camera.Parameters params) {
		List<android.hardware.Camera.Size> sizesPicture = params.getSupportedPictureSizes();


		int minNumberPixels = 999999999;
		android.hardware.Camera.Size bestSize = null;
		for(Camera.Size size : sizesPicture) {
			if (size.width>Constants.DESIRED_CAMERA_IMAGE_WIDTH && size.height>Constants.DESIRED_IMAGE_CAMERA_HEIGHT) {
				int currentNumPixels = size.width * size.height;
				if (currentNumPixels<minNumberPixels) {
					minNumberPixels = currentNumPixels;
					bestSize = size;
				}
			}
		}
		if (bestSize == null) { // just get the best size
			int maxNumberPixels = 0;
			for(Camera.Size size : sizesPicture) {
				Log.i(TAG, "Sizes " + size.width + "x" + size.height);
				int currentNumPixels = size.width * size.height;
				if (currentNumPixels>=maxNumberPixels) {
					bestSize = size;
					maxNumberPixels = currentNumPixels;
				}
			}
		}
		//int max_size = 4096;//openglrender throws an exception, in case we load a bitmap with size bigger than 4096
		
		return bestSize;
	}

	protected void setupCameraSimple() {
		if(mCamera != null){
			Camera.Parameters params = mCamera.getParameters();
			Log.d(TAG, "getSupportedPreviewSizes()");
			//List<android.hardware.Camera.Size> sizes = params.getSupportedPreviewSizes();
			List<android.hardware.Camera.Size> sizes = params.getSupportedPictureSizes();

			if (sizes != null) {			

				android.hardware.Camera.Size bestSize = getTheRightSize(params);

				params.setPictureSize(bestSize.width, bestSize.height);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					params.setRecordingHint(true);

				if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
					params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				}

				try {
					mCamera.setParameters(params);
				}
				catch (Exception e) {
					Log.e(TAG, "Catching exception in camera parameters");
				}

				params = mCamera.getParameters();

				mFrameWidth = params.getPreviewSize().width;
				mFrameHeight = params.getPreviewSize().height;

				Log.i(TAG, "frame " + mFrameWidth + "x" + mFrameHeight);
			}

			int result = getSurfaceOrientation();
			Log.i(TAG, "camera orientation " + result);
			if( mCamera != null)
				mCamera.setDisplayOrientation(result);

			mPreview = new CameraPreview(this, mCamera);
			FrameLayout preview = (FrameLayout) findViewById(R.id.framelayout_camera);
			preview.addView(mPreview);

			mDataReady = true;

		}
	}

	public void activateFlash() {
		if(mCamera != null) {
			Camera.Parameters params;
			params = mCamera.getParameters();

			if(params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH)) {
				params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			}
			mCamera.setParameters(params);
		}
	}

	@Override 
	protected void onPause() {

		super.onPause();
		releaseCamera();
		if(mPreview != null)
			mPreview = null;
		
		// release background resource
		getUsedMemory();
	}

	@Override 
	protected void onStop() {
		super.onStop();
		releaseCamera();
		if(mPreview != null)
			mPreview = null;
	}

	private void releaseCamera() {
		if(mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	private File getOutputMediaFile() {
		File mediaFile = null;
		if(mNumberOfPicturesTaken == 0) {
			mediaFile = new File(mLoggingDir.getPath() + File.separator + Constants.CAMERA_NO_FLASH_FILENAME + Constants.JPG_FILE_EXTENSION);
		}
		else if(mNumberOfPicturesTaken == 1) {
			mediaFile = new File(mLoggingDir.getPath() + File.separator + Constants.CAMERA_FLASH_FILENAME + Constants.JPG_FILE_EXTENSION);
		}

		return mediaFile;
	}

	@SuppressWarnings("deprecation")
	public int getSurfaceOrientation() {
		// Adapt rotation to current orientation
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo( mDefaultCameraId, info);
		int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0: degrees = 0; break;
		case Surface.ROTATION_90: degrees = 90; break;
		case Surface.ROTATION_180: degrees = 180; break;
		case Surface.ROTATION_270: degrees = 270; break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}

		return result;
	}
}
