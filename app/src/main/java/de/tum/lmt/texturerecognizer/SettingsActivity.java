package de.tum.lmt.texturerecognizer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    
    private static boolean mAccelAvailable = false;
	private static boolean mGravAvailable = false;
	private static boolean mGyroAvailable = false;
	private static boolean mMagnetAvailable = false;
	private static boolean mRotVecAvailable = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        
        mAccelAvailable = intent.getBooleanExtra("accelAvailable", false);
        mGravAvailable = intent.getBooleanExtra("gravAvailable", false);
        mGyroAvailable = intent.getBooleanExtra("gyroAvailable", false);
        mMagnetAvailable = intent.getBooleanExtra("magnetAvailable", false);
        mRotVecAvailable = intent.getBooleanExtra("rotVecAvailable", false);
        
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    //this Fragment contains the Settings
    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    	private CheckBoxPreference mAccelPref;
        private CheckBoxPreference mGravPref;
        private CheckBoxPreference mGyroPref;
        private CheckBoxPreference mMagnetPref;
        private CheckBoxPreference mRotVecPref;
        private CheckBoxPreference mExternAccelPref;
        private CheckBoxPreference mVelocityPref;
        private CheckBoxPreference mModeSelectPref;
    	
        //always set the entries and the entry values when this fragment is being created (maybe the
        // user downloaded new languages and voices)
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            
            mAccelPref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_ACCEL_SELECT);
            mGravPref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_GRAV_SELECT);
            mGyroPref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_GYRO_SELECT);
            mMagnetPref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_MAGNET_SELECT);
            mRotVecPref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_ROTVEC_SELECT);
            mExternAccelPref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_EXTERN_ACCEL);
            mVelocityPref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_VELOCITY);
            mModeSelectPref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_MODE_SELECT);
            
            //Accel always checked
            mAccelPref.setEnabled(false);
            
            checkIfSensorAvailable();
            
            setSummaries();
        }
        
        private void checkIfSensorAvailable() {
        	if(!mAccelAvailable) {
                mAccelPref.setEnabled(false);
                mAccelPref.setSummary(getString(R.string.not_available));
            }
            
            if(!mGravAvailable) {
                mGravPref.setEnabled(false);
                mGravPref.setSummary(getString(R.string.not_available));
            }
            
            if(!mGyroAvailable) {
                mGyroPref.setEnabled(false);
                mGyroPref.setSummary(getString(R.string.not_available));
            }
            
            if(!mMagnetAvailable) {
                mMagnetPref.setEnabled(false);
                mMagnetPref.setSummary(getString(R.string.not_available));
            }
            
            if(!mRotVecAvailable) {
                mRotVecPref.setEnabled(false);
                mRotVecPref.setSummary(getString(R.string.not_available));
            }
        }
        
        private void setSummaries() {
        	
        	if(mAccelPref.isChecked()) {
                mAccelPref.setSummary(getString(R.string.active));
            } else {
            	mAccelPref.setSummary(getString(R.string.inactive));
            }
            
            if(mGravPref.isChecked()) {
            	mGravPref.setSummary(getString(R.string.active));
            } else {
            	mGravPref.setSummary(getString(R.string.inactive));
            }
            
            if(mGyroPref.isChecked()) {
            	mGyroPref.setSummary(getString(R.string.active));
            } else {
            	mGyroPref.setSummary(getString(R.string.inactive));
            }
            
            if(mMagnetPref.isChecked()) {
            	mMagnetPref.setSummary(getString(R.string.active));
            } else {
            	mMagnetPref.setSummary(getString(R.string.inactive));
            }
            
            if(mRotVecPref.isChecked()) {
            	mRotVecPref.setSummary(getString(R.string.active));
            } else {
                mRotVecPref.setSummary(getString(R.string.inactive));
            }

            if(mExternAccelPref.isChecked()) {
                mExternAccelPref.setSummary(getString(R.string.active));
            } else {
                mExternAccelPref.setSummary(getString(R.string.inactive));
            }

            if(mVelocityPref.isChecked()) {
                mVelocityPref.setSummary(getString(R.string.used));
            } else {
                mVelocityPref.setSummary(getString(R.string.not_used));
            }

            if(mModeSelectPref.isChecked()) {
                mModeSelectPref.setSummary(getString(R.string.used));
            } else {
                mModeSelectPref.setSummary(getString(R.string.not_used));
            }
        }
        
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            switch(key) {

            case Constants.PREF_KEY_ACCEL_SELECT:
            	
            	if(sharedPreferences.getBoolean(key, false)) {
            		mAccelPref.setSummary(getString(R.string.active));
            	} else {
                    mAccelPref.setSummary(getString(R.string.inactive));
            	}
            	
            	break;
            case Constants.PREF_KEY_GRAV_SELECT: 

            	if(sharedPreferences.getBoolean(key, false)) {
            		mGravPref.setSummary(getString(R.string.active));
            	} else {
                    mGravPref.setSummary(getString(R.string.inactive));
            	}
            	
            	break;
            case Constants.PREF_KEY_GYRO_SELECT:

            	if(sharedPreferences.getBoolean(key, false)) {
            		mGyroPref.setSummary(getString(R.string.active));
            	} else {
            		mGyroPref.setSummary(getString(R.string.inactive));
            	}
            	
            	break;
            case Constants.PREF_KEY_MAGNET_SELECT:

            	if(sharedPreferences.getBoolean(key, false)) {
            		mMagnetPref.setSummary(getString(R.string.active));
            	} else {
                    mMagnetPref.setSummary(getString(R.string.inactive));
            	}
            	
            	break;
            case Constants.PREF_KEY_ROTVEC_SELECT:

            	if(sharedPreferences.getBoolean(key, false)) {
            		mRotVecPref.setSummary(getString(R.string.active));
            	} else {
                    mRotVecPref.setSummary(getString(R.string.inactive));
            	}
            	
            	break;
            case Constants.PREF_KEY_EXTERN_ACCEL:

            	if(sharedPreferences.getBoolean(key, false)) {
            		mExternAccelPref.setSummary(getString(R.string.yes));
            	} else {
                    mExternAccelPref.setSummary(getString(R.string.no));
            	}
            	
            	break;
            case Constants.PREF_KEY_MODE_SELECT:

                CheckBoxPreference modeSelectPref = (CheckBoxPreference) findPreference(key);

                if (sharedPreferences.getBoolean(key, false)) {
                    modeSelectPref.setSummary(getString(R.string.database_on));
                } else {
                    modeSelectPref.setSummary(getString(R.string.database_off));
                }

                break;
                case Constants.PREF_KEY_VELOCITY:

                    if (sharedPreferences.getBoolean(key, false)) {
                        mVelocityPref.setSummary(getString(R.string.used));
                    } else {
                        mVelocityPref.setSummary(getString(R.string.not_used));
                    }

                    break;
            }
        }
    
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
