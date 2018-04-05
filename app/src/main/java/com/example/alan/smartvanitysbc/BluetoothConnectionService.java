package com.example.alan.smartvanitysbc;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by User on 12/21/2016.
 */

public class BluetoothConnectionService {
    public static final String BROADCAST_FILTER = "BluetoothConection_broadcast_receiver_intent_filter";

    private static final String TAG = "BluetoothConnectionServ";

    private static final String appName = "SmartVanity-SBC";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;

    private ConnectedThread mConnectedThread;


    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

//        Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//        myIntent.setData(Uri.parse("package:" + mContext.getPackageName()));
//        LoginActivity.class.startActivityForResult(myIntent, 1234);


        drawMP();
        start();
    }


    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try{
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            //talk about this is in the 3rd
            if(socket != null){
                connected(socket,mmDevice);
            }

            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_UUID_INSECURE );
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE );
            }

            //will talk about this in the 3rd video
            connected(mmSocket,mmDevice);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    public synchronized void endConnection() {
   /* if (mConnectedThread.mmSocket.isConnected()){
        mConnectedThread.interrupt();
        mConnectThread.cancel();

    }*/

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.interrupt();
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**

     AcceptThread starts and sits waiting for a connection.
     Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     **/


    /**
     Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            boolean breakLoop = false;
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                if (breakLoop) {
                    break;
                }
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    switch (incomingMessage) {
                        case "!01":
                            mView.Update(-10, 0);
                            mView.postInvalidate();
                            break;
                        case "!02":
                            mView.Update(-30, 0);
                            mView.postInvalidate();
                            break;
                        case "!03":
                            mView.Update(-100, 0);
                            mView.postInvalidate();
                            break;
                        case "!04":
                            mView.Update(10, 0);
                            mView.postInvalidate();
                            break;
                        case "!05":
                            mView.Update(30, 0);
                            mView.postInvalidate();
                            break;
                        case "!06":
                            mView.Update(100, 0);
                            mView.postInvalidate();
                            break;
                        case "!07":
                            mView.Update(0, -10);
                            mView.postInvalidate();
                            break;
                        case "!08":
                            mView.Update(0, -30);
                            mView.postInvalidate();
                            break;
                        case "!09":
                            mView.Update(0, -100);
                            mView.postInvalidate();
                            break;
                        case "!10":
                            mView.Update(0, 10);
                            mView.postInvalidate();
                            break;
                        case "!11":
                            mView.Update(0, 30);
                            mView.postInvalidate();
                            break;
                        case "!12":
                            mView.Update(0, 100);
                            mView.postInvalidate();
                            break;
                        case "!13":
                            int loc[] = new int[2];
                            //mView.getLocationOnScreen(loc);
                            loc[0] = mView.x;
                            loc[1] = mView.y;

                            try {
                                Process process = Runtime.getRuntime().exec("su");
                                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                                String cmd = "/system/bin/input tap " + mView.x + " " + mView.y + "\n";
                                os.writeBytes(cmd);
                                os.writeBytes("exit\n");
                                os.flush();
                                os.close();
                                process.waitFor();
                            } catch (Exception e) {
                                Log.e("OKOK", e.getMessage());
                            }

                            Log.d("Debug", "x: " + loc[0] + ", y: " + loc[1]);
                            break;
                        case "!14":
                            endConnection();

                            Intent i = new Intent(BROADCAST_FILTER);
                            i.putExtra("connection_eneded", true);
                            mContext.sendBroadcast(i);
                            breakLoop = true;

                            try {
                                mmSocket.close();

                            } catch (Exception e) {

                            }
                            mmSocket = null;

                            try {
                                mmInStream.close();

                            } catch (Exception e) {

                            }
                            mmInStream = null;

                            try {
                                mmOutStream.close();

                            } catch (Exception e) {

                            }
                            mmOutStream = null;

                            wm.removeView(mView);

                            break;
                        default:
                            processStringInput(incomingMessage);
                    }


                } catch (IOException e) {
                    Log.e(TAG, "read: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }

    OverlayView mView;
    WindowManager.LayoutParams wmParams;
    WindowManager wm;

    public void drawMP() {
        mView = new OverlayView(mContext);
        mView.setWillNotDraw(false);

        wmParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,//TYPE_SYSTEM_ALERT,//TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, //will cover status bar as well!!!
                PixelFormat.TRANSLUCENT);
        wmParams.gravity = Gravity.TOP | Gravity.LEFT;
        wmParams.x = mView.x;
        wmParams.y = mView.y;
        Log.d("DEBUG123", mView.x + "");
        Log.d("DEBUG123", mView.y + "");
        //params.setTitle("Cursor");
        wm = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
        wm.addView(mView, wmParams);

        mView.ShowCursor(true);
    }

    public void processStringInput(String stringInput) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            String cmd = "/system/bin/input text " + stringInput;
            os.writeBytes(cmd);
//            os.writeBytes("exit\n");
            os.flush();
            os.close();
            process.waitFor();
        } catch (Exception e) {
            Log.e("OKOK", e.getMessage());
        }
    }
}
























