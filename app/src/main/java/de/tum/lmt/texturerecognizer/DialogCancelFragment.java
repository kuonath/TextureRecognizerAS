package de.tum.lmt.texturerecognizer;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class DialogCancelFragment extends DialogFragment {
	
	Context mContext;
	
	private File mLoggingDir = MainActivity.getLoggingDir();
	
	//UI Elements
	private CheckBox mCheckboxKeepData;
	private CheckBox mCheckboxDeleteData;
	
	private String mGapFiller;
	private boolean mFinalValues;
	
	public DialogCancelFragment(Context context, String gapFiller, boolean finalValues) {
		
		mContext = context;
		mGapFiller = gapFiller;
		mFinalValues = finalValues;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.dialog_cancel, null);
		
		mCheckboxKeepData = (CheckBox) content.findViewById(R.id.checkbox_keep_data);
		mCheckboxKeepData.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean checked = mCheckboxDeleteData.isChecked();
				if(checked) {
					mCheckboxDeleteData.setChecked(false);
				}
			}
		});
		
		if(mFinalValues && (getActivity().getClass() == SensorLoggingActivity.class)) {
			mCheckboxKeepData.setText(R.string.checkbox_keep_data_not_analyzed);
		}
		
		mCheckboxDeleteData = (CheckBox) content.findViewById(R.id.checkbox_delete_data);
		mCheckboxDeleteData.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean checked = mCheckboxKeepData.isChecked();
				if(checked) {
					mCheckboxKeepData.setChecked(false);
				}
			}
		});
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean mode = sharedPrefs.getBoolean(Constants.PREF_KEY_MODE_SELECT, false);
		
		if(mode) {
			mCheckboxKeepData.setVisibility(View.GONE);
			mCheckboxDeleteData.setVisibility(View.GONE);
		}
		
		builder.setView(content);
		if(!mode) {
			builder.setMessage(getString((R.string.message_dialog_cancel), mGapFiller));
		}
		else {
			builder.setMessage(getString((R.string.message_dialog_cancel_database), mGapFiller));
		}
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			
			  	   @Override
		 		   public void onClick(DialogInterface dialog, int which) {
			  		   
			  		   if(mCheckboxDeleteData.isChecked()) {
			  			   
			  			   File[] files = mLoggingDir.listFiles();
			  			   
			  			   if(files != null) {
			  				   for(File file : files) {
			  					   if(!file.isDirectory()) {
			  						   file.delete();
			  					   }
			  					   else {
			  						   for(File child : file.listFiles()) {
			  							   child.delete();
			  						   }
			  						   file.delete();
			  					   }
			  				   }
			  			   }
			  				   
			  			   mLoggingDir.delete();
			  		   }
			  		   else if(mCheckboxKeepData.isChecked()) {
			  			   
			  			   File newFileName;
			  			   
			  			   if(getActivity().getClass() == SensorLoggingActivity.class) {
			  				   if(mFinalValues) {
			  					   newFileName = new File(mLoggingDir.getAbsolutePath() + "_not_analyzed");
			  				   }
			  				   else {
			  					   newFileName = new File(mLoggingDir.getAbsolutePath() + "_unfinished");
			  				   }
			  			   } 
			  			   else {
			  				 newFileName = new File(mLoggingDir.getAbsolutePath() + "_unfinished");
			  			   }
			  			   
			  			   mLoggingDir.renameTo(newFileName);
			  		   }
			  		   
			  		   getActivity().finish();
		   		   }
		   	   })
		   	   .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				
		   		   @Override
			   	   public void onClick(DialogInterface dialog, int which) {
		   			   if(!mFinalValues) {
		   				   getActivity().finish();
		   				   getActivity().startActivityForResult(getActivity().getIntent(), 10);
		   			   }
		   			   DialogCancelFragment.this.getDialog().cancel();
			   	   }
			   });
		
		return builder.create();
	}
}
