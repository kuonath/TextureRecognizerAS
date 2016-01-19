package de.tum.lmt.texturerecognizer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

public class LoggingSensorListener implements SensorEventListener {
	
	private SensorManager mSensorManager = null;
	private Sensor mAccel = null;
	private Sensor mGrav = null;
	private Sensor mGyro = null;
	private Sensor mMagnet = null;
	private Sensor mRotVec = null;
	
	private SensorLog mAccelLog;
	private SensorLog mGravLog;
	private SensorLog mGyroLog;
	private SensorLog mMagnetLog;
	private SensorLog mRotVecLog;
	
	public LoggingSensorListener(Context context, SensorManager manager, boolean useAccel, boolean useGrav, boolean useGyro, boolean useMagnet, boolean useRotVec, boolean useExternAccel) {
		
		mSensorManager = manager;
		
		if(useAccel && !useExternAccel) {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            } else {
                Toast.makeText(context, context.getString(R.string.sensor_missing), Toast.LENGTH_LONG).show();
            }
        }
		
		if(useGrav) {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
                mGrav = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            } else {
                Toast.makeText(context, context.getString(R.string.sensor_missing), Toast.LENGTH_LONG).show();
            }
        }
		
		if(useGyro) {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            } else {
                Toast.makeText(context, context.getString(R.string.sensor_missing), Toast.LENGTH_LONG).show();
            }
        }
		
		if(useMagnet) {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                mMagnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            } else {
                Toast.makeText(context, context.getString(R.string.sensor_missing), Toast.LENGTH_LONG).show();
            }
        }
		
		if(useRotVec) {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
                mRotVec = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            } else {
                Toast.makeText(context, context.getString(R.string.sensor_missing), Toast.LENGTH_LONG).show();
            }
        }
	}
	
	public void registerListener() {
		
		Log.i("Logging", "register Sensor Listener");
		
		if(mAccel != null) {
			mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_FASTEST);
			mAccelLog = new SensorLog(Sensor.TYPE_ACCELEROMETER);
		}
		if(mGrav != null) {
			mSensorManager.registerListener(this, mGrav, SensorManager.SENSOR_DELAY_NORMAL);
			mGravLog = new SensorLog(Sensor.TYPE_GRAVITY);
		}
		if(mGyro != null) {
			mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
			mGyroLog = new SensorLog(Sensor.TYPE_GYROSCOPE);
		}
		if(mMagnet != null) {
			mSensorManager.registerListener(this, mMagnet, SensorManager.SENSOR_DELAY_NORMAL);
			mMagnetLog = new SensorLog(Sensor.TYPE_MAGNETIC_FIELD);
		}
		if(mRotVec != null) {
			mSensorManager.registerListener(this, mRotVec, SensorManager.SENSOR_DELAY_NORMAL);
			mRotVecLog = new SensorLog(Sensor.TYPE_ROTATION_VECTOR);
		}
	}
	
	public void unregisterListener() {
		
		Log.i("Logging", "unregister Sensor Listener");

		if(mAccel != null) {
			mSensorManager.unregisterListener(this, mAccel);
		}
		if(mGrav != null) {
			mSensorManager.unregisterListener(this, mGrav);
		}
		if(mGyro != null) {
			mSensorManager.unregisterListener(this, mGyro);
		}
		if(mMagnet != null) {
			mSensorManager.unregisterListener(this, mMagnet);
		}
		if(mRotVec != null) {
			mSensorManager.unregisterListener(this, mRotVec);
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		switch(event.sensor.getType()) {
		
		case Sensor.TYPE_ACCELEROMETER:
			
			mAccelLog.addTimestamp(event.timestamp);
			mAccelLog.addValues(event.values);
						
			break;
		case Sensor.TYPE_GRAVITY:
						
			mGravLog.addTimestamp(event.timestamp);
			mGravLog.addValues(event.values);
			
			break;
		case Sensor.TYPE_GYROSCOPE:
			
			mGyroLog.addTimestamp(event.timestamp);
			mGyroLog.addValues(event.values);
			
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			
			mMagnetLog.addTimestamp(event.timestamp);
			mMagnetLog.addValues(event.values);
			
			break;
		case Sensor.TYPE_ROTATION_VECTOR:
			
			mRotVecLog.addTimestamp(event.timestamp);
			mRotVecLog.addValues(event.values);
			
			break;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public SensorLog getAccelLog() {
		return mAccelLog;
	}
	
	public SensorLog getGravLog() {
		return mGravLog;
	}
	
	public SensorLog getGyroLog() {
		return mGyroLog;
	}
	
	public SensorLog getMagnetLog() {
		return mMagnetLog;
	}
	
	public SensorLog getRotVecLog() {
		return mRotVecLog;
	}
}
