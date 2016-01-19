// Dima: Here the fragment is not fullscreen, it seems not so obvious how to make it fullscreen though
// worth a try to rewrite this fragment according to example in 
// http://www.techrepublic.com/article/pro-tip-unravel-the-mystery-of-androids-full-screen-dialog-fragments/
package de.tum.lmt.texturerecognizer;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Toast;

public class DialogSendFragment extends DialogFragment {

	private static final String TAG = DialogSendFragment.class.getSimpleName();
	private String dataToSendPath = MainActivity.getLoggingDir().getAbsolutePath() + Constants.DATA_TO_SEND_FOLDER_NAME;
	private File dataToSendDir;

	private BluetoothAdapter mBluetoothAdapter;

	private CheckBox checkboxMail;
	private CheckBox checkboxBluetooth;

	class FileToSendName {
		File file;
		String name;
		FileToSendName(File file_, String name_) {
			file = file_;
			name = name_;
		}
	}	
	
	void sendOverBluetooth(ArrayList<FileToSendName> fileNames) {
		if (fileNames.size()>0) {
			Log.i(TAG, "sending multiple files");
			Intent sendBt = new Intent(Intent.ACTION_SEND_MULTIPLE);
			sendBt.setType("*/*"); // previously this was sendBt.setType("image/*"); but it does not work on some devices so "*/*"
			sendBt.setPackage("com.android.bluetooth");
			ArrayList<Uri> uris = new ArrayList<Uri>();
			for (FileToSendName n : fileNames) {
				if (n.file != null) {
					Uri currentUri = Uri.fromFile(n.file);
					if (currentUri != null) {
						uris.add(currentUri);
					}
					else {
						Toast.makeText(getActivity().getApplicationContext(), "One file empty " + n.name, Toast.LENGTH_SHORT).show();
					}
				}
			}
			if (uris.size()>0) {
				sendBt.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				startActivity(sendBt);
				Log.i(TAG, "Creating bluetooth intent:and now starting stuff");
			}
			else {
				Toast.makeText(getActivity().getApplicationContext(), "Empty files", Toast.LENGTH_LONG).show();
			}
		}
		else {
			Toast.makeText(getActivity().getApplicationContext(), "Empty files", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.dialog_send, null);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		dataToSendDir = new File(dataToSendPath);

		if (mBluetoothAdapter == null) {
			Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
			dismiss();
			return null;
		}

		checkboxMail = (CheckBox) content.findViewById(R.id.checkbox_mail);
		checkboxMail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean checked = checkboxBluetooth.isChecked();
				if(checked) {
					checkboxBluetooth.setChecked(false);
				}
			}
		});
		
		checkboxMail.setEnabled(false);
		checkboxMail.setClickable(false);

		checkboxBluetooth = (CheckBox) content.findViewById(R.id.checkbox_bluetooth);
		checkboxBluetooth.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean checked = checkboxMail.isChecked();
				if(checked) {
					checkboxMail.setChecked(false);
				}
			}
		});

		builder.setView(content).setMessage(R.string.message_dialog_send)
		.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				Log.i(TAG, "DialongSendFragment called, getting URIS");

				if(checkboxMail.isChecked()) {
					//showMailDialog();
				}
				else if(checkboxBluetooth.isChecked()) {

					File[] fileList = dataToSendDir.listFiles();						   

					ArrayList<FileToSendName> fileNames = new ArrayList<FileToSendName>();
					
					for (String name : Constants.NAMES_OF_BLUETOOTH_FILES_TO_SEND) {
						fileNames.add(new FileToSendName(null, name));
					}

					// find which one is there
					for(File file : fileList) {
						for (FileToSendName n : fileNames) {
							if (file.getName().contains(n.name)) {
								n.file = file;
								n.name = file.getName();
							}
						}
					}
					
					sendOverBluetooth(fileNames);
					
				}
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

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
