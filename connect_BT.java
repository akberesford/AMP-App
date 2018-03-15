package com.example.alexk_000.ampapp;

import android.Manifest;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View.OnClickListener;
import android.app.Activity;

import org.w3c.dom.Text;

import java.util.Set;
import java.util.UUID;

public class connect_BT extends Activity {
    private Button backBtn;
    private Button nextBtn;
    private TextView pairedDevices;
    private ListView deviceList;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_DEVICE_NAME = "device_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect__bt);

        backBtn = (Button) findViewById(R.id.backBtn);
        nextBtn = (Button) findViewById(R.id.nextBtn);
        pairedDevices = (TextView) findViewById(R.id.pairedDevices);
        deviceList = (ListView) findViewById(R.id.devicesList);
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        deviceList.setAdapter(mPairedDevicesArrayAdapter);
        deviceList.setOnItemClickListener(mDeviceClickListener);

        back();
        next();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBTState();
        Toast.makeText(getBaseContext(),"Here",Toast.LENGTH_SHORT).show();
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        deviceList.setAdapter(mPairedDevicesArrayAdapter);
        deviceList.setOnItemClickListener(mDeviceClickListener);
        pairedDevices = (TextView) findViewById(R.id.pairedDevices);
        deviceList = (ListView) findViewById(R.id.devicesList);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> Devices = mBtAdapter.getBondedDevices();

        if (Devices.size() > 0) {
            for (BluetoothDevice device : Devices) {
                mPairedDevicesArrayAdapter.add(device.getName() + device.getAddress());
            }
        } else {
            mPairedDevicesArrayAdapter.add("No devices paired");
        }
    }

    private void checkBTState() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!mBtAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    public void back() {
        backBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(connect_BT.this, User.class);
                        startActivity(intent);
                    }
                }
        );
    }
    public void next() {
        nextBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(connect_BT.this, LocationActivity.class);
                        startActivity(intent);
                    }
                }
        );
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            String name = info.substring(0,7);

            Intent i = new Intent(connect_BT.this, LocationActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS,address);
            i.putExtra(EXTRA_DEVICE_NAME,name);
            startActivity(i);
        }
    };

}

