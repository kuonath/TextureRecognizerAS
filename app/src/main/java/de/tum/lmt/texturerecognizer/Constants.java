package de.tum.lmt.texturerecognizer;

import java.util.Arrays;
import java.util.List;

public class Constants {

	//Preferences
	public static final String PREF_KEY_ACCEL_SELECT = "pref_key_accel_select";
	public static final String PREF_KEY_GRAV_SELECT = "pref_key_grav_select";
	public static final String PREF_KEY_GYRO_SELECT = "pref_key_gyro_select";
	public static final String PREF_KEY_MAGNET_SELECT = "pref_key_magnet_select";
	public static final String PREF_KEY_ROTVEC_SELECT = "pref_key_rotvec_select";
	public static final String PREF_KEY_EXTERN_ACCEL = "pref_key_extern_accel";
	public static final String PREF_KEY_MODE_SELECT = "pref_key_mode_select";
	public static final String PREF_KEY_VELOCITY = "pref_key_velocity";
	
	//Files and Paths
	public static final String PATH_TO_STORAGE = "Log/";
	public static final String ANALYSIS_FOLDER_NAME = "/Analysis/";
	public static final String DATABASE_FOLDER_NAME = "/Database";
	public static final String DATA_TO_SEND_FOLDER_NAME = "/DataToSend";
	public final static String SENSOR_LOGGING_FOLDER_NAME = "Sensors";
	
	//Calibration Activity
	public static boolean GRAVITY_NOT_PRESENT;
	public static final float CALIB_PROXIMITY_THRESHOLD = 4.5f;
	public static final float CALIB_FILTER_FORGETTING_FACTOR = 1.0f/100.0f;
	public static final float CALIB_THRESH = 0.015f;
	public static final long CALIB_TIME_DIFF_SINCE_LAST_GYRO_EVENT = 500;
	
	//Camera Activity
	public static final int DURATION_BREAK_PICTURE = 1500;
	public static final int DESIRED_CAMERA_IMAGE_WIDTH = 720;
	public static final int DESIRED_IMAGE_CAMERA_HEIGHT = 1280;
	public static final String CAMERA_NO_FLASH_FILENAME = "picture";
	public static final String CAMERA_FLASH_FILENAME = "picture_flash";
	public static final String CAMERA_DISPLAY_FILENAME = "display";
	public static final String JPG_FILE_EXTENSION = ".jpg";
	public static final int JPG_COMPRESSION_LEVEL = 40; // the bigger the better
	
	//SensorLoggingActivity
	public final static long DURATION_WAIT = 1000;
	public final static long DURATION_INTERESTED = 3000; // ms, 5s is fine
	public final static int PRECISION_TO_WRITE_SENSOR_DATA = 4;	// 4 digits after comma when writing sensor data to file 
	public final static long DURATION_TO_LOG = DURATION_INTERESTED + 1000; // ms, because not instant
	
	//AUDIO
	public static final String AUDIO_FILENAME = "audio";
	public static final String AUDIO_FILENAME1 = "sound";
	public static final String AUDIO_FILENAME2 = "impact";
	public static final String FEATURE_FILE_NAME = "accel";
	public static final int RECORDER_SAMPLING_RATE = 44100;
	public static Byte[] SOUND_ARRAY;
	
	//Continue Dialog
	public static final String CALIBRATION = "calibration";
	public static final String CAMERA = "camera";
	
	//Features Dialog
	public static final String BITMAP_NO_FLASH_FILENAME = "/" + CAMERA_NO_FLASH_FILENAME + JPG_FILE_EXTENSION;
	public static final String BITMAP_FLASH_FILENAME = "/" + CAMERA_FLASH_FILENAME + JPG_FILE_EXTENSION;
	public static final String TXT_SUFFIX_NAME = ".txt";
	public static final String PARAMETERS_FILENAME = "parameters" + TXT_SUFFIX_NAME;
	public static final String IMPACT_FILENAME = "/impact.wav";
	
	//Feature Extractor
	public static final int SAMPLE_RATE_ACCEL = 100;
	
	//Send Dialog
	public static final List<String> NAMES_OF_BLUETOOTH_FILES_TO_SEND = Arrays.asList(new String[] { "macro", "parameters", "display", "sound", "impact" });
}
