package de.tum.lmt.texturerecognizer;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 *  this activity is simply used to perform calibration of accelerometer, while the phone is in stationary position, later the calculated offset values are taken into account when reading off the measurement values
 * @author ga56den
 *
 */
public class SensorCalibrationActivity extends Activity implements SensorEventListener{

	private static final String TAG = SensorCalibrationActivity.class.getSimpleName();
	
	//UI Elements
	private ImageButton buttonOkCalib;
	private ImageButton buttonCancelCalib;
	TextView mDescription;
	TextView mDescription_2;
	TextView mOffSetX;
	TextView mOffSetY;
	TextView mOffSetZ;
	
	private SensorManager mSensorManager;
	
	private boolean mCloseToFloor = false;
	private boolean mLyingOnTheFloor = false;
	private boolean mFinalValues = false;
	private boolean mProximitySensorAvailable = false;
	
	public double mOffsetX = 0.0;
	public double mOffsetY = 0.0; 
	public double mOffsetZ = 0.0;
	public static double offsetValues[] = {0.0, 0.0, 0.0};
	private String mFinalOffsetX;
	private String mFinalOffsetY;
	private String mFinalOffsetZ;
	
	private long mTimestampLastGyro; // 500 ms
	private int mAccelerometerMinDelay;
	private int mGyroMinDelay = -1;
	
	private String mGapFiller;
	
	private Vibrator mVibrator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);
		
		mDescription=((TextView) findViewById(R.id.textview_instructions_calib_1));
		mDescription_2=((TextView) findViewById(R.id.textview_instructions_calib_2));
		mOffSetX=((TextView) findViewById(R.id.textview_offsetX));
		mOffSetY=((TextView) findViewById(R.id.textview_offsetY));
		mOffSetZ =((TextView) findViewById(R.id.textview_offsetZ));		

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		buttonOkCalib =  (ImageButton) findViewById(R.id.button_ok_calib);

		buttonOkCalib.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onPause();
				
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				boolean mode = sharedPrefs.getBoolean(Constants.PREF_KEY_MODE_SELECT, false);
				
				if(!mode) {
					//not in database mode
					showContinueDialog();
				} 
				else {
					Intent intentCam = new Intent(SensorCalibrationActivity.this, CameraActivity.class);
					startActivity(intentCam);
					finish();
				}
			}
		});

		buttonCancelCalib = (ImageButton) findViewById(R.id.button_cancel_calib);

		buttonCancelCalib.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onPause();

				if(mFinalValues) {
					mGapFiller = getString(R.string.gapFiller_after) + " " + getString(R.string.calibration);
					showCancelDialog(mGapFiller);
				}
				else {
					mGapFiller = getString(R.string.gapFiller_before) + " " + getString(R.string.calibration);
					showCancelDialog(mGapFiller);
				}
				//SensorCalibrationActivity.this.finish();*/
			}
		});


		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		mTimestampLastGyro = System.currentTimeMillis();

		// show used memory
       	{
	       	final Runtime runtime = Runtime.getRuntime();
			final long usedMemInMB=(runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
			final long maxHeapSizeInMB=runtime.maxMemory() / 1048576L;
			Log.i(TAG, "SCActivity, Used in MB " + Long.toString(usedMemInMB) + ", total  " + Long.toString(maxHeapSizeInMB));
       	}
	}





	protected void showContinueDialog() {

		DialogFragment continueDialog = new DialogContinueFragment("calibration");
		continueDialog.show(getFragmentManager(), "DialogContinueFragment");
	}

	protected void showCancelDialog(String gapFiller) {

		DialogFragment cancelDialog = new DialogCancelFragment(getBaseContext(), gapFiller, mFinalValues);
		cancelDialog.show(getFragmentManager(), "DialogCancelFragment");
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// register this class as a listener for the gyro, proximity and accelerometer sensors
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
			mProximitySensorAvailable = true;
			mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_FASTEST);
			Log.i(TAG, "Registered proximity sensor");
		}
		else if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
			mProximitySensorAvailable = false;
			mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_FASTEST);
			Log.i(TAG, "Registered light sensor");
		}
		else {
			Log.i(TAG, "Light nor proximity sensors are present here!");
		}

		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
		if (!Constants.GRAVITY_NOT_PRESENT) {
			mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
			mGyroMinDelay = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).getMinDelay();
		}
		mAccelerometerMinDelay = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getMinDelay();
		
		if (Constants.GRAVITY_NOT_PRESENT) {
			mDescription.setText("No gravity found, please continue to next screen for data collection");
			mOffSetX.setText("0.0 (default)");
			mOffSetY.setText("0.0 (default)");
			mOffSetZ.setText("0.0 (default)");
		}
		else {
			mDescription.setText(getString(R.string.textview_instructions_calib_1, mAccelerometerMinDelay/1000, mGyroMinDelay/1000));
		}
		Log.i(TAG, "onResume");
	}

	@Override
	public void onPause() {
		// unregister listener
		super.onPause();
		mSensorManager.unregisterListener(this);


	}


	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && mCloseToFloor && !mFinalValues) {
			mLyingOnTheFloor = true;
			getAccelerometer(event);
		}
		if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			Log.i(TAG, "proximity value " + event.values[0]);

			if (event.values[0] < Constants.CALIB_PROXIMITY_THRESHOLD) {
				mCloseToFloor = true;
				mTimestampLastGyro = System.currentTimeMillis();
				if(mVibrator != null)
				{
					mVibrator.vibrate(200);
				}
			}
			else {
				mCloseToFloor = false;
			}
		}
		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			Log.i(TAG, "light value " + event.values[0]);
			if (event.values[0] < 8.0)
				mCloseToFloor = true;
			else
				mCloseToFloor = false;
		}
		
		if (!Constants.GRAVITY_NOT_PRESENT) {
		
			if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				// as soon as we get the gyro event, we assume that we are not stationary anymore, which means calibration finished
				if (event.values[0] > Constants.CALIB_THRESH || event.values[1] > Constants.CALIB_THRESH || event.values[2] > 
				Constants.CALIB_THRESH && (event.timestamp - mTimestampLastGyro/1e6) > Constants.CALIB_TIME_DIFF_SINCE_LAST_GYRO_EVENT) {
					
					if (mLyingOnTheFloor) {
						mDescription.setText("");
						mDescription_2.setText(R.string.textview_instructions_calib_2);
						buttonOkCalib.setEnabled(true);
						offsetValues[0] = -mOffsetX;
						offsetValues[1] = mOffsetY;
						offsetValues[2] = mOffsetZ;
						mFinalValues = true;
						mLyingOnTheFloor = false;
						if(mVibrator != null)
						{
							mVibrator.vibrate(500);
						}
					}
				}
			}
		}
		else {
			if (event.sensor.getType() == Sensor.TYPE_PROXIMITY && mLyingOnTheFloor) {
				mDescription.setText("");
				mDescription_2.setText(R.string.textview_instructions_calib_2);
				buttonOkCalib.setEnabled(true);
				offsetValues[0] = -mOffsetX;
				offsetValues[1] = mOffsetY;
				offsetValues[2] = mOffsetZ;
				mFinalValues = true;
				mLyingOnTheFloor = false;
				if(mVibrator != null)
				{
					mVibrator.vibrate(500);
				}
			}
		}
	}

	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;
		// Movement	    
		mOffsetX = (1 - Constants.CALIB_FILTER_FORGETTING_FACTOR) * mOffsetX + Constants.CALIB_FILTER_FORGETTING_FACTOR * values[0];
		mOffsetY = (1 - Constants.CALIB_FILTER_FORGETTING_FACTOR) * mOffsetY + Constants.CALIB_FILTER_FORGETTING_FACTOR * values[1];
		if (values[2]<0.0)
			mOffsetZ = (1 - Constants.CALIB_FILTER_FORGETTING_FACTOR) * mOffsetZ + Constants.CALIB_FILTER_FORGETTING_FACTOR * (SensorManager.GRAVITY_EARTH + values[2]);
		else
			mOffsetZ = (1 - Constants.CALIB_FILTER_FORGETTING_FACTOR) * mOffsetZ + Constants.CALIB_FILTER_FORGETTING_FACTOR * (-SensorManager.GRAVITY_EARTH + values[2]);
		Log.i(TAG, "getting: " + values[2] + ", summing: " + mOffsetZ);
		if (!mFinalValues) {
			mFinalOffsetX = getString(R.string.textview_offsetX) + mOffsetX + " m/s^2";
			mFinalOffsetY = getString(R.string.textview_offsetY) + mOffsetY + " m/s^2";
			mFinalOffsetZ = getString(R.string.textview_offsetZ) + mOffsetZ + " m/s^2";
			mOffSetX.setText(mFinalOffsetX);
			mOffSetY.setText(mFinalOffsetY);		
			mOffSetZ.setText(mFinalOffsetZ);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		mSensorManager.unregisterListener(this);
		finish();
	}
}
