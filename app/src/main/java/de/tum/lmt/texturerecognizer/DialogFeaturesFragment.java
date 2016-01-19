package de.tum.lmt.texturerecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DialogFeaturesFragment extends DialogFragment implements FeatureComputer.iOnFeaturesFinishedListener {

	private static final String TAG = DialogFeaturesFragment.class.getSimpleName();

	private Context mContext;
	private boolean mDatabaseMode;
	
	ProgressBar mProgressBar;
	TextView mTextView;
	Button mButtonSend;
	
	SensorLog mAccelLog;
	SensorLog mAccelMinusOffsetLog;
	SensorLog mGravLog;
	SensorLog mGyroLog;
	SensorLog mMagnetLog;
	SensorLog mRotVecLog;

	private InputStream mSoundStream;
	private WaveReader mWaveReader;

	private File mLoggingDir = MainActivity.getLoggingDir();	
	private String mDataToSendPath;
	private File mDataToSendDir;
	private String mAudioFilename;
	private String mBitmapNoFlashPath = MainActivity.getLoggingDir() + Constants.BITMAP_NO_FLASH_FILENAME;
	private String mBitmapFlashPath = MainActivity.getLoggingDir() + Constants.BITMAP_FLASH_FILENAME;
	private AudioRecorderWAV recorder;
	
	private FeatureComputer mFeatureComputer;
	
	private long mDuration;
	
	public DialogFeaturesFragment(Context context, SensorLog accelLog, SensorLog gravLog, SensorLog gyroLog, SensorLog magnetLog, SensorLog rotVecLog) {
		mContext = context;
		
		mAccelLog = accelLog;
		mGravLog = gravLog;
		mGyroLog = gyroLog;
		mMagnetLog = magnetLog;
		mRotVecLog = rotVecLog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		recorder = new AudioRecorderWAV(mContext);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mDatabaseMode = sharedPrefs.getBoolean(Constants.PREF_KEY_MODE_SELECT, false);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.dialog_features, null);

		mProgressBar = (ProgressBar) content.findViewById(R.id.progressbar_features);
		mTextView = (TextView) content.findViewById(R.id.textview_features);

		mButtonSend = (Button) content.findViewById(R.id.button_send_features);
		
		if(mDatabaseMode) {
			mDataToSendPath = mLoggingDir.getAbsolutePath();
			mAudioFilename = mLoggingDir.getAbsolutePath() + File.separator + Constants.AUDIO_FILENAME + ".wav";
		}
		else {
			mDataToSendPath = MainActivity.getLoggingDir().getAbsolutePath() + Constants.DATA_TO_SEND_FOLDER_NAME;
			mAudioFilename = mDataToSendPath + Constants.IMPACT_FILENAME;
			mDataToSendDir = new File(mDataToSendPath);
			
			if(!mDataToSendDir.exists()) {
				mDataToSendDir.mkdirs();
			}
		}

		builder.setView(content);

		return builder.create();
	}

	byte[] toPrimitives(Byte[] oBytes)
	{

		byte[] bytes = new byte[oBytes.length];
		for(int i = 0; i < oBytes.length; i++){
			bytes[i] = oBytes[i];
		}
		return bytes;

	}

	@Override
	public void onStart() {
		super.onStart();
		Runnable r = new Runnable() {

			public void run() {

				//loadBitmaps();

				//following method should be used for cropping pictures to desired aspect ratio, if this has not already been done
				//check Dimas CameraActivity first and test with the TPad
				//so far the method only saves the display picture in the DataToSend Directory
				//prepareBitmaps();
				
				String featurePath = MainActivity.getLoggingDir() + Constants.DATA_TO_SEND_FOLDER_NAME + File.separator;
				
				List<Double> soundData = getSoundData();
				
				mFeatureComputer = new FeatureComputer(featurePath, mBitmapNoFlashPath, mBitmapFlashPath, soundData, mAccelLog, mDatabaseMode);
				
				mFeatureComputer.computeFeatures();
				
				extractImpact(mDuration);

				if(!mDatabaseMode) {
					showSendDialog();
					dismiss();
				}
				else {
					getActivity().finish();
					dismiss();
				}
				//					Log.i(TAG, "Recycling bitmaps");
				//					mBitmapNoFlash.recycle();
				//					mBitmapFlash.recycle();
			}
		};
		
		Thread FeatureThread = new Thread(r);	
		FeatureThread.start();
	}
	
	//from user2813523 on http://stackoverflow.com/questions/15533854/converting-byte-array-to-double-array
	public static byte[] toByteArray(double[] doubleArray){
	    int times = Double.SIZE / Byte.SIZE;
	    byte[] bytes = new byte[doubleArray.length * times];
	    for(int i=0;i<doubleArray.length;i++){
	        ByteBuffer.wrap(bytes, i*times, times).putDouble(doubleArray[i]);
	    }
	    return bytes;
	}
	
	//should be called after Feature Extraction
	private void extractImpact(long duration) {
		
		/*mSoundStream = getSoundStream();

		if(mSoundStream != null) {

			try {
				mWaveReader = new WaveReader(mSoundStream);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			Log.e(TAG, "Could not read .wav file");
		}*/
		
		//byte[] soundImpactArray = toByteArray(mWaveReader.getData());

		byte[] soundImpactArray = toPrimitives(Constants.SOUND_ARRAY);
		
		int beginImpactIndex = 2000;
		Log.i(TAG,"Impact at index: " + beginImpactIndex);
		
		final int fixedDuration = (int) Math.round(Constants.RECORDER_SAMPLING_RATE * 2.0); // = 1000ms

		Log.i(TAG, "Impact found, start at " + beginImpactIndex + " and until " + (beginImpactIndex+fixedDuration));
		byte[] impactData = new byte[fixedDuration]; 
		for(int i=0;i<fixedDuration;i++)
		{
			impactData[i] = soundImpactArray[beginImpactIndex + i];
		}
			
		String path = mDataToSendPath + Constants.IMPACT_FILENAME;
			
		recorder.writeWaveFile(impactData, path, 1, impactData.length);
		
	}

	private void showSendDialog() {
		DialogFragment sendDialog = new DialogSendFragment();
		FragmentManager frag = getFragmentManager();
		if (frag != null) {
			sendDialog.show(frag, "DialogSendFragment");
		}
	}
	
	/*private void prepareBitmaps() {
		
		String featurePath = MainActivity.getLoggingDir() + Constants.DATA_TO_SEND_FOLDER_NAME + File.separator;
		
		saveDisplayPictureToFile(featurePath, mBitmapNoFlash);
	}*/
	
	private void saveDisplayPictureToFile(String pathToFile, Bitmap displayPicture) {
		
		File pictureFile = new File(pathToFile + "display.jpg");
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		
		displayPicture.compress(Bitmap.CompressFormat.JPEG, Constants.JPG_COMPRESSION_LEVEL, bytes);

		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(pictureFile);
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

	private InputStream getSoundStream() {
		File soundData = new File(mAudioFilename);
		InputStream soundStream = null;

		try {
			soundStream = new FileInputStream(soundData);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return soundStream;
	}
	
	private List<Double> getSoundData() {
		
		mSoundStream = getSoundStream();

		if(mSoundStream != null) {

			try {
				mWaveReader = new WaveReader(mSoundStream);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			Log.e(TAG, "Could not read .wav file");
		}
		
		double[] soundDataArray =  mWaveReader.getData();
		
		List<Double> soundDataList = new ArrayList<Double>();
		
		for(double sound : soundDataArray) {
			soundDataList.add(sound);
		}
		
		return soundDataList;
	}

	@Override
	public void onFeaturesComputed(long duration) {
		
		mDuration = duration;
		
	}

}
