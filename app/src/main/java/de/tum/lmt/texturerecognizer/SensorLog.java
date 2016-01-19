package de.tum.lmt.texturerecognizer;

import java.util.ArrayList;
import java.util.List;

import android.hardware.Sensor;

public class SensorLog {
	
	private int mType = -1;
	private List<Long> mTimestamps;
	private List<float[]> mValues;
	
	public SensorLog(int type) {
		mType = type;
		
		mTimestamps = new ArrayList<Long>();
		mValues = new ArrayList<float[]>();
	}
	
	public int getType() {
		return mType;
	}
	
	public void addTimestamp(long timestamp) {
		mTimestamps.add(timestamp);
	}
	
	public List<Long> getTimestamps() {
		return mTimestamps;
	}
	
	public void addValues(float[] values) {

		mValues.add(new float[]{values[0], values[1], values[2]});
	}
	
	public List<float[]> getValues() {
		return mValues;
	}
}
