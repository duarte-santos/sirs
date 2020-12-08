package poo.poo.roo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean _Scanning = false;
    private Scanner _bleScanner;

    private Button _ScanButton;
    //private Button _AdvertiseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Contact Tracing");

        _ScanButton = (Button) findViewById(R.id.scan_btn);
        //_AdvertiseButton = (Button) findViewById(R.id.advertise_btn);

        _ScanButton.setOnClickListener(this);
        //_AdvertiseButton.setOnClickListener(this);

        if (savedInstanceState == null) {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

            checkLocationPermission();
            checkBluetoothSupport(btAdapter);

            _bleScanner = new Scanner(this, btAdapter);
        }
    }


    /* ====================================================================== */
    /* ====[                        PERMISSIONS                         ]==== */
    /* ====================================================================== */

    private void checkBluetoothSupport(BluetoothAdapter btAdapter) {
        // Is Bluetooth supported on this device?
        if (btAdapter != null) {
            // Is Bluetooth turned on?
            if (btAdapter.isEnabled()) {
                // Are Bluetooth Advertisements supported on this device?
                if (btAdapter.isMultipleAdvertisementSupported()) {
                    // Everything is supported and enabled.

                } else {
                    // Bluetooth Advertisements are not supported.
                    AlertAndExit(R.string.bt_required, R.string.bt_ads_not_supported);
                }
            } else {
                // Prompt user to turn on Bluetooth.
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            }
        } else {
            // Bluetooth is not supported.
            AlertAndExit(R.string.bt_required, R.string.bt_not_supported);
        }
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            new AlertDialog.Builder(this)
                .setTitle(R.string.title_location_permission)
                .setMessage(R.string.text_location_permission)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION }, Constants.REQUEST_LOCATION);
                    }
                })
                .create()
                .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // permission denied, boo! Disable the functionality that depends on this permission.
                AlertAndExit(R.string.title_location_denied, R.string.title_location_denied);
            }
        }
    }


    /* ====================================================================== */
    /* ====[                      UI INTERACTION                        ]==== */
    /* ====================================================================== */

    public void scan(View v){
        if (_Scanning){
            _Scanning = false;
            _bleScanner.stopScanning();
            _ScanButton.setText(R.string.bt_scan);
        } else {
            _Scanning = true;
            _bleScanner.startScanning();
            _ScanButton.setText(R.string.bt_stop);
        }
    }

    @Override
    public void onClick(View v) {
        if( v.getId() == R.id.scan_btn) {
            scan(v);
        }
        //else if( v.getId() == R.id.advertise_btn ) {
        //    advertise();
        //}
    }


    /* ====================================================================== */
    /* ====[                        AUXILIARY                           ]==== */
    /* ====================================================================== */

    private void AlertAndExit(int title, int message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .create()
                .show();
    }

} // class MainActivity
