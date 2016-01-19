package de.tum.lmt.texturerecognizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class DialogContinueFragment extends DialogFragment {
	
	private String mStep;
	private String mMessagePart1;
	
	public DialogContinueFragment(String step) {
		mStep = step;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		switch(mStep) {
			case Constants.CALIBRATION:
				mMessagePart1 = getString(R.string.next_camera);
				break;
			case Constants.CAMERA:
				mMessagePart1 = getString(R.string.next_logging);
				break;
		}
		
		builder.setMessage(mMessagePart1 + "\n" + getString(R.string.message_continue))
		   	   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			
			  	   @Override
		 		   public void onClick(DialogInterface dialog, int which) {
			  		   
			  		   switch(mStep) {
			  		   		case Constants.CALIBRATION:
			  		   			Intent intentCam = new Intent(getActivity(), CameraActivity.class);
			  		   			startActivity(intentCam);
			  		   			break;
			  		   		case Constants.CAMERA:
			  		   			Intent intentLogging = new Intent(getActivity(), SensorLoggingActivity.class);
			  		   			startActivity(intentLogging);
			  		   			break;
			  		   }
			  		   getActivity().finish();
		   		   }
		   	   })
		   	   .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				
		   		   @Override
			   	   public void onClick(DialogInterface dialog, int which) {
		   			   
			   	   }
			   });
		
		return builder.create();
	}
	
	/*protected void showMailDialog() {
		
		DialogFragment mailDialog = new DialogMailFragment();
		mailDialog.show(getFragmentManager(), "DialogMailFragment");
	}*/
	
}
