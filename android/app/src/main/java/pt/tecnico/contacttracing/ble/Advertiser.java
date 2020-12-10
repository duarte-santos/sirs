package pt.tecnico.contacttracing.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;


/**
 * Allows user to start & stop Bluetooth LE Advertising of their device.
 */
public class Advertiser {

    private static final String TAG = Advertiser.class.getSimpleName();

    private final Context _context;
    private final BluetoothLeAdvertiser _bleAdvertiser;
    private AdvertiseCallback _bleAdvertiseCallback;

    public Advertiser(Context context, BluetoothAdapter btAdapter) {
        _context = context;
        _bleAdvertiser = btAdapter.getBluetoothLeAdvertiser();
    }

    /**
     * Starts BLE Advertising.
     */
    public void startAdvertising(byte[] identifier, byte[] timestamp, byte[] signature) {
        Log.d(TAG, "Starting Advertise");

        String advertiseData_data = "23bytes________________";
        String  scanResponse_data = "________________23bytes";

        // Kick off a new scan.
        _bleAdvertiseCallback = new Advertiser.SampleAdvertiseCallback();
        AdvertiseData advertiseData = buildAdvertiseData(identifier, Constants.AdvertiseData_Service_UUID);
        AdvertiseData  scanResponse = buildAdvertiseData(timestamp,  Constants.ScanResponse_Service_UUID);
        _bleAdvertiser.startAdvertising(buildAdvertiseSettings(), advertiseData, scanResponse, _bleAdvertiseCallback);
    }

    /**
     * Stops BLE Advertising.
     */
    public void stopAdvertising() {
        Log.d(TAG, "Stopping Advertise");

        // Stop the advertise, wipe the callback.
        _bleAdvertiser.stopAdvertising(_bleAdvertiseCallback);
        _bleAdvertiseCallback = null;
    }

    /**
     * Return a {@link AdvertiseData} object which includes the Service UUID.
     */
    private AdvertiseData buildAdvertiseData(byte[] data, ParcelUuid serviceUuid) {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addServiceUuid(serviceUuid)
                .addServiceData(serviceUuid, data);
        return builder.build();
    }

    /**
     * Return a {@link AdvertiseSettings} object set to use low power (to preserve battery life).
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        //FIXME : set timeout
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .setTimeout(0);
        return builder.build();
    }

    private class SampleAdvertiseCallback extends AdvertiseCallback {

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Toast.makeText(_context, "Advertise failed with error: " + errorCode, Toast.LENGTH_LONG).show();
            }

    } // class SampleAdvertiseCallback

} // class Advertiser
