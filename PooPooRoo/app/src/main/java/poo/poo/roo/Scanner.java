package poo.poo.roo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * Scans for Bluetooth Low Energy Advertisements matching a filter and displays them to the user.
 */
public class Scanner {

    private static final String TAG = Scanner.class.getSimpleName();

    private final Context _context;
    private final BluetoothLeScanner _BluetoothLeScanner;
    private ScanCallback _ScanCallback;

    public Scanner(Context context, BluetoothAdapter btAdapter) {
        _context = context;
        _BluetoothLeScanner = btAdapter.getBluetoothLeScanner();
    }

    /**
     * Start scanning for BLE Advertisements.
     */
    public void startScanning() {
        Log.d(TAG, "Starting Scanning");

        // Kick off a new scan.
        _ScanCallback = new SampleScanCallback();
        _BluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), _ScanCallback);
    }

    /**
     * Stop scanning for BLE Advertisements.
     */
    public void stopScanning() {
        Log.d(TAG, "Stopping Scanning");

        // Stop the scan, wipe the callback.
        _BluetoothLeScanner.stopScan(_ScanCallback);
        _ScanCallback = null;
    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
        //builder.setServiceUuid(Constants.Service_UUID);
        scanFilters.add(builder.build());

        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L);
        return builder.build();
    }

    private class SampleScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //super.onScanResult(callbackType, result);

            String name = result.getDevice().getName();
            String address = result.getDevice().getAddress();
            String msg = ( ( name != null ) ? name : address );
            Log.i(TAG, msg);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(_context, "Scan failed with error: " + errorCode, Toast.LENGTH_LONG).show();
        }

    } // class SampleScannerCallback

} // class Scanner
