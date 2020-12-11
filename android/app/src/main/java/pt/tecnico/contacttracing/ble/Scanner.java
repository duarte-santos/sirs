package pt.tecnico.contacttracing.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import pt.tecnico.contacttracing.MainActivity;


/**
 * Scans for Bluetooth Low Energy Advertisements matching a filter and displays them to the user.
 */
public class Scanner {

    private static final String TAG = Scanner.class.getSimpleName();

    private final Context _context;
    private final BluetoothLeScanner _bleScanner;
    private ScanCallback _bleScanCallback;

    public Scanner(Context context, BluetoothAdapter btAdapter) {
        _context = context;
        _bleScanner = btAdapter.getBluetoothLeScanner();
    }

    /**
     * Start scanning for BLE Advertisements.
     */
    public void startScanning() {
        Log.d(TAG, "Starting Scanning");

        // Kick off a new scan.
        _bleScanCallback = new SampleScanCallback();
        _bleScanner.startScan(buildScanFilters(), buildScanSettings(), _bleScanCallback);
    }

    /**
     * Stop scanning for BLE Advertisements.
     */
    public void stopScanning() {
        Log.d(TAG, "Stopping Scanning");

        // Stop the scan, wipe the callback.
        _bleScanner.stopScan(_bleScanCallback);
        _bleScanCallback = null;
    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
        builder.setServiceUuid(Constants.AdvertiseData_Service_UUID);
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

        public Instant bytesToInstant(byte[] bytes) {
            int dateInSec = ByteBuffer.wrap(bytes).getInt();
            return Instant.ofEpochSecond(dateInSec);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if( result == null || result.getDevice() == null /*|| TextUtils.isEmpty(result.getDevice().getName())*/ )
                return;

            String name = result.getDevice().getName();
            String address = result.getDevice().getAddress();
            String msg = ( ( name != null ) ? name : address );

            String advertiseData = new String(result.getScanRecord().getServiceData(Constants.AdvertiseData_Service_UUID), StandardCharsets.UTF_8);
            String  scanResponse = new String(result.getScanRecord().getServiceData( Constants.ScanResponse_Service_UUID), StandardCharsets.UTF_8);

            Instant instant = bytesToInstant(scanResponse.getBytes(StandardCharsets.UTF_8));

            Log.i(TAG, msg + " sent \"" + advertiseData + "\" + \"" + instant + "\"" );

            MainActivity activity = (MainActivity) _context;
            activity.storeReceivedNumber(Integer.parseInt(advertiseData), instant);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(_context, "Scan failed with error: " + errorCode, Toast.LENGTH_LONG).show();
        }

    } // class SampleScannerCallback

} // class Scanner
