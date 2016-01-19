package de.tum.lmt.texturerecognizer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SensorLoggingActivity extends Activity {
	
	private static final String TAG = SensorLoggingActivity.class.getSimpleName();
	
	//Bluetooth
	private static final int REQUEST_ENABLE_BT = 0;
	
	private File mParentLoggingDir = MainActivity.getLoggingDir();
	private String mSensorLoggingPath;
	private File mSensorLoggingDir;
	private Map<Integer,String> mMap;

	//Sensors
	LoggingSensorListener mListener;
	SensorLog mAccelLog;
	SensorLog mAccelMinusOffsetLog;
	SensorLog mGravLog;
	SensorLog mGyroLog;
	SensorLog mMagnetLog;
	SensorLog mRotVecLog;
	String mExternData;
	SensorLog mExternAccelLog;
	
	//Audio
	private AudioRecorderWAV mRecorder;

	//UI Elements
	private CheckBox mCheckboxSensorsLogging;
	private CheckBox mCheckboxAudioLogging;
	private ImageButton mButtonStartLogging;
	private ImageButton mButtonStopLogging;
	private ImageButton mButtonOkLogging;
	private ImageButton mButtonCancelLogging;
	private TextView mDescription1;
	private TextView mDescription2;
	private TextView mDescription3;
	
	private boolean mIsLogging = false;
	private boolean mLoggingFinished = false;

	//Sensors
	private String[] sensorNames;
	private int numberOfSensorsToLog = 0;

	private File mActivityStreamFile;
	private BufferedOutputStream mOutActivity;

	//Bluetooth
	Bluetooth mBT;

	//Timers
	private Handler mTimerHandler;
	private Handler mTimedUpdateHandler;
	private Runnable mTimedUpdateRunnable;

	private Vibrator mVibrator;
	
	private String mGapFiller;

	// init data
	class Sensor {
		int nameID;
		String name;
		Sensor(int nameID_, String name_) {
			nameID = nameID_;
			name = name_;
		}
	}

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logging);

		mSensorLoggingPath = mParentLoggingDir.getAbsolutePath() + File.separator + Constants.SENSOR_LOGGING_FOLDER_NAME;

		mBT = new Bluetooth(getApplicationContext());

		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mRecorder = new AudioRecorderWAV(getBaseContext());

		mSensorLoggingDir = new File(mSensorLoggingPath);

		mTimerHandler = new Handler();

		mCheckboxSensorsLogging = (CheckBox) findViewById(R.id.checkbox_sensors_logging);
		mCheckboxAudioLogging = (CheckBox) findViewById(R.id.checkbox_audio_logging);

		mDescription1 = (TextView) findViewById(R.id.textview_instructions_logging_1);
		mDescription2 = (TextView) findViewById(R.id.textview_instructions_logging_2);
		mDescription3 = (TextView) findViewById(R.id.textview_instructions_logging_3);

		mButtonStartLogging = (ImageButton) findViewById(R.id.button_start_logging);
		mButtonStartLogging.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				mDescription2.setText(getString(R.string.wait_vibration));

				mTimerHandler.postDelayed( new Runnable() {
					@Override
					public void run() {
							startLogging();
					}
				}, Constants.DURATION_WAIT);
			}
		});

		mButtonStopLogging = (ImageButton) findViewById(R.id.button_stop_logging);
		mButtonStopLogging.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				stopLogging();
				
			}
		});

		mButtonOkLogging = (ImageButton) findViewById(R.id.button_features_logging);
		mButtonOkLogging.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				showFeaturesDialog();
			}
		});

		mButtonCancelLogging = (ImageButton) findViewById(R.id.button_cancel_logging);
		mButtonCancelLogging.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if (mLoggingFinished) {
					mGapFiller = getString(R.string.gapFiller_after) + " " + getString(R.string.logging);
					showCancelDialog(mGapFiller);
				} else {
					mGapFiller = getString(R.string.gapFiller_before) + " " + getString(R.string.logging);
					showCancelDialog(mGapFiller);
				}
			}
		});
		
		mButtonOkLogging.setEnabled(false);
		mButtonOkLogging.setClickable(false);

	}

	protected void showFeaturesDialog() {
		DialogFragment featuresDialog = new DialogFeaturesFragment(getBaseContext(), mAccelMinusOffsetLog, mGravLog, mGyroLog, mMagnetLog, mRotVecLog);
		featuresDialog.show(getFragmentManager(), "DialogFeaturesFragment");
	}

	protected void showCancelDialog(String gapFiller) {
		DialogFragment cancelDialog = new DialogCancelFragment(getBaseContext(), gapFiller, mLoggingFinished);
		cancelDialog.show(getFragmentManager(), "DialogCancelFragment");
	}

	@Override 
	protected void onPause(){
		super.onPause();
		if (mIsLogging) {
			stopLogging();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_ENABLE_BT) {
			if(resultCode == Activity.RESULT_OK) {
				Toast toast = Toast.makeText(getApplicationContext(), R.string.toast_enabled_bt, Toast.LENGTH_SHORT);
				toast.show();
			} 
			else {
				Toast toast = Toast.makeText(getApplicationContext(), R.string.toast_error_bt, Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	}

	boolean checkIfInsideStringArray(String s, String[] sArray) {
		for (String it : sArray) {
			if (it.contains(s)) {
				return true;
			}
		}
		return false;
	}

	private void stopLoggingNoSensors(String message) {
		mLoggingFinished = false;

		mDescription2.setText("");
		mDescription3.setText("Something went wrong: " + message);
		Toast.makeText(getApplicationContext(), "Sensor is missing " + message + ". Please check again.", Toast.LENGTH_LONG).show();
		mButtonOkLogging.setEnabled(true);
		mButtonOkLogging.setClickable(true);
		mButtonStopLogging.setEnabled(false);
	}

	private void startLogging() {

		if(!mSensorLoggingDir.exists()) {
			mSensorLoggingDir.mkdirs();
		}

		mButtonOkLogging.setEnabled(false);
		mButtonOkLogging.setClickable(false);

		if(!mIsLogging && !mLoggingFinished && (mCheckboxSensorsLogging.isChecked() || mCheckboxAudioLogging.isChecked())) {
			Log.i(TAG, "logging sensors on native level to folder: " + mSensorLoggingDir.getAbsolutePath());

			mBT.findBluetooth();
			try {
				mBT.openBT();
			} catch (IOException e) {
				e.printStackTrace();
			}

			mVibrator.vibrate(500);

			mButtonStartLogging.setEnabled(false);
			mButtonStartLogging.setClickable(false);
			mButtonStopLogging.setEnabled(true);
			mButtonStopLogging.setClickable(true);

			if(mCheckboxAudioLogging.isChecked()) {
				mRecorder.startRecording();
			}

			if(mCheckboxSensorsLogging.isChecked()) {
				
				SensorManager manager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
				
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				boolean useAccel = sharedPrefs.getBoolean(Constants.PREF_KEY_ACCEL_SELECT, false);
				boolean useGrav = sharedPrefs.getBoolean(Constants.PREF_KEY_GRAV_SELECT, false);
				boolean useGyro = sharedPrefs.getBoolean(Constants.PREF_KEY_GYRO_SELECT, false);
				boolean useMagnet = sharedPrefs.getBoolean(Constants.PREF_KEY_MAGNET_SELECT, false);
				boolean useRotVec = sharedPrefs.getBoolean(Constants.PREF_KEY_ROTVEC_SELECT, false);
				boolean useExternAccel = sharedPrefs.getBoolean(Constants.PREF_KEY_EXTERN_ACCEL, false);
								
				mListener = new LoggingSensorListener(this, manager, useAccel, useGrav, useGyro, useMagnet, useRotVec, useExternAccel);
				mListener.registerListener();

				mBT.beginListenForData();

				mIsLogging = true;
			}

			mTimerHandler.postDelayed( new Runnable() {
				@Override
				public void run() {
					stopLogging();
				}
			}, Constants.DURATION_TO_LOG);
		}
		else if(!mCheckboxAudioLogging.isChecked() && !mCheckboxSensorsLogging.isChecked()) {
			mDescription2.setText(getString(R.string.no_box_checked));
		}
	}

	private void stopLogging() {

		if(mIsLogging) {

			mDescription2.setText("Logging finished, parsing the necessary data...");

			//mTimedUpdateHandler.removeCallbacks(mTimedUpdateRunnable);

			mVibrator.vibrate(500);

			if(mCheckboxAudioLogging.isChecked()) {
				mRecorder.stopRecording();
			}

			if(mCheckboxSensorsLogging.isChecked()) {
				mListener.unregisterListener();
				try {
					mBT.closeBT();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			mIsLogging = false;
			mLoggingFinished = true;

			getLoggingData();
			
			subtractOffsetFromAccelData();
			
			writeLoggingDataToFile();
			
			//dropDataPoints();

			mButtonOkLogging.setEnabled(true);
			mButtonOkLogging.setClickable(true);
			mButtonStopLogging.setEnabled(false);
			mButtonStopLogging.setClickable(false);

			mDescription2.setText("");
			mDescription3.setText(getString(R.string.textview_instructions_logging_3));
		}
	}
	
	private void getLoggingData() {
		
		if(mListener.getAccelLog() != null) {
			mAccelLog = mListener.getAccelLog();
		}
		
		if(mListener.getGravLog() != null) {
			mGravLog = mListener.getGravLog();
		}
		
		if(mListener.getGyroLog() != null) {
			mGyroLog = mListener.getGyroLog();
		}
		
		if(mListener.getMagnetLog() != null) {
			mMagnetLog = mListener.getMagnetLog();
		}
		
		if(mListener.getRotVecLog() != null) {
			mRotVecLog = mListener.getRotVecLog();
		}

		if(mBT.getData() != null) {

			Log.i(TAG, "Received Data in SensorLoggingActivity not equal to null");

			mExternData = mBT.getData();
		}
	}
	
	private void subtractOffsetFromAccelData() {
		
		if((mAccelLog != null) && !(mAccelLog.getType() == -1)) {
			
			mAccelMinusOffsetLog = new SensorLog(mAccelLog.getType());
			
			for(long timestamp : mAccelLog.getTimestamps()) {
				mAccelMinusOffsetLog.addTimestamp(timestamp);
			}
			
			float[] valuesMinusOffset = new float[3];
			
			for(float[] values : mAccelLog.getValues()) {
				valuesMinusOffset[0] = values[0] - (float) SensorCalibrationActivity.offsetValues[0];
				valuesMinusOffset[1] = values[1] - (float) SensorCalibrationActivity.offsetValues[1];
				valuesMinusOffset[2] = values[2] - (float) SensorCalibrationActivity.offsetValues[2];
				
				mAccelMinusOffsetLog.addValues(valuesMinusOffset);
			}
		}
	}
	
	private void writeLoggingDataToFile() {
		
		if((mAccelMinusOffsetLog != null) && !(mAccelMinusOffsetLog.getType() == -1)) {
			
			String accelLogString = buildLogString(mAccelMinusOffsetLog);
			
			String accelFileString = mSensorLoggingDir.getAbsolutePath() + File.separator + "accel.txt";
			File accelFile = new File(accelFileString);
			
			if(!accelFile.exists()) {
				try {
					accelFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			writeStringToFile(accelFile, accelLogString);
		}
		
		if((mGravLog != null) && !(mGravLog.getType() == -1)) {
			
			String gravLogString = buildLogString(mGravLog);
			
			String gravFileString = mSensorLoggingDir.getAbsolutePath() + File.separator + "grav.txt";
			File gravFile = new File(gravFileString);
			
			if(!gravFile.exists()) {
				try {
					gravFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			writeStringToFile(gravFile, gravLogString);
		}
		
		if((mGyroLog != null) && !(mGyroLog.getType() == -1)) {
			
			String gyroLogString = buildLogString(mGyroLog);
			
			String gyroFileString = mSensorLoggingDir.getAbsolutePath() + File.separator + "gyro.txt";
			File gyroFile = new File(gyroFileString);
			
			if(!gyroFile.exists()) {
				try {
					gyroFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			writeStringToFile(gyroFile, gyroLogString);
		}
		
		if((mMagnetLog != null) && !(mMagnetLog.getType() == -1)) {
			
			String magnetLogString = buildLogString(mMagnetLog);
			
			String magnetFileString = mSensorLoggingDir.getAbsolutePath() + File.separator + "magnet.txt";
			File magnetFile = new File(magnetFileString);
			
			if(!magnetFile.exists()) {
				try {
					magnetFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			writeStringToFile(magnetFile, magnetLogString);
		}
		
		if((mRotVecLog != null) && !(mRotVecLog.getType() == -1)) {
			
			String rotVecLogString = buildLogString(mRotVecLog);
			
			String rotVecFileString = mSensorLoggingDir.getAbsolutePath() + File.separator + "rotvec.txt";
			File rotVecFile = new File(rotVecFileString);
			
			if(!rotVecFile.exists()) {
				try {
					rotVecFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			writeStringToFile(rotVecFile, rotVecLogString);
		}

		if(mExternData != null) {

			String externAccelFileString = mSensorLoggingDir.getAbsolutePath() + File.separator + "externaccel.txt";
			File externAccelFile = new File(externAccelFileString);

			if(!externAccelFile.exists()) {
				try {
					externAccelFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			writeStringToFile(externAccelFile, mExternData);
		}
	}
	
	private String buildLogString(SensorLog log) {
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < log.getTimestamps().size(); i++) {
			
			sb.append(Long.toString(log.getTimestamps().get(i)));
			sb.append("\t");
			sb.append(Float.toString(log.getValues().get(i)[0]));
			sb.append("\t");
			sb.append(Float.toString(log.getValues().get(i)[1]));
			sb.append("\t");
			sb.append(Float.toString(log.getValues().get(i)[2]));
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	private void writeStringToFile(File file, String logString) {
		
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
		try {
			myOutWriter.append(logString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			myOutWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}