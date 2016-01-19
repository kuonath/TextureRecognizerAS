package de.tum.lmt.texturerecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

public class FeatureComputer {
	
	private String mFeaturePath;
	
	private String mSurfacePictureNoFlashPath;
	private String mSurfacePictureFlashPath;
	
	private boolean mDatabaseMode;

	private float mImpactBegins; //in seconds, can be converted to sound or sensor index with known sampling rate
	private float mImpactEnds;
	private float mMovementBegins;
	private float mMovementEnds;
	
	private double mWeightMacro;
	private double mWeightMicro;
	private double mWeightGlossiness;
	
	private double mMacroAmplitude;
	private double mMicroAmplitude;
	private double mGlossinessAmplitude;
	
	private double mHardness;
	private long mImpactDuration;
	
	private double mNoiseDistribution;
	
	/*private SensorLog mAccelLog;
	private SensorLog mGravLog;
	private SensorLog mGyroLog;
	private SensorLog mMagnetLog;
	private SensorLog mRotVecLog;*/
	
	private List<Double> mSoundData;
	private List<Double> mSoundDataMovement;
	private List<Double> mSoundDataImpact;
	private List<float[]> mAccelData;
	private List<Float> mFilteredAccelData;
	private List<Float> mAccelDataMovement;
	private List<Float> mAccelDataImpact;
	
	private Calculator mCalculator;
	
	public interface iOnFeaturesFinishedListener {
		public void onFeaturesComputed(long duration);
	}
	
	public FeatureComputer(String featurePath, String surfacePictureNoFlashPath, String surfacePictureFlashPath, List<Double> soundData, SensorLog accelLog, boolean databaseMode) {
		
		mCalculator = new Calculator();
		
		mFeaturePath = featurePath;
		mSurfacePictureNoFlashPath = surfacePictureNoFlashPath;
		mSurfacePictureFlashPath = surfacePictureFlashPath;
		mSoundData = soundData;
		mAccelData = accelLog.getValues();
		mDatabaseMode = databaseMode;

		mFilteredAccelData = new ArrayList<Float>();
		mSoundDataMovement = new ArrayList<Double>();
		mSoundDataImpact = new ArrayList<Double>();
		mAccelDataMovement = new ArrayList<Float>();
		mAccelDataImpact = new ArrayList<Float>();
	}
	
	public void computeFeatures() {
		
		initWeights();
		
		getIntervals(); // get Indices to split sensor and sound data into "no contact", "impact" and "movement"
						   // splitting happens in the next two methods; so far only one data segment after 1s is used (movement)
		
		prepareSoundData(); //extract movement data (currently fixed to 1s to end) 
		
		prepareSensorData(); //extract movement data (currently fixed to 1s to end); use only one axis or use absolute acceleration (currently only y-axis)
		
		computeHardnessAndImpactDuration();
		
		doOperationsOnBitmaps();
		
		computeMacroAmplitude();
		computeMicroAmplitude();
		computeFinenessAmplitude();
		
		computeNoiseDistribution();
		
		calculateWeightMacroRoughness();
		calculateWeightMicroRoughness();
		calculateWeightGlossiness();
		
		if(!mDatabaseMode) {
			renormalizeWeights();
		}
		
		printAllFeatures();
		
		//onFeaturesComputed(mImpactDuration);
	}
	
	private void initWeights() {
		
		Log.i("Features", "initWeights()");
		
		mWeightMacro = 0.4;
		mWeightMicro = 0.4;
		mWeightGlossiness = 0.2;
	}
	
	private void getIntervals() {
		
		Log.i("Features", "getIntervals()");
		
		mImpactBegins = 0.5f;
		mImpactEnds = 1.0f;
		mMovementBegins = 1.0f;
		//mMovementEnds = // currently movement ends at the end of the recording (last index of lists)
	}
	
	private void prepareSoundData() {
				
		Log.i("Features", "prepareSoundData()");
		
		mSoundDataImpact = mSoundData;
		mSoundDataMovement = mSoundData;
		
		int beginImpactIndex = (int)(mImpactBegins * Constants.RECORDER_SAMPLING_RATE);
		int endImpactIndex = (int)(mImpactEnds * Constants.RECORDER_SAMPLING_RATE);
		
		for(int i = (mSoundDataImpact.size() - 1); i > endImpactIndex; i--) {
			mSoundDataImpact.remove(i);
		}
		
		if(beginImpactIndex < mSoundDataImpact.size()) {
			for(int i = (beginImpactIndex - 1); i >= 0; i--) {
				mSoundDataImpact.remove(i);
			}
		}
		
		int beginMovementIndex = (int)(mMovementBegins * Constants.RECORDER_SAMPLING_RATE);
		
		if(beginMovementIndex < mSoundDataMovement.size()) {
			for(int i = (beginMovementIndex - 1); i >= 0; i--) {
				mSoundDataMovement.remove(i); // remove elements before start of movement
			}
		}
	}
	
	private void prepareSensorData() {
		
		Log.i("Features", "prepareSensorData()");

		eliminateHandMovement();

		for(int i = 0; i < mAccelData.size(); i++) {
			mAccelDataImpact.add(mAccelData.get(i)[1]); // use only y-axis
		}
		
		for(int i = 0; i < mAccelData.size(); i++) {
			mAccelDataMovement.add(mAccelData.get(i)[1]); // use only y-axis
		}
		
		int beginImpactIndex = (int)(mImpactBegins * Constants.SAMPLE_RATE_ACCEL);
		int endImpactIndex = (int)(mImpactEnds * Constants.SAMPLE_RATE_ACCEL);
		
		for(int i = (mAccelDataImpact.size() - 1); i > endImpactIndex; i--) {
			mAccelDataImpact.remove(i);
		}
		
		if(beginImpactIndex < mAccelDataImpact.size()) {
			for(int i = (beginImpactIndex - 1); i >= 0; i--) {
				mAccelDataImpact.remove(i);
			}
		}
		
		int beginMovementIndex = (int)(mMovementBegins * Constants.SAMPLE_RATE_ACCEL);
		
		if(beginMovementIndex < mAccelDataMovement.size()) {
			for(int i = (beginMovementIndex - 1); i >= 0; i--) {
				mAccelDataMovement.remove(i); // remove elements before start of movement
			}
		}
	}

	private void eliminateHandMovement() {

		int length = 20;
		int shift = 10;

		List<Float> movingAverage = new ArrayList<Float>();

		for(int i = (length-1); i < mAccelData.size(); i++) {

			float sum = 0;

			for(int j = 0; j < length; j++) {

				sum += mAccelData.get(i-j)[1];
			}

			movingAverage.add((i-length+1), sum/length);
		}

		for(int k = 0; k < (mAccelData.size() - length); k++) {

			float newValue = mAccelData.get(k+shift)[1] - movingAverage.get(k);

			mFilteredAccelData.add(k, newValue);
		}

		saveFilteredAccelForTesting();

		Log.i("hand", "original length: " + mAccelData.size() + ", new size: " + mFilteredAccelData.size());
	}
	
	private void computeHardnessAndImpactDuration() {
		mHardness = 1;
		mImpactDuration = 500;
	}
	
	private double doOperationsOnBitmaps() {

		Bitmap bitmapNoFlash = loadBitmap(mSurfacePictureNoFlashPath);

		saveBitmapToFile(mFeaturePath, "display.jpg", bitmapNoFlash);

		Bitmap bitmapNoFlashGrayscale = toGrayscale(bitmapNoFlash);

		bitmapNoFlash.recycle();

		int heightNoFlash = bitmapNoFlashGrayscale.getHeight();
		int widthNoFlash = bitmapNoFlashGrayscale.getWidth();

		double numberOfBrightPixelsNoFlash = calculateNumberOfBrightPixels(bitmapNoFlashGrayscale, heightNoFlash, widthNoFlash);

		//number of bright pixels relative to total number of pixels
		double brightPixelsToTotalNumberOfPixelsNoFlash = numberOfBrightPixelsNoFlash / (heightNoFlash*widthNoFlash);
		
		saveBitmapToFile(mFeaturePath, "macro.jpg", bitmapNoFlashGrayscale);

		bitmapNoFlashGrayscale.recycle();

		//with flash
		Bitmap bitmapFlash = loadBitmap(mSurfacePictureFlashPath);

		Bitmap bitmapFlashGrayscale = toGrayscale(bitmapFlash);

		bitmapFlash.recycle();

		int heightFlash = bitmapFlashGrayscale.getHeight();
		int widthFlash = bitmapFlashGrayscale.getWidth();

		double numberOfBrightPixelsFlash = calculateNumberOfBrightPixels(bitmapFlashGrayscale, heightFlash, widthFlash);

		//number of bright pixels relative to total number of pixels
		double brightPixelsToTotalNumberOfPixelsFlash = numberOfBrightPixelsFlash / (heightFlash*widthFlash);

		double brightPixelsFlashToNoFlash = brightPixelsToTotalNumberOfPixelsFlash / brightPixelsToTotalNumberOfPixelsNoFlash;

		Log.i("Pixels", "number no flash: " + numberOfBrightPixelsNoFlash + ", rel no flash: " + brightPixelsToTotalNumberOfPixelsNoFlash);
		Log.i("Pixels", "number flash: " + numberOfBrightPixelsFlash + ", rel no flash: " + brightPixelsToTotalNumberOfPixelsFlash);
		Log.i("Pixels", "rel flash to no flash: " + brightPixelsFlashToNoFlash);

		return brightPixelsFlashToNoFlash;
	}

	private Bitmap loadBitmap(String pathToBitmap) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeFile(pathToBitmap, options);
	}
	
	//from leparlon on http://stackoverflow.com/questions/3373860/convert-a-bitmap-to-grayscale-in-android
	private Bitmap toGrayscale(Bitmap bmpOriginal) {  		
	    int width, height;
	    height = bmpOriginal.getHeight();
	    width = bmpOriginal.getWidth();   

	    Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    Canvas c = new Canvas(bmpGrayscale);
	    Paint paint = new Paint();
	    ColorMatrix cm = new ColorMatrix();
	    cm.setSaturation(0);
	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
	    paint.setColorFilter(f);
	    c.drawBitmap(bmpOriginal, 0, 0, paint);
	    return bmpGrayscale;
	}
	
	private void saveBitmapToFile(String pathToFile, String filename, Bitmap picture) {
		
		File pictureFile = new File(pathToFile + filename);
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		
		picture.compress(Bitmap.CompressFormat.JPEG, Constants.JPG_COMPRESSION_LEVEL, bytes);

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

	private double calculateNumberOfBrightPixels(Bitmap bitmap, int height, int width) {

		int pixel;
		int numberOfBrightPixels = 0;

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {

				pixel = (bitmap.getPixel(j, i));

				if(Color.red(pixel) > 250) {  //greyscale -> R=G=B, any color can be used
					numberOfBrightPixels = numberOfBrightPixels + 1;
				}
			}
		}

		return numberOfBrightPixels;
	}
	
	private void computeMacroAmplitude() {
		mMacroAmplitude = 1;
	}
	
	private void computeMicroAmplitude() {
		mMicroAmplitude = 1;
	}
	
	private void computeFinenessAmplitude() {
		mGlossinessAmplitude = 1;
	}
	
	private void computeNoiseDistribution() {
				
		ListIterator<Double> itBegin = mSoundDataMovement.listIterator(0);
		ListIterator<Double> itEnd = mSoundDataMovement.listIterator(mSoundDataMovement.size()-1);
		
		double maxAbs = mCalculator.getAbsoluteMaximumDouble(itBegin, itEnd);
		
		Log.i("Features", "Max Sound: " + maxAbs);
		
		double distThresh = 0.1 * maxAbs;
		
		List<Integer> binarySignal = new ArrayList<Integer>();
		
		int numberOfOnes = 0;
		int numberOfZeros = 0;
		
		itBegin = mSoundDataMovement.listIterator(0);
		
		for(ListIterator<Double> lit = itBegin; (lit.hasNext() && lit != itEnd); ) {
			
			double nextValue = lit.next();
			
			if(nextValue > distThresh) {
				binarySignal.add(1);
				numberOfOnes++;
			} else {
				binarySignal.add(0);
				numberOfZeros++;
			}
		}
		
		mNoiseDistribution = (double)numberOfOnes / (double)numberOfZeros;
		
		if(!mDatabaseMode && (mNoiseDistribution > 1.0)) {
			mNoiseDistribution = 1.0;
		}
		
		Log.i("Features", "noise distribution: " + mNoiseDistribution);

		
		mNoiseDistribution = 0.5;
	}
	
	private void calculateWeightMacroRoughness() {
		
		ListIterator<Float> itBegin = mAccelDataMovement.listIterator(0);
		ListIterator<Float> itEnd = mAccelDataMovement.listIterator(mAccelDataMovement.size()-1);
		
		double variance = mCalculator.varianceFloat(itBegin, itEnd);
		
		mWeightMacro = 3.0 * variance;
		
		if(!mDatabaseMode && mWeightMacro > 1.0) {
			mWeightMacro = 1.0;
		}
		
		Log.i("Features", "mWeightMacro: " + mWeightMacro);
		
	}
	
	private void calculateWeightMicroRoughness() {
		
		ListIterator<Double> itBegin = mSoundDataMovement.listIterator(0);
		ListIterator<Double> itEnd = mSoundDataMovement.listIterator(mSoundDataMovement.size()-1);
		
		double bandpower = mCalculator.bandPowerDouble(itBegin, itEnd);
		
		mWeightMicro = 5 * bandpower;
		if (!mDatabaseMode && mWeightMicro > 1.0) {
			mWeightMicro = 1.0;
		}
		
		Log.i("Features", "mWeightMicro: " + mWeightMicro);
		
	}
	
	private void calculateWeightGlossiness() {
		mWeightGlossiness = doOperationsOnBitmaps();
	}
	
	private void renormalizeWeights() {
		
		double sumOfWeights = mWeightMacro + mWeightMicro + mWeightGlossiness;
		
		if(Math.abs(sumOfWeights - 1) > 0.001) {
			mWeightMacro /= sumOfWeights;
			mWeightMicro /= sumOfWeights;
			mWeightGlossiness /= sumOfWeights;
		}
				
		Log.i("Features", "final weights: " + mWeightMacro + ", " + mWeightMicro + ", " + mWeightGlossiness);
	}
	
	private void printAllFeatures() {
		int precisionDigitParameter = 2;
		int precisionWeights = 3;

		//only round when not in database mode
		if(!mDatabaseMode) {
			mMacroAmplitude = mCalculator.roundDigits(mMacroAmplitude, precisionDigitParameter);
			mMicroAmplitude = mCalculator.roundDigits(mMicroAmplitude, precisionDigitParameter);
			mGlossinessAmplitude = mCalculator.roundDigits(mGlossinessAmplitude, precisionDigitParameter);
			mNoiseDistribution = mCalculator.roundDigits(mNoiseDistribution, precisionDigitParameter);
			mWeightMacro = mCalculator.roundDigits(mWeightMacro, precisionWeights);
			mWeightMicro = mCalculator.roundDigits(mWeightMicro, precisionWeights);
			mWeightGlossiness = mCalculator.roundDigits(mWeightGlossiness, precisionWeights);
			mHardness = mCalculator.roundDigits(mHardness, precisionDigitParameter);
			//impact duration does not have to be rounded
		}
		
		if(!mDatabaseMode) {
			String delimiter = "#";
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(Double.toString(mMacroAmplitude));
			sb.append(delimiter);
			sb.append(Double.toString(mMicroAmplitude));
			sb.append(delimiter);
			sb.append(Double.toString(mGlossinessAmplitude));
			sb.append(delimiter);
			sb.append(Double.toString(mNoiseDistribution));
			sb.append(delimiter);
			sb.append(Double.toString(mWeightMacro));
			sb.append(delimiter);
			sb.append(Double.toString(mWeightMicro));
			sb.append(delimiter);
			sb.append(Double.toString(mWeightGlossiness));
			sb.append(delimiter);
			sb.append(Double.toString(mHardness));
			sb.append(delimiter);
			sb.append(Long.toString(mImpactDuration));
			
			String featureString = sb.toString();
			
			writeStringToFile(mFeaturePath, featureString);
		}
	}
	
	private void writeStringToFile(String pathToFile, String stringToWrite) {

		File featureFile = new File(pathToFile + "parameters.txt");
		
		if(!featureFile.exists()) {
			try {
				featureFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(featureFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
		try {
			myOutWriter.append(stringToWrite);
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

	private void saveFilteredAccelForTesting() {

		String filteredAccelLogString = buildLogString(mFilteredAccelData);

		String filteredAccelFileString = mFeaturePath + "accel.txt";
		File filteredAccelFile = new File(filteredAccelFileString);

		if(!filteredAccelFile.exists()) {
			try {
				filteredAccelFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		writeTestStringToFile(filteredAccelFile, filteredAccelLogString);
	}

	private String buildLogString(List<Float> values) {

		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < values.size(); i++) {
			sb.append(values.get(i));
			sb.append('\n');
		}

		return sb.toString();
	}

	private void writeTestStringToFile(File file, String logString) {

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
