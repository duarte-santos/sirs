package pt.tecnico.contacttracing.location;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import pt.tecnico.contacttracing.R;

public class LocationTrack extends Service implements LocationListener {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60;

    private static final String TAG = LocationTrack.class.getSimpleName();

    private final Context _context;

    boolean _checkGPS = false;
    boolean _checkNetwork = false;
    boolean _canGetLocation = false;

    Location _location;
    double _latitude;
    double _longitude;

    protected LocationManager _locationManager;

    public LocationTrack(Context context) {
        _context = context;
    }

    public Location getLocation() {

        try {
            _locationManager = (LocationManager) _context.getSystemService(LOCATION_SERVICE);

            // get GPS status
            _checkGPS = _locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // get network provider status
            _checkNetwork = _locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!_checkGPS && !_checkNetwork) {
                Toast.makeText(_context, "No Service Provider is available", Toast.LENGTH_SHORT).show();
            } else {
                this._canGetLocation = true;

                // if GPS Enabled get lat/long using GPS Services
                if (_checkGPS) {

                    if (ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED /*&& ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {

                    }
                    _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (_locationManager != null) {
                        _location = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (_location != null) {
                            _latitude = _location.getLatitude();
                            _longitude = _location.getLongitude();
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, String.valueOf(R.string.location_not_available));
        }
        return _location;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) { }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }
}
