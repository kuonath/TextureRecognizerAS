package de.tum.lmt.texturerecognizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class DialogSensorFragment extends DialogFragment {
	
	private static final String TAG = DialogSensorFragment.class.getSimpleName();
	
	private Context mContext;
	
	private SensorManager mSensorManager;
	
	private CheckBox mAccelBox;
	private CheckBox mGravBox;
	private CheckBox mGyroBox;
	private CheckBox mMagnetBox;
	private CheckBox mRotVecBox;
	private CheckBox mExternAccelBox;
    private CheckBox mVelocityBox;
	
	private boolean mAccelAvailable = false;
	private boolean mGravAvailable = false;
	private boolean mGyroAvailable = false;
	private boolean mMagnetAvailable = false;
	private boolean mRotVecAvailable = false;
	
	private boolean mUseAccel = false;
	private boolean mUseGrav = false;
	private boolean mUseGyro = false;
	private boolean mUseMagnet = false;
	private boolean mUseRotVec = false;
	private boolean mUseExternAccel = false;
    private boolean mUseVelocity = false;
	
	public interface iOnDialogButtonClickListener {
        public void onFinishSensorDialog(String buttonClicked);
    }
	
	public DialogSensorFragment(Context context, boolean accelAvailable, boolean gravAvailable, boolean gyroAvailable, boolean magnetAvailable, boolean rotVecAvailable) {
		mContext = context;
		mAccelAvailable = accelAvailable;
		mGravAvailable = gravAvailable;
		mGyroAvailable = gyroAvailable;
		mMagnetAvailable = magnetAvailable;
		mRotVecAvailable = rotVecAvailable;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_sensor, null);

        mAccelBox = (CheckBox) content.findViewById(R.id.accel_box);
        mGravBox = (CheckBox) content.findViewById(R.id.grav_box);
        mGyroBox = (CheckBox) content.findViewById(R.id.gyro_box);
        mMagnetBox = (CheckBox) content.findViewById(R.id.magnet_box);
        mRotVecBox = (CheckBox) content.findViewById(R.id.rotvec_box);
        mExternAccelBox = (CheckBox) content.findViewById(R.id.extern_accel_box);
        mVelocityBox = (CheckBox) content.findViewById(R.id.velocity_box);

        // read shared preferences and check CheckBoxes accordingly so that the CheckBoxes show the settings
        // of the last use of the app when the app gets opened for the next time
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //mUseAccel = sharedPrefs.getBoolean(Constants.PREF_KEY_ACCEL_SELECT, false);
        mUseGrav = sharedPrefs.getBoolean(Constants.PREF_KEY_GRAV_SELECT, false);
        mUseGyro = sharedPrefs.getBoolean(Constants.PREF_KEY_GYRO_SELECT, false);
        mUseMagnet = sharedPrefs.getBoolean(Constants.PREF_KEY_MAGNET_SELECT, false);
        mUseRotVec = sharedPrefs.getBoolean(Constants.PREF_KEY_ROTVEC_SELECT, false);
        mUseExternAccel = sharedPrefs.getBoolean(Constants.PREF_KEY_EXTERN_ACCEL, false);
        mUseVelocity = sharedPrefs.getBoolean(Constants.PREF_KEY_VELOCITY, false);
        
        //Accel immer verwenden
        if(mAccelAvailable) {
        	mUseAccel = true;
        	mAccelBox.setClickable(false);
        	mAccelBox.setEnabled(false);
        }
        
        mAccelBox.setChecked(mUseAccel);
        mGravBox.setChecked(mUseGrav);
        mGyroBox.setChecked(mUseGyro);
        mMagnetBox.setChecked(mUseMagnet);
        mRotVecBox.setChecked(mUseRotVec);
        mExternAccelBox.setChecked(mUseExternAccel);
        mVelocityBox.setChecked(mUseVelocity);
        
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);
        
        if(!mAccelAvailable) {
        	mAccelBox.setEnabled(false);
        	mAccelBox.setClickable(false);
        }
        if(!mGravAvailable) {
        	mGravBox.setEnabled(false);
        	mGravBox.setClickable(false);
        }
        if(!mGyroAvailable) {
        	mGyroBox.setEnabled(false);
        	mGyroBox.setClickable(false);
        }
        if(!mMagnetAvailable) {
        	mMagnetBox.setEnabled(false);
        	mMagnetBox.setClickable(false);
        }
        if(!mRotVecAvailable) {
        	mRotVecBox.setEnabled(false);
        	mRotVecBox.setClickable(false);
        }
        
        //onClickListener for CheckBoxes to check/uncheck them and activate/deactivate their features
        mAccelBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mAccelBox.isChecked();
                if (checked) {
                    Log.i(TAG, "checked");
                    //can't uncheck Accel
                    //mAccelBox.setChecked(false);
                    //mUseAccel = false;
                    Toast.makeText(mContext, getString(R.string.accel_click), Toast.LENGTH_LONG);
                } else {
                    Log.i(TAG, "not checked");
                    mAccelBox.setChecked(true);
                    mUseAccel = true;
                }
            }
        });
        
        mGravBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mGravBox.isChecked();
                if (checked) {
                    Log.i(TAG, "checked");
                    mGravBox.setChecked(false);
                    mUseGrav = false;
                } else {
                    Log.i(TAG, "not checked");
                    mGravBox.setChecked(true);
                    mUseGrav = true;
                }
            }
        });
        
        mGyroBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mGyroBox.isChecked();
                if (checked) {
                    Log.i(TAG, "checked");
                    mGyroBox.setChecked(false);
                    mUseGyro = false;
                } else {
                    Log.i(TAG, "not checked");
                    mGyroBox.setChecked(true);
                    mUseGyro = true;
                }
            }
        });
        
        mMagnetBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mMagnetBox.isChecked();
                if (checked) {
                    Log.i(TAG, "checked");
                    mMagnetBox.setChecked(false);
                    mUseMagnet = false;
                } else {
                    Log.i(TAG, "not checked");
                    mMagnetBox.setChecked(true);
                    mUseMagnet = true;
                }
            }
        });
        
        mRotVecBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mRotVecBox.isChecked();
                if (checked) {
                    Log.i(TAG, "checked");
                    mRotVecBox.setChecked(false);
                    mUseRotVec = false;
                } else {
                    Log.i(TAG, "not checked");
                    mRotVecBox.setChecked(true);
                    mUseRotVec = true;
                }
            }
        });
        
        mExternAccelBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mExternAccelBox.isChecked();
                if (checked) {
                    Log.i(TAG, "checked");
                    mExternAccelBox.setChecked(false);
                    mUseExternAccel = false;
                } else {
                    Log.i(TAG, "not checked");
                    mExternAccelBox.setChecked(true);
                    mUseExternAccel = true;
                }
            }
        });

        mVelocityBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mVelocityBox.isChecked();
                if (checked) {
                    Log.i(TAG, "checked");
                    mVelocityBox.setChecked(false);
                    mUseVelocity = false;
                } else {
                    Log.i(TAG, "not checked");
                    mVelocityBox.setChecked(true);
                    mUseVelocity = true;
                }
            }
        });

        builder.setView(content)

                .setMessage(getString(R.string.select_sensors))

                //save values to Shared Preferences so they can be read again in the CommandsActivity and the selected features can be activated
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putBoolean(Constants.PREF_KEY_ACCEL_SELECT, mUseAccel);
                        editor.putBoolean(Constants.PREF_KEY_GRAV_SELECT, mUseGrav);
                        editor.putBoolean(Constants.PREF_KEY_GYRO_SELECT, mUseGyro);
                        editor.putBoolean(Constants.PREF_KEY_MAGNET_SELECT, mUseMagnet);
                        editor.putBoolean(Constants.PREF_KEY_ROTVEC_SELECT, mUseRotVec);
                        editor.putBoolean(Constants.PREF_KEY_EXTERN_ACCEL, mUseExternAccel);
                        editor.putBoolean(Constants.PREF_KEY_VELOCITY, mUseVelocity);
                        editor.commit();

                        iOnDialogButtonClickListener activity = (iOnDialogButtonClickListener) getActivity();
                        activity.onFinishSensorDialog(getString(R.string.ok));
                    }
                })

                //neutral Button could be used to open settings

                /*.setNeutralButton(R.string.button_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })*/

                //dialog dismissed automatically, close activity (action taken in implementation of
                // iOnDialogButtonClickListener in CommandsActivity itself)
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        iOnDialogButtonClickListener activity = (iOnDialogButtonClickListener) getActivity();
                        activity.onFinishSensorDialog(getString(R.string.close));
                    }
                });

        return builder.create();
    }
}
