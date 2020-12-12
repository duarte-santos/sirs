package pt.tecnico.contacttracing.ble;

import android.os.ParcelUuid;


/**
 * Constants for use in the Bluetooth Advertisements sample
 */
public class Constants {

    /**
     * UUID identified with this app - set as Service UUID for BLE Advertisements.
     *
     * Bluetooth requires a certain format for UUIDs associated with Services.
     * The official specification can be found here:
     * {@link https://www.bluetooth.org/en-us/specification/assigned-numbers/service-discovery}
     */
    public static final ParcelUuid AdvertiseData_Service_UUID = ParcelUuid.fromString("0000b81d-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid  ScanResponse_Service_UUID = ParcelUuid.fromString("0000b81e-0000-1000-8000-00805f9b34fb");

    public static final int ADVERTISEMENT_MAX_BYTES = 23;

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_LOCATION = 2;

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;

    public static final long ROTATION_PERIOD = 1 * MINUTE;
    public static final long SCAN_PERIOD = 15 * SECOND;
    public static final long SCAN_INTERVAL = 5 * SECOND;
    public static final long ADVERTISE_PERIOD = 1 * SECOND;
    public static final long ADVERTISE_INTERVAL = 5 * SECOND;

    public static final int LOCATION_REFRESH_TIME = 15 * 60 * 1000; // 15 min
    public static final int LOCATION_REFRESH_DISTANCE = 1000; // 1km

}
