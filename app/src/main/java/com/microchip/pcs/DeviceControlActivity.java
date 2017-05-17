
package com.microchip.pcs;
import java.util.ArrayList;
import java.util.List;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.note.TextNote;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import java.util.UUID;

/**
 * This Activity receives CRC_checkArray Bluetooth device address provides the user interface to connect, display data, and display GATT services
 * and characteristics supported by the device. The Activity communicates with {@code BluetoothLeService}, which in turn
 * interacts with the Bluetooth LE API.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressWarnings("deprecation")
public class DeviceControlActivity extends FragmentActivity implements ActionBar.TabListener,View.OnClickListener, DiscreteSeekBar.OnProgressChangeListener {

    private ActionBar actionBar;
    public static String spmin,spmax,cp,sw,sen,p1r,p2r,csa,str;
    private final static String TAG = DeviceControlActivity.class.getSimpleName();      //Get name of activity to tag debug and warning messages
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";                      //Name passed by intent that lanched this activity
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";                //MAC address passed by intent that lanched this activity
    private static final String MLDP_PRIVATE_SERVICE = "00035b03-58e6-07dd-021a-08123a000300"; //Private service for Microchip MLDP
    private static final String MLDP_DATA_PRIVATE_CHAR = "00035b03-58e6-07dd-021a-08123a000301"; //Characteristic for MLDP Data, properties - notify, write
    private static final String MLDP_CONTROL_PRIVATE_CHAR = "00035b03-58e6-07dd-021a-08123a0003ff"; //Characteristic for MLDP Control, properties - read, write
    private static final String CHARACTERISTIC_NOTIFICATION_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";	//Special UUID for descriptor needed to enable notifications//BluetoothAdapter controls the Bluetooth radio in the phone
    private BluetoothGatt mBluetoothGatt;                                               //BluetoothGatt controls the Bluetooth communication link
    private BluetoothGattCharacteristic mDataMDLP;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;                                           //Handler used to send die roll after CRC_checkArray time delay
    private String mDeviceAddress;                                         //Strings for the Bluetooth device name and MAC address
    private boolean mConnected = false;
    private SpeedView speedView1,speedView2;
    private Button button ,button1;
    private DiscreteSeekBar dsb;
    private Vibrator vibe;
    private Handler mHandler;
    private  List<Byte> Received_bytes=new ArrayList<>();
    private boolean isPressed = false;
    private MediaPlayer mp;
    private int CRC_checkArray[]=new int[9];
    private int Received_array[]=new int[9];
    private int CRC_Return,counter;
    private int progress;
    private RadioButton rb;
    private int flag=0;

    //High-Order Byte Table
/* Table of CRC values for high–order byte */
    static  int auchCRCHi[] = {
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
            0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
            0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01,
            0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81,
            0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
            0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01,
            0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
            0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
            0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01,
            0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
            0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
            0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01,
            0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
            0x40
    } ;
    //Low-Order Byte Table
/* Table of CRC values for low–order byte */
    static int auchCRCLo[] = {
            0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2, 0xC6, 0x06, 0x07, 0xC7, 0x05, 0xC5, 0xC4,
            0x04, 0xCC, 0x0C, 0x0D, 0xCD, 0x0F, 0xCF, 0xCE, 0x0E, 0x0A, 0xCA, 0xCB, 0x0B, 0xC9, 0x09,
            0x08, 0xC8, 0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB, 0xDA, 0x1A, 0x1E, 0xDE, 0xDF, 0x1F, 0xDD,
            0x1D, 0x1C, 0xDC, 0x14, 0xD4, 0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6, 0xD2, 0x12, 0x13, 0xD3,
            0x11, 0xD1, 0xD0, 0x10, 0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3, 0xF2, 0x32, 0x36, 0xF6, 0xF7,
            0x37, 0xF5, 0x35, 0x34, 0xF4, 0x3C, 0xFC, 0xFD, 0x3D, 0xFF, 0x3F, 0x3E, 0xFE, 0xFA, 0x3A,
            0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38, 0x28, 0xE8, 0xE9, 0x29, 0xEB, 0x2B, 0x2A, 0xEA, 0xEE,
            0x2E, 0x2F, 0xEF, 0x2D, 0xED, 0xEC, 0x2C, 0xE4, 0x24, 0x25, 0xE5, 0x27, 0xE7, 0xE6, 0x26,
            0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0, 0xA0, 0x60, 0x61, 0xA1, 0x63, 0xA3, 0xA2,
            0x62, 0x66, 0xA6, 0xA7, 0x67, 0xA5, 0x65, 0x64, 0xA4, 0x6C, 0xAC, 0xAD, 0x6D, 0xAF, 0x6F,
            0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8, 0x68, 0x78, 0xB8, 0xB9, 0x79, 0xBB,
            0x7B, 0x7A, 0xBA, 0xBE, 0x7E, 0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C, 0xB4, 0x74, 0x75, 0xB5,
            0x77, 0xB7, 0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71, 0x70, 0xB0, 0x50, 0x90, 0x91,
            0x51, 0x93, 0x53, 0x52, 0x92, 0x96, 0x56, 0x57, 0x97, 0x55, 0x95, 0x94, 0x54, 0x9C, 0x5C,
            0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E, 0x5A, 0x9A, 0x9B, 0x5B, 0x99, 0x59, 0x58, 0x98, 0x88,
            0x48, 0x49, 0x89, 0x4B, 0x8B, 0x8A, 0x4A, 0x4E, 0x8E, 0x8F, 0x4F, 0x8D, 0x4D, 0x4C, 0x8C,
            0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86, 0x82, 0x42, 0x43, 0x83, 0x41, 0x81, 0x80,
            0x40
    };


    // ----------------------------------------------------------------------------------------------------------------
    // Activity launched
    // Invoked by Intent in onListItemClick method in DeviceScanActivity
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.die_screen);                                            //Show the screen with the die number and button
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        this.getActionBar().setTitle("MultiEVo");                           //Display "BLE Device Scan" on the action bar
        mp = MediaPlayer.create(this, R.raw.click);
        final Intent intent = getIntent();                                              //Get the Intent that launched this activity
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);                  //Get the BLE device address from the Intent
        mHandler = new Handler();                                                       //Create Handler to delay sending first roll after new connection
        dsb=(DiscreteSeekBar)findViewById(R.id.dsb);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        dsb.setMax(255);
        dsb.setOnProgressChangeListener(this);
        rb=(RadioButton)(findViewById(R.id.radioButton));
        rb.setChecked(false);
        speedView1 = (SpeedView) findViewById(R.id.speedView1);
        speedView2 = (SpeedView) findViewById(R.id.speedView2);
        speedView1.setMaxSpeed(255);
        speedView2.setMaxSpeed(255);
        speedView1.setMinSpeed(0);
        speedView2.setMinSpeed(0);
        speedView1.setWithTremble(false);
        speedView1.setTextSize(1);
        speedView2.setTextSize(1);
        speedView1.setUnit("PSI");
        speedView2.setUnit("Hz");
        speedView1.setSpeedTextColor(R.color.GreenYellow);
        speedView2.setSpeedTextColor(R.color.Red);
        button=(Button)(findViewById(R.id.button2));
        button1=(Button)(findViewById(R.id.button1));
        button.setOnClickListener(this);
        button1.setOnClickListener(this);
        rb.setChecked(false);
        button.setBackgroundResource(R.drawable.off);
        this.getActionBar().setDisplayHomeAsUpEnabled(true);                            //Make home icon clickable with < symbol on the left
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE); //Get the BluetoothManager
        mBluetoothAdapter = bluetoothManager.getAdapter();                              //Get CRC_checkArray reference to the BluetoothAdapter (radio)
        if (mBluetoothAdapter == null) {                                                //Check if we got the BluetoothAdapter
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show(); //Message that Bluetooth is not supported
            finish();                                                                   //End the activity
        }
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Activity resumed
    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
           scanLeDevice(true);
        }


        if (mBluetoothAdapter == null || mDeviceAddress == null) {                      //Check that we still have CRC_checkArray Bluetooth adappter and device address
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");     //Warn that something went wrong
            finish();                                                                   //End the Activity
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress); //Get the Bluetooth device by referencing its address
        if (device == null) {                                                           //Check whether CRC_checkArray device was returned
            Log.w(TAG, "Device not found.  Unable to connect.");                        //Warn that something went wrong
            finish();                                                                   //End the Activity
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);                //Directly connect to the device so autoConnect is false
        Log.d(TAG, "Trying to create CRC_checkArray new connection.");

    }
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);

                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.toString());
                            connectToDevice(device);
                        }
                    });
                }
            };

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    vibe.vibrate(100);
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    vibe.vibrate(100);
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            gatt.readCharacteristic(services.get(1).getCharacteristics().get
                    (0));
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
        }
    };


    // ----------------------------------------------------------------------------------------------------------------
    // Activity paused
    @Override
    protected void onPause() {
        super.onPause();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Activity is ending
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothGatt != null) {                                            //If there is CRC_checkArray valid GATT connection
            mBluetoothGatt.disconnect();                                        // then disconnect
        }
        mBluetoothGatt.close();                                                         //Close the connection
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Options menu is different depending on whether connected or not
    // Show Connect option if not connected or show Disconnect option if we are connected
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);                          //Show the Options menu
        if (mConnected) {                                                               //See if connected
            menu.findItem(R.id.menu_connect).setVisible(false);                         // then dont show disconnect option
            menu.findItem(R.id.menu_disconnect).setVisible(true);                       // and do show connect option
        }
        else {                                                                          //If not connected
            menu.findItem(R.id.menu_connect).setVisible(true);                          // then show connect option
            menu.findItem(R.id.menu_disconnect).setVisible(false);                      // and don't show disconnect option
        }
        return true;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Menu item selected
    // Connect or disconnect to BLE device
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {                                                     //Get which menu item was selected
            case R.id.menu_connect:                                                     //Option to Connect chosen
                if(mBluetoothGatt != null) {                                            //If there is CRC_checkArray valid GATT connection
                    mBluetoothGatt.connect();                                           // then connect

                    updateDieState();
                }
                return true;
            case R.id.menu_disconnect:                                                  //Option to Disconnect chosen
                if(mBluetoothGatt != null) {                                            //If there is CRC_checkArray valid GATT connection
                    mBluetoothGatt.disconnect();                                        // then disconnect

                }
                return true;
            case android.R.id.home:                                                     //Option to go back was chosen
                onBackPressed();                                                        //Execute functionality of back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Update text with connection state
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
          //      mConnectionState.setText("Connected");                                   //Update text to say "Connected" or "Disconnected"

            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Update text roll of die and send over Bluetooth
    private void updateDieState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (mConnected) {

                }

            }
        });
    }


    private void findMldpGattService(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {                                                     //Verify that list of GATT services is valid
            Log.d(TAG, "findMldpGattService found no Services");
            return;
        }
        String uuid;                                                                    //String to compare received UUID with desired known UUIDs
        mDataMDLP = null;                                                               //Searching for CRC_checkArray characteristic, start with null value

        for (BluetoothGattService gattService : gattServices) {                         //Test each service in the list of services
            uuid = gattService.getUuid().toString();                                    //Get the string version of the service's UUID
            if (uuid.equals(MLDP_PRIVATE_SERVICE)) {                                    //See if it matches the UUID of the MLDP service
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics(); //If so then get the service's list of characteristics
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) { //Test each characteristic in the list of characteristics
                    uuid = gattCharacteristic.getUuid().toString();                     //Get the string version of the characteristic's UUID
                    if (uuid.equals(MLDP_DATA_PRIVATE_CHAR)) {                          //See if it matches the UUID of the MLDP data characteristic
                        mDataMDLP = gattCharacteristic;                                 //If so then save the reference to the characteristic
                        Log.d(TAG, "Found MLDP data characteristics");
                    }
                    else if (uuid.equals(MLDP_CONTROL_PRIVATE_CHAR)) {                  //See if UUID matches the UUID of the MLDP control characteristic
                        BluetoothGattCharacteristic mControlMLDP = gattCharacteristic;
                        Log.d(TAG, "Found MLDP control characteristics");
                    }
                    final int characteristicProperties = gattCharacteristic.getProperties(); //Get the properties of the characteristic
                    if ((characteristicProperties & (BluetoothGattCharacteristic.PROPERTY_NOTIFY)) > 0) { //See if the characteristic has the Notify property
                        mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true); //If so then enable notification in the BluetoothGatt
                        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(UUID.fromString(CHARACTERISTIC_NOTIFICATION_CONFIG)); //Get the descripter that enables notification on the server
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE); //Set the value of the descriptor to enable notification
                        mBluetoothGatt.writeDescriptor(descriptor);                     //Write the descriptor
                    }
                    if ((characteristicProperties & (BluetoothGattCharacteristic.PROPERTY_INDICATE)) > 0) { //See if the characteristic has the Indicate property
                        mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true); //If so then enable notification (and indication) in the BluetoothGatt
                        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(UUID.fromString(CHARACTERISTIC_NOTIFICATION_CONFIG)); //Get the descripter that enables indication on the server
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE); //Set the value of the descriptor to enable indication
                        mBluetoothGatt.writeDescriptor(descriptor);                     //Write the descriptor
                    }
                    if ((characteristicProperties & (BluetoothGattCharacteristic.PROPERTY_WRITE)) > 0) { //See if the characteristic has the Write (acknowledged) property
                        gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT); //If so then set the write type (write with acknowledge) in the BluetoothGatt
                    }
                    if ((characteristicProperties & (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) { //See if the characteristic has the Write (unacknowledged) property
                        gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE); //If so then set the write type (write with no acknowledge) in the BluetoothGatt
                    }
                }
                break;                                                                  //Found the MLDP service and are not looking for any other services
            }
        }
        if (mDataMDLP == null) {                                                        //See if the MLDP data characteristic was not found
            Toast.makeText(this, R.string.mldp_not_supported, Toast.LENGTH_SHORT).show(); //If so then show an error message
            Log.d(TAG, "findMldpGattService found no MLDP service");
            finish();                                                                   //and end the activity
        }
        mHandler.postDelayed(new Runnable() {                                           //Create delayed runnable that will send CRC_checkArray roll of the die after CRC_checkArray delay
            @Override
            public void run() {
                updateDieState();                                                       //Update the state of the die with CRC_checkArray new roll and send over BLE
            }
        }, 500);                                                                        //Do it after 200ms delay to give the RN4020 time to configure the characteristic

    }

    // ----------------------------------------------------------------------------------------------------------------
    // Implements callback methods for GATT events that the app cares about.  For example: connection change and services discovered.
    // When onConnectionStateChange() is called with newState = STATE_CONNECTED then it calls mBluetoothGatt.discoverServices()
    // resulting in another callback to onServicesDiscovered()
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) { //Change in connection state
            if (newState == BluetoothProfile.STATE_CONNECTED) {                         //See if we are connected
                Log.i(TAG, "Connected to GATT server.");
                mConnected = true;                                                      //Record the new connection state
                updateConnectionState(R.string.connected);                              //Update the display to say "Connected"
                invalidateOptionsMenu();                                                //Force the Options menu to be regenerated to show the disconnect option
                mBluetoothGatt.discoverServices();
                vibe.vibrate(100);   // Attempt to discover services after successful connection.
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {                 //See if we are not connected
                Log.i(TAG, "Disconnected from GATT server.");
                mConnected = false;                                                     //Record the new connection state
                updateConnectionState(R.string.disconnected);                           //Update the display to say "Disconnected"
                invalidateOptionsMenu();
                vibe.vibrate(100); //Force the Options menu to be regenerated to show the connect option
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {              //Service discovery complete
            if (status == BluetoothGatt.GATT_SUCCESS && mBluetoothGatt != null) {       //See if the service discovery was successful
                findMldpGattService(mBluetoothGatt.getServices());                      //Get the list of services and call method to look for MLDP service
            }
            else {                                                                      //Service discovery was not successful
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        //For information only. This application uses Indication to receive updated characteristic data, not Read
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) { //A request to Read has completed
            if (status == BluetoothGatt.GATT_SUCCESS) {                                 //See if the read was successful
//                String dataValue = characteristic.getStringValue(0);                        //Get the value of the characteristic
//                incomingMessage = incomingMessage.concat(dataValue);
                byte[] data=characteristic.getValue();

            }
        }

        //For information only. This application sends small packets infrequently and does not need to know what the previous write completed
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) { //A request to Write has completed
            if (status == BluetoothGatt.GATT_SUCCESS) {                                 //See if the write was successful
                boolean writeComplete = true;
            }
        }






        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        { //Indication or notification was received
            Received_bytes.add(characteristic.getValue()[0]);                   //Receiving Values From BLE Device
            Activity act1 = DeviceControlActivity.this;              //ASYNC Task Thread Creation
            act1.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                        for (counter = 0; counter < (Received_bytes.size()); counter++)
                        {
                            Received_array[counter] = Received_bytes.get(counter) & 0xFF;
                            if (counter == 3) {
                                if (Received_array[3] == 0) {
                                    rb.setChecked(false);
                                } else {
                                    rb.setChecked(true);
                                }
                            }
                        }
                        if (Received_bytes.size() == 9)
                        {
                            for (counter = 0; counter < (Received_bytes.size() - 2); counter++)
                            {
                                CRC_checkArray[counter] = Received_bytes.get(counter + 2) & 0xFF;
                            }
                            Received_bytes.clear();
                            counter=0;
                            CRC_Return = CRC_check(CRC_checkArray, 7);
                            if (CRC_Return == 1)
                            {
                                speedView1.speedTo(CRC_checkArray[counter + 2]);
                                speedView1.addNote(new TextNote(DeviceControlActivity.this,"Pressure "+ CRC_checkArray[counter+2]));
                                speedView2.speedTo(CRC_checkArray[counter + 3]);
                                speedView2.addNote(new TextNote(DeviceControlActivity.this,"Frequency "+ CRC_checkArray[counter+3]));
                                speedView1.setWithTremble(false);
                                speedView2.setWithTremble(false);
                                Received_bytes.clear();
                            }
                            else
                            {
                                rb.setChecked(false);
                                Toast.makeText(DeviceControlActivity.this, "NOT VALID FRAME", Toast.LENGTH_SHORT).show();
                                Received_bytes.clear();
                            }
                            if(Received_array[3]==0 && flag==0)
                            {
                                button.setBackgroundResource(R.drawable.off);
                            }
                            else if(Received_array[3]==1 && flag==0)
                            {
                                button.setBackgroundResource(R.drawable.on);
                            }
                        }
                }

            });
        }
    };


    // ----------------------------------------------------------------------------------------------------------------
    // Request CRC_checkArray read of CRC_checkArray given BluetoothGattCharacteristic. The Read result is reported asynchronously through the
    // BluetoothGattCallback onCharacteristicRead callback method.
    // For information only. This application uses Indication to receive updated characteristic data, not Read

    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {                      //Check that we have access to CRC_checkArray Bluetooth radio
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);                              //Request the BluetoothGatt to Read the characteristic
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Write to CRC_checkArray given characteristic. The completion of the write is reported asynchronously through the
    // BluetoothGattCallback onCharacteristicWrire callback method.
    private void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {                      //Check that we have access to CRC_checkArray Bluetooth radio
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        int test = characteristic.getProperties();                                      //Get the properties of the characteristic
        if ((test & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0 && (test & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) { //Check that the property is writable 
            return;
        }

        if (mBluetoothGatt.writeCharacteristic(characteristic)) {                       //Request the BluetoothGatt to do the Write
            Log.d(TAG, "writeCharacteristic successful");                               //The request was accepted, this does not mean the write completed
            Log.d("Writing :",characteristic.toString());


        }
        else {
            Log.d(TAG, "writeCharacteristic failed");

        }

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
//        viewPager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onBackPressed() {
        if(mBluetoothGatt != null) {                                            //If there is CRC_checkArray valid GATT connection
            mBluetoothGatt.disconnect();                                        // then disconnect
        }
        mBluetoothGatt.close();                                                         //Close the connection
        Intent j=new Intent(getApplicationContext(), DeviceScanActivity.class);
        j.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(j);
        super.onBackPressed();
        this.finish();
    }


    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.button1:
            {
                 if (Received_array !=null) {
                    if (Received_array[3] == 0) {

                        rb.setChecked(false);
                    } else {

                        rb.setChecked(true);
                    }
                    int[] tempwritevalues = new int[7];
                    int tempCRC;
                    byte writeindex = 0;
                    tempwritevalues[writeindex++] = 1;//to write this byte should be 0x01;
                    tempwritevalues[writeindex++] = Received_array[3];//pump status -> read only
                    tempwritevalues[writeindex++] = Received_array[4];//pressure -> read only
                    tempwritevalues[writeindex++] = Received_array[5];//frequency -> read only
                    tempwritevalues[writeindex++] = progress;  //pressure set point
                    tempCRC = (CRC16(tempwritevalues, writeindex)) & 0xFFFF;
                    tempwritevalues[writeindex++] = tempCRC & 0xFF;
                    tempwritevalues[writeindex++] = (tempCRC >> 8) & 0xFF;
                    byte[] array = new byte[10];
                    array[0] = -1;// sync byte 0xFF or -1 or 255
                    array[1] = writeindex;
                    for (int i = 0; i < writeindex; i++) {
                        array[i + 2] = (byte) (tempwritevalues[i] & 0xFF);
                    }
                        mDataMDLP.setValue(array);
                        writeCharacteristic(mDataMDLP);
                        Toast.makeText(DeviceControlActivity.this, "UPDATED", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    rb.setChecked(false);
                    Toast.makeText(DeviceControlActivity.this, "RECEIVE DATA FIRST", Toast.LENGTH_LONG).show();
                }
                    break;
             }
            case R.id.button2:
           {
                    if(isPressed)
                    {
                        button.setBackgroundResource(R.drawable.on);
                        Received_array[3]=1;
                        flag=1;
                    }
                    else
                    {
                        button.setBackgroundResource(R.drawable.off);
                        Received_array[3]=0;
                        flag=1;
                    }
                   isPressed = !isPressed;
               break;
           }
            default: break;
        }
    }





    int CRC16( int puchMsg[],int usDataLen )
    {
        int CRC;
        int uchCRCHi = 0xFF ; /* high byte of CRC initialized */
        int uchCRCLo = 0xFF ; /* low byte of CRC initialized */
        int uIndex ; /* will index into CRC lookup table */
        int index = 0;
        while(usDataLen!=0)
        { /* pass through message buffer */
            usDataLen--;
            uIndex = uchCRCLo ^ puchMsg[index++] ; /* calculate the CRC */
            uchCRCLo = uchCRCHi ^ auchCRCHi[uIndex] ;
            uchCRCHi = auchCRCLo[uIndex] ;
        }
        CRC=(uchCRCHi << 8 )| uchCRCLo;
        return (CRC) ;//(highbyte | lowbyte)
    }


//********************************************************************
// PARAMETERS:
// RX_BUFFER
// pointer to CRC_checkArray string.
//
// buf_len
// Length of the given string.
//
// RETURN VALUE:
// unsigned int
// CRC of string
//
// DESCRIPTION:
// Calculate the CRC of CRC_checkArray string (Last two bytes of string).
// NOTE: char type message only.
//********************************************************************

     int get_frame_CRC(int RX_BUFFER[],int buf_len)
    {
         int HighByte,LowByte , CRC1;
        HighByte = RX_BUFFER[buf_len-1];//high byte
        LowByte = RX_BUFFER[buf_len-2];//low byte
        CRC1=(HighByte << 8) | LowByte;
        return (CRC1);//(highbyte | lowbyte)
    }

//********************************************************************
// PARAMETERS:
// RX_BUFFER
// pointer to CRC_checkArray string.
//
// buf_len
// Length of the given string.
//
// RETURN VALUE:
// unsigned int
// result of CRC check(matched or not matched).
//
// DESCRIPTION:
// It checks for CRC match.
// NOTE: char type message only.
//********************************************************************

     int CRC_check(int  RX_BUFFER[],int buf_len)
    {
        int flag_crc_match;
        if( get_frame_CRC(RX_BUFFER,buf_len)== CRC16( RX_BUFFER,(buf_len-2) ) )
        {
            flag_crc_match = 1;
        }
        else
        {
            flag_crc_match = 0;
        }
        return flag_crc_match;
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        progress=value;
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }
}
