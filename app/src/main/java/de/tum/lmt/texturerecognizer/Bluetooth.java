package de.tum.lmt.texturerecognizer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Kev94 on 15.01.2016.
 */
public class Bluetooth {

    private static final String TAG = "Bluetooth";

    private Context mContext;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mSocket;

    private Thread mWorkerThread;

    private OutputStream mOutputStream;
    private InputStream mInputStream;
    volatile boolean stopWorker;

    int readBufferPosition = 0;
    byte[] readBuffer = new byte[1024];

    StringBuilder mSB;
    String mData;

    public Bluetooth(Context context) {
        mContext = context;
        mSB = new StringBuilder();
    }

    public void findBluetooth() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            Toast.makeText(mContext, R.string.no_bluetooth, Toast.LENGTH_LONG).show();
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                Log.i("TAG", "Current found device: " + device.getName());
                if(device.getName().equals("HC-06"))
                {
                    mBluetoothDevice = device;
                    Toast.makeText(mContext, mContext.getString(R.string.device_found, "HC-06"), Toast.LENGTH_LONG).show();
                    break;
                }
                else
                {
                    Log.e("TAG","No BT module found!!!");
                }
            }
        }
    }

    void openBT() throws IOException
    {
        Log.i("TAG","3");
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        Log.i("TAG", "4");
        if(mBluetoothDevice != null)
        {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            Log.i("TAG", "5");
            mSocket.connect();
            Log.i("TAG","6");
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
            Log.i("TAG", "7");
            beginListenForData();

            Log.i(TAG, "Bluetooth device HTC-06 opened!");
        }
        else
        {
            Log.i(TAG, "No Bluetooth device could be opened...");
        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        mWorkerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            //Log.i(TAG, "appended Data: " + data);

                                            int i = 0;
                                            String data2 = "";


                                            while(data.charAt(i) != '\n') {

                                                switch(data.charAt(i)) {
                                                    case '0':
                                                    case '1':
                                                    case '2':
                                                    case '3':
                                                    case '4':
                                                    case '5':
                                                    case '6':
                                                    case '7':
                                                    case '8':
                                                    case '9':
                                                    case '\t':

                                                        data2 = data2.concat(data.charAt(i) + "");

                                                        break;
                                                    default:

                                                        data2 = data2.concat("x");

                                                        break;
                                                }

                                                i++;
                                            }

                                            mSB.append(data2);
                                            mSB.append("\n");
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        mWorkerThread.start();
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        if(mOutputStream != null)
            mOutputStream.close();
        if(mInputStream != null)
            mInputStream.close();
        if(mSocket != null)
            mSocket.close();
        Log.i(TAG, "Bluetooth device closed!");
    }

    String getData() {

        if(stopWorker) {
            Log.i(TAG, "stopWorker is true: " + mSB.toString());
            mData = mSB.toString();
            return mData;
        }

        return null;
    }
}
