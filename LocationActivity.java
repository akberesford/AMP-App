package com.example.alexk_000.ampapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.renderscript.ScriptGroup;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.view.KeyEvent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import java.io.OutputStream;
import java.io.InputStream;
import org.w3c.dom.Text;
import java.util.Set;
import java.util.UUID;
import android.app.Activity;
import android.os.Message;
import java.lang.Object;



public class LocationActivity extends AppCompatActivity implements SensorEventListener {

    private Button restart_button;
    private TextView textView3;
    private Button button;
    private Sensor mSensor;
    private Sensor mSensorRotation;
    private TextView btText;
    private TextView sensorText;

    private SensorManager mSensorManager;
    private SensorManager mSensorManagerRotation;
    private float[] AccelerometerValues;
    private float[] MagneticFieldValues;
    private float[] RotationMatrix;
    private long nextRefreshTime;
    private DecimalFormat df;
    private LocationManager locationManager;
    private LocationListener listener;

    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread mConnectedThread;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address;
    private static String name;
    private static String TAG = "Bluetooth1";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        textView3 = (TextView) findViewById(R.id.textView3);
        restart_button = (Button) findViewById(R.id.button_Restart);
        button = (Button) findViewById(R.id.button);
        btText = (TextView) findViewById(R.id.btInfo);
        sensorText = (TextView) findViewById(R.id.SensorText);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Location location2 = new Location("");
                location2.setLongitude(-118.3215482);
                location2.setLatitude(34.1341151);
                location2.setAltitude(1589.5);
                float[] rotation = new float[9];
                double lng = location.getLongitude();
                double lat = location.getLatitude();
                double alt = location.getAltitude();
                float bear = location.getBearing();
                float bearto = location.bearingTo(location2);
                double PI = 3.141592653589793;
                double endlng = -118.3215482;
                double endlat = 34.1341151;
                double endalt = 1589.5;
                double dlon = endlng * PI / 180 - lng * PI / 180;
                double dlat = endlat * PI / 180 - lat * PI / 180;
                double a;
                double c;
                double d;
                double R;
                float[] rotationMatrix = new float[9];
                float[] orientationAngles = new float[3];

                R = 6378137;
                a = ((Math.sin(dlat / 2)) * (Math.sin(dlat / 2))) + (Math.cos(lat * PI / 180) * Math.cos(endlat * PI / 180) * (Math.sin(dlon / 2)) * (Math.sin(dlon / 2)));
                c = 2 * Math.atan(Math.sqrt(a));
                d = R * c;
                //SensorManager.getRotationMatrixFromVector(rotation, event.);
                //altitude is in meters, longiatude and latitude in degrees
                //distance is in meters
                float[] distance = new float[2];
                location.distanceBetween(lat, lng, endlng, endlat, distance);


                textView3.setText("Long: " + lng + System.getProperty("line.separator") +
                        "Lat: " + lat + System.getProperty("line.separator") +
                        "Alt: " + alt + System.getProperty("line.separator") +
                        "Bearing: " + bear + System.getProperty("line.separator") +
                        "Distance (mi): " + d * 0.000621371 + System.getProperty("line.separator") +
                        "Bearing to: " + bearto);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        configure_button();
        RestartButton();


        mSensorManagerRotation = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorRotation = mSensorManagerRotation.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Sensor sensorAccelerometer = mSensorManagerRotation.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManagerRotation.registerListener(this, sensorAccelerometer,SensorManager.SENSOR_DELAY_UI);
        Sensor SensorMagField = mSensorManagerRotation.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManagerRotation.registerListener(this, SensorMagField, SensorManager.SENSOR_DELAY_UI);

        AccelerometerValues = new float[3];
        MagneticFieldValues = new float[3];
        RotationMatrix = new float[9];
        nextRefreshTime = 0;
        df = new DecimalFormat("#.00");

        Intent intent = getIntent();
        address = intent.getStringExtra(connect_BT.EXTRA_DEVICE_ADDRESS);
        name = intent.getStringExtra(connect_BT.EXTRA_DEVICE_NAME);

        bluetoothIn=new Handler(){
            public void handleMessage(android.os.Message msg){
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("~");
                    if (endOfLineIndex>0){
                        String dataInPrint = recDataString.substring(0,endOfLineIndex);
                        int dataLength = dataInPrint.length();
                        if (recDataString.charAt(0)=='@'){
                            String gpslat = recDataString.substring(1,9);
                            String gpslng = recDataString.substring(11,21);
                            String gpsalt = recDataString.substring(23,33);
                            btText.setText(recDataString);
                        }
                    }

                    recDataString.delete(0, recDataString.length());
                    }
                }
            };
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTstate();
        }

    public void RestartButton() {

        restart_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*try {
                            btSocket.close();
                            Toast.makeText(getBaseContext(),"closed", Toast.LENGTH_LONG).show();
                        } catch (IOException e2) {
                            Toast.makeText(getBaseContext(), "Socket close failed", Toast.LENGTH_LONG).show();
                        }*/
                        Intent intent = new Intent(LocationActivity.this, connect_BT.class);
                        startActivity(intent);
                    }
                }
        );
    }
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }
    void configure_button() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);
            }
            return;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View veiw) {
                locationManager.requestLocationUpdates("gps", 5, 0, listener);
            }
        });
    }

   private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }
    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        address = intent.getStringExtra(connect_BT.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        btAdapter.cancelDiscovery();
        try {
            btSocket.connect();

        } catch (IOException e) {
            try {
                btSocket.close();
                Toast.makeText(getBaseContext(), address, Toast.LENGTH_SHORT).show();
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "Socket close failed", Toast.LENGTH_SHORT).show();
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }
    @Override
    public void onPause() {
        super.onPause();
        try {
            btSocket.close();
            Toast.makeText(getBaseContext(), "closed", Toast.LENGTH_LONG).show();
        } catch (IOException e2) {
            Toast.makeText(getBaseContext(), "Socket close failed", Toast.LENGTH_LONG).show();
        }
    }
    private void checkBTstate() {
        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "Device does not support BT", Toast.LENGTH_SHORT).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    private class ConnectedThread extends Thread{
        private final InputStream mmInStream;
        public ConnectedThread(BluetoothSocket socket){
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e){}
            mmInStream = tmpIn;
        }
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            while(true){
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState,bytes,-1,readMessage).sendToTarget();
                } catch (IOException e){
                    break;
                }
            }
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, AccelerometerValues,0 , AccelerometerValues.length);}
            //Toast.makeText(getBaseContext(),"Accelerometer",Toast.LENGTH_SHORT).show();}
        else if (event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, MagneticFieldValues, 0, MagneticFieldValues.length);}
            //Toast.makeText(getBaseContext(),"Magnetic",Toast.LENGTH_SHORT).show();}


        if (AccelerometerValues!= null && MagneticFieldValues != null){

            if(SensorManager.getRotationMatrix(RotationMatrix,null, AccelerometerValues,MagneticFieldValues)){
                float[] OrientationValues = new float[3];
                SensorManager.getOrientation(RotationMatrix,OrientationValues);
                if (OrientationValues[0]<0) OrientationValues[0]+=2*(float)Math.PI;
                OrientationValues[2] *=  -1;

                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis>nextRefreshTime){
                    nextRefreshTime = currentTimeMillis+250;
                    sensorText.setText("Azimuth "+ AngleToStr(OrientationValues[0]));
                    //Toast.makeText(getBaseContext(),"new time",Toast.LENGTH_SHORT).show();
                    /*sensorText.setText("Azimuth "+ AngleToStr(OrientationValues[0])+System.getProperty("line.separator")+
                    "Pitch "+AngleToStr(OrientationValues[1])+System.getProperty("line.separator")+
                    "Roll "+AngleToStr(OrientationValues[2])+System.getProperty("line.separator")+
                    "GeoMag Field x "+FloatToStr(MagneticFieldValues[0])+System.getProperty("line.separator")+
                    "GeoMag Field y "+FloatToStr(MagneticFieldValues[1])+System.getProperty("line.separator")+
                    "GeoMag Field z "+FloatToStr(MagneticFieldValues[2])+System.getProperty("line.separator")+
                    "Acc x "+ FloatToStr(AccelerometerValues[0])+System.getProperty("line.separator")+
                    "Acc y "+FloatToStr(AccelerometerValues[1])+System.getProperty("line.separator")+
                    "Acc z "+FloatToStr(AccelerometerValues[2]));*/
                }
            }
        }
    }
    private String AngleToStr(double AngleInRadians){
        String Str = "  "+Integer.toString((int)Math.toDegrees(AngleInRadians));
        return Str.substring(Str.length()-3);
    }
    private String FloatToStr(float flt){
        String Str = "  "+df.format(flt);
        return Str.substring(Str.length()-6);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mSensorManagerRotation.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1){}

    /*public class MyBluetoothService{
        private static final String TAG = "BluetoothChatService";

        // Name for the SDP record when creating server socket
        private static final String NAME_SECURE = "BluetoothChatSecure";
        private static final String NAME_INSECURE = "BluetoothChatInsecure";
        //private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        // Member fields
        private final BluetoothAdapter mAdapter;
        private final Handler mHandler;
        private AcceptThread mSecureAcceptThread;
        private AcceptThread mInsecureAcceptThread;
        private ConnectThread mConnectThread;
        private ConnectedThread mConnectedThread;
        private int mState;
        private int mNewState;


        // Constants that indicate the current connection state
        public static final int STATE_NONE = 0;       // we're doing nothing
        public static final int STATE_LISTEN = 1;     // now listening for incoming connections
        public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
        public static final int STATE_CONNECTED = 3;  // now connected to a remote device


        public static final int MESSAGE_READ = 2;
        public static final int MESSAGE_WRITE = 3;
        public static final int MESSAGE_TOAST = 5;
        public static final int MESSAGE_STATE_CHANGE = 1;
        public static final int MESSAGE_DEVICE_NAME = 4;
        public static final String DEVICE_NAME = "device_name";
        public static final String TOAST = "toast";


        public MyBluetoothService(Context context, Handler handler) {
            mAdapter = BluetoothAdapter.getDefaultAdapter();
            mState = STATE_NONE;
            mNewState = mState;
            mHandler = handler;
        }

        *//**
         * Update UI title according to the current state of the chat connection
         *//*
        private synchronized void updateUserInterfaceTitle() {
            mState = getState();
            //Toast.makeText(getBaseContext(), "New State:"+ mState, Toast.LENGTH_SHORT).show();
            mNewState = mState;

            // Give the new state to the Handler so the UI Activity can update
            mHandler.obtainMessage(MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
        }

        *//**
         * Return the current connection state.
         *//*
        public synchronized int getState() {
            return mState;
        }

        *//**
         * Start the chat service. Specifically start AcceptThread to begin a
         * session in listening (server) mode. Called by the Activity onResume()
         *//*
        public synchronized void start() {
            Log.d(TAG, "start");

            // Cancel any thread attempting to make a connection
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            // Start the thread to listen on a BluetoothServerSocket
            if (mSecureAcceptThread == null) {
                mSecureAcceptThread = new AcceptThread(true);
                mSecureAcceptThread.start();
            }
            if (mInsecureAcceptThread == null) {
                mInsecureAcceptThread = new AcceptThread(false);
                mInsecureAcceptThread.start();
            }
            // Update UI title
            updateUserInterfaceTitle();
        }

        *//**
         * Start the ConnectThread to initiate a connection to a remote device.
         *
         * @param device The BluetoothDevice to connect
         * @param secure Socket Security type - Secure (true) , Insecure (false)
         *//*
        public synchronized void connect(BluetoothDevice device, boolean secure) {
            Log.d(TAG, "connect to: " + device);

            // Cancel any thread attempting to make a connection
            if (mState == STATE_CONNECTING) {
                if (mConnectThread != null) {
                    mConnectThread.cancel();
                    mConnectThread = null;
                }
            }

            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            // Start the thread to connect with the given device
            mConnectThread = new ConnectThread(device, secure);
            mConnectThread.start();
            // Update UI title
            updateUserInterfaceTitle();
        }

        *//**
         * Start the ConnectedThread to begin managing a Bluetooth connection
         *
         * @param socket The BluetoothSocket on which the connection was made
         * @param device The BluetoothDevice that has been connected
         *//*
        public synchronized void connected(BluetoothSocket socket, BluetoothDevice
                device, final String socketType) {
            Log.d(TAG, "connected, Socket Type:" + socketType);

            // Cancel the thread that completed the connection
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            // Cancel the accept thread because we only want to connect to one device
            if (mSecureAcceptThread != null) {
                mSecureAcceptThread.cancel();
                mSecureAcceptThread = null;
            }
            if (mInsecureAcceptThread != null) {
                mInsecureAcceptThread.cancel();
                mInsecureAcceptThread = null;
            }

            // Start the thread to manage the connection and perform transmissions
            mConnectedThread = new ConnectedThread(socket, socketType);
            mConnectedThread.start();

            // Send the name of the connected device back to the UI Activity
            Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(DEVICE_NAME, device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            // Update UI title
            updateUserInterfaceTitle();
        }

        *//**
         * Stop all threads
         *//*
        public synchronized void stop() {
            Log.d(TAG, "stop");

            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            if (mSecureAcceptThread != null) {
                mSecureAcceptThread.cancel();
                mSecureAcceptThread = null;
            }

            if (mInsecureAcceptThread != null) {
                mInsecureAcceptThread.cancel();
                mInsecureAcceptThread = null;
            }
            mState = STATE_NONE;
            // Update UI title
            updateUserInterfaceTitle();
        }

        *//**
         * Write to the ConnectedThread in an unsynchronized manner
         *
         * @param out The bytes to write
         * @see ConnectedThread#write(byte[])
         *//*
        *//*public void write(byte[] out) {
            // Create temporary object
            ConnectedThread r;
            // Synchronize a copy of the ConnectedThread
            synchronized (this) {
                if (mState != STATE_CONNECTED) return;
                r = mConnectedThread;
            }
            // Perform the write unsynchronized
            r.write(out);
        }*//*

        *//**
         * Indicate that the connection attempt failed and notify the UI Activity.
         *//*
        private void connectionFailed() {
            // Send a failure message back to the Activity
            Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(TOAST, "Unable to connect device");
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            mState = STATE_NONE;
            // Update UI title
            updateUserInterfaceTitle();

            // Start the service over to restart listening mode
            MyBluetoothService.this.start();
        }

        *//**
         * Indicate that the connection was lost and notify the UI Activity.
         *//*
        private void connectionLost() {
            // Send a failure message back to the Activity
            Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(TOAST, "Device connection was lost");
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            mState = STATE_NONE;
            // Update UI title
            updateUserInterfaceTitle();

            // Start the service over to restart listening mode
            MyBluetoothService.this.start();
        }

        *//**
         * This thread runs while listening for incoming connections. It behaves
         * like a server-side client. It runs until a connection is accepted
         * (or until cancelled).
         *//*
        private class AcceptThread extends Thread {
            // The local server socket
            private final BluetoothServerSocket mmServerSocket;
            private String mSocketType;

            public AcceptThread(boolean secure) {
                BluetoothServerSocket tmp = null;
                mSocketType = secure ? "Secure" : "Insecure";

                // Create a new listening server socket
                try {
                    if (secure) {
                        tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                                BTMODULEUUID);
                    } else {
                        tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                                NAME_INSECURE, BTMODULEUUID);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
                }
                mmServerSocket = tmp;
                mState = STATE_LISTEN;
            }

            public void run() {
                Log.d(TAG, "Socket Type: " + mSocketType +
                        "BEGIN mAcceptThread" + this);
                setName("AcceptThread" + mSocketType);

                BluetoothSocket socket = null;

                // Listen to the server socket if we're not connected
                while (mState != STATE_CONNECTED) {
                    try {
                        // This is a blocking call and will only return on a
                        // successful connection or an exception
                        socket = mmServerSocket.accept();
                    } catch (IOException e) {
                        Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                        break;
                    }

                    // If a connection was accepted
                    if (socket != null) {
                        synchronized (MyBluetoothService.this) {
                            switch (mState) {
                                case STATE_LISTEN:
                                case STATE_CONNECTING:
                                    // Situation normal. Start the connected thread.
                                    connected(socket, socket.getRemoteDevice(),
                                            mSocketType);
                                    break;
                                case STATE_NONE:
                                case STATE_CONNECTED:
                                    // Either not ready or already connected. Terminate new socket.
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        Log.e(TAG, "Could not close unwanted socket", e);
                                    }
                                    break;
                            }
                        }
                    }
                }
                Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

            }

            public void cancel() {
                Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
                }
            }
        }


        *//**
         * This thread runs while attempting to make an outgoing connection
         * with a device. It runs straight through; the connection either
         * succeeds or fails.
         *//*
        private class ConnectThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final BluetoothDevice mmDevice;
            private String mSocketType;

            public ConnectThread(BluetoothDevice device, boolean secure) {
                mmDevice = device;
                BluetoothSocket tmp = null;
                mSocketType = secure ? "Secure" : "Insecure";

                // Get a BluetoothSocket for a connection with the
                // given BluetoothDevice
                try {
                    if (secure) {
                        tmp = device.createRfcommSocketToServiceRecord(
                                BTMODULEUUID);
                    } else {
                        tmp = device.createInsecureRfcommSocketToServiceRecord(
                                BTMODULEUUID);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
                }
                mmSocket = tmp;
                mState = STATE_CONNECTING;
            }

            public void run() {
                Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
                setName("ConnectThread" + mSocketType);

                // Always cancel discovery because it will slow down a connection
                mAdapter.cancelDiscovery();

                // Make a connection to the BluetoothSocket
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmSocket.connect();
                } catch (IOException e) {
                    // Close the socket
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "unable to close() " + mSocketType +
                                " socket during connection failure", e2);
                    }
                    connectionFailed();
                    return;
                }

                // Reset the ConnectThread because we're done
                synchronized (MyBluetoothService.this) {
                    mConnectThread = null;
                }

                // Start the connected thread
                connected(mmSocket, mmDevice, mSocketType);
            }

            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
                }
            }
        }

        *//**
         * This thread runs during a connection with a remote device.
         * It handles all incoming and outgoing transmissions.
         *//*
        private class ConnectedThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final InputStream mmInStream;
            //private final OutputStream mmOutStream;

            public ConnectedThread(BluetoothSocket socket, String socketType) {
                Log.d(TAG, "create ConnectedThread: " + socketType);
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the BluetoothSocket input and output streams
                try {
                    tmpIn = socket.getInputStream();
                    //tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                    Log.e(TAG, "temp sockets not created", e);
                }

                mmInStream = tmpIn;
                //mmOutStream = tmpOut;
                mState = STATE_CONNECTED;
            }

            public void run() {
                Log.i(TAG, "BEGIN mConnectedThread");
                byte[] buffer = new byte[1024];
                int bytes;

                // Keep listening to the InputStream while connected
                while (mState == STATE_CONNECTED) {
                    try {
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);

                        // Send the obtained bytes to the UI Activity
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
                    } catch (IOException e) {
                        Log.e(TAG, "disconnected", e);
                        connectionLost();
                        break;
                    }
                }
            }

            *//*public void write(byte[] buffer) {
                try {
                    mmOutStream.write(buffer);

                    // Share the sent message back to the UI Activity
                    mHandler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                }
            }*//*

            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "close() of connect socket failed", e);
                }
            }
        }
    }



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Toast.makeText(getBaseContext(), "here", Toast.LENGTH_SHORT).show();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(getBaseContext(), "Bluetooth Device Found", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Toast.makeText(getBaseContext(), "Bluetooth Device Connected", Toast.LENGTH_SHORT).show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getBaseContext(), "Bluetooth Finished Discovery", Toast.LENGTH_SHORT).show();

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Toast.makeText(getBaseContext(), "Bluetooth Device Disconnected Request", Toast.LENGTH_SHORT).show();

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(getBaseContext(), "Bluetooth Device Disconnected", Toast.LENGTH_SHORT).show();

            }
        }
    };*/
}
