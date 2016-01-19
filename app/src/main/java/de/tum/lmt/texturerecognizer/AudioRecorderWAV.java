package de.tum.lmt.texturerecognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.preference.PreferenceManager;
import android.util.Log;

//From http://www.edumobile.org/android/audio-recording-in-wav-format-in-android-programming/

public class AudioRecorderWAV {

	private Context mContext;
	
	private File loggingDir = MainActivity.getLoggingDir();
	private String dataToSendPath = MainActivity.getLoggingDir().getAbsolutePath() + Constants.DATA_TO_SEND_FOLDER_NAME;
	private File dataToSendDir;
	private static final int RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp" + AUDIO_RECORDER_FILE_EXT_WAV;
	private static final int RECORDER_SAMPLERATE = Constants.RECORDER_SAMPLING_RATE;

	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final long HEADER_LENGTH = 36;
	private ArrayList<Byte> dataArray = new ArrayList<Byte>(); 

	private int channels_num = 1;
	private static int RECORDER_CHANNELS;// = AudioFormat.CHANNEL_IN_MONO;//AudioFormat.CHANNEL_IN_STEREO;

	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	private String TAG = "AudioRecorderWAV";

	public AudioRecorderWAV(Context context) {
		
		mContext = context;
		
		if (channels_num == 1) {
			Log.i(TAG, "NumChannels " + channels_num);
			RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
		}
		else {
			RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
			Log.i(TAG, "NumChannels " + channels_num);
		}
		bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				RECORDER_CHANNELS,
				AudioFormat.ENCODING_PCM_16BIT);
		Log.i("AudioRecorderWav", "creating instance of audio recorder " + dataToSendPath);    	
		dataToSendDir = new File(dataToSendPath);

	}

	private String getFilename(){
		return (loggingDir.getAbsolutePath() + File.separator + Constants.AUDIO_FILENAME + AUDIO_RECORDER_FILE_EXT_WAV);
	}

	private String getFilename1(){
		return (dataToSendDir.getAbsolutePath() + File.separator + Constants.AUDIO_FILENAME1 + AUDIO_RECORDER_FILE_EXT_WAV);
	}

	private String getFilename2(){
		return (dataToSendDir.getAbsolutePath() + File.separator + Constants.AUDIO_FILENAME2 + AUDIO_RECORDER_FILE_EXT_WAV);
	}

	private String getTempFilename(){

		File file = new File(loggingDir.getAbsolutePath());

		if(!file.exists()){
			file.mkdirs();
		}

		File tempFile = new File(loggingDir.getAbsolutePath());

		if(tempFile.exists())
			tempFile.delete();

		return (file.getAbsolutePath() + File.separator + AUDIO_RECORDER_TEMP_FILE);
	}	

	public void startRecording(){
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
		if(recorder.getState()==1) {
			recorder.startRecording();
		}

		isRecording = true;
		recordingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				writeAudioDataToFile();
			}
		},"AudioRecorder Thread");
		
		recordingThread.start();
	}

	private void writeAudioDataToFile(){
		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();
		Log.i(TAG, "WriteAUdioDataToFile, name " + filename);
		FileOutputStream os = null;

		try {
			os = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(null != os){
			while(isRecording){
				int read = recorder.read(data, 0, bufferSize);
				if(AudioRecord.ERROR_INVALID_OPERATION != read){
					try {
						os.write(data);
						// add to arraylist
						for (int i=0; i<data.length; i++) {
							dataArray.add(data[i]);
						}
						Log.i(TAG, "Added to soundArray "  + data.length + " bytes"); 
						// TODO
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void stopRecording(){
		if(null != recorder){
			isRecording = false;
			if(recorder.getState()==1) {
				recorder.stop();
			}
			writeToSound();
			recorder.release();
			recorder = null;
			recordingThread = null;
		}

		copyWaveFile(getTempFilename(),getFilename());
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean mode = sharedPrefs.getBoolean(Constants.PREF_KEY_MODE_SELECT, false);
		
		if(!mode) {
			copyWaveFile(getTempFilename(), getFilename1());
			copyWaveFile(getTempFilename(), getFilename2());
		}	
		deleteTempFile();
		
	}

	private void deleteTempFile() {
		File file = new File(getTempFilename());
		file.delete();
	}


	public void writeToSound() {
		Constants.SOUND_ARRAY = dataArray.toArray(new Byte[dataArray.size()]);
	}
	
	public void writeWaveFile(byte[] data, String targetPath, int num_channels, int audioLen)
	{
		FileOutputStream out = null;
		long longSampleRate = RECORDER_SAMPLERATE;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * num_channels/8;
		try {
			out = new FileOutputStream(targetPath);
			long totalDataLen = audioLen + HEADER_LENGTH;
			WriteWaveFileHeader(out, audioLen, totalDataLen, longSampleRate, num_channels, byteRate);
			out.write(data);
			
			
			
			
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void copyWaveFile(String inFilename,String outFilename){
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + HEADER_LENGTH;
		long longSampleRate = RECORDER_SAMPLERATE;

		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels_num/8;

		byte[] data = new byte[bufferSize];

		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + HEADER_LENGTH;

			//AppLog.logString("File size: " + totalDataLen);

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels_num, byteRate);

			while(in.read(data) != -1){
				out.write(data);
			}

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void WriteWaveFileHeader(
			FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {

		int numBytesHeader = 44;
		byte[] header = new byte[numBytesHeader];

		header[0] = 'R';  // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8);  // block align
		header[33] = 0;
		header[34] = RECORDER_BPP;  // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, numBytesHeader);
	}
}
