package pt.tecnico.contacttracing;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import pt.tecnico.contacttracing.ble.Advertiser;
import pt.tecnico.contacttracing.ble.Constants;
import pt.tecnico.contacttracing.ble.Scanner;
import pt.tecnico.contacttracing.location.LocationTrack;
import pt.tecnico.contacttracing.model.NumberKey;
import pt.tecnico.contacttracing.model.ReceivedNumber;
import pt.tecnico.contacttracing.model.SignedBatch;
import pt.tecnico.contacttracing.webservice.ApiInterface;
import pt.tecnico.contacttracing.webservice.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Database
    SQLiteDatabase database;

    // Signed by health authority
    static SignedBatch signed = null;

    static Instant lastUpdate = null;

    private Long _lastGenerated = null;

    private String SERVER_URL = "https://192.168.1.125:8888/";
    private String HEALTH_URL = "https://192.168.1.125:9999/";

    private LocationTrack _location;

    private boolean _Scanning = false;
    private boolean _Advertising = false;

    private Scanner _bleScanner;
    private Advertiser _bleAdvertiser;
    private Handler _Handler;

    private Button _ScanButton;
    private Button _AdvertiseButton;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Contact Tracing");

        _ScanButton = (Button) findViewById(R.id.scan_btn);
        _AdvertiseButton = (Button) findViewById(R.id.advertise_btn);

        _ScanButton.setOnClickListener(this);
        _AdvertiseButton.setOnClickListener(this);
        resultText = (TextView) findViewById(R.id.result_text);
        resultText.setMovementMethod(new ScrollingMovementMethod());

        //if (savedInstanceState == null) {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

            checkLocationPermission();
            checkBluetoothSupport(btAdapter);

            _bleScanner = new Scanner(this, btAdapter);
            _bleAdvertiser = new Advertiser(this, btAdapter);
        //}

        _Handler = new Handler();

        // Generate new number and MAC address every 5 minutes
        Timer timer = new Timer();
        timer.schedule(new GenerateNumber(), 0, 1000 * 30);

        init_database();
    }


    /* ====================================================================== */
    /* ====[                         DATABASE                           ]==== */
    /* ====================================================================== */

    private void init_database() {
        database = openOrCreateDatabase("database", MODE_PRIVATE,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS GeneratedNumbers(Number INTEGER PRIMARY KEY, PrivateKey VARCHAR);");
        database.execSQL("CREATE TABLE IF NOT EXISTS ReceivedNumbers(Number INTEGER PRIMARY KEY, FirstSeconds INT, FirstNanos INT, LastSeconds INT, LastNanos INT, Location VARCHAR);");
    }

    private void add_generated(long number, String key) {
        database.execSQL("INSERT OR IGNORE INTO GeneratedNumbers VALUES('" + number + "','" + key + "')");
    }

    private void add_received(long number, Instant timestamp, Location current_location) {
        Cursor cursor = database.rawQuery("Select * from ReceivedNumbers where Number='"+ number + "';",null);
        long seconds = timestamp.getEpochSecond();
        long nanos = timestamp.getNano();

        if (cursor.getCount() == 0) {
            String location = current_location.toString();
            database.execSQL("INSERT INTO ReceivedNumbers VALUES('" + number + "','" + seconds + "','" + nanos + "','" + seconds + "','" + nanos + "','" + location + "');");
        }
        else {
            database.execSQL("UPDATE ReceivedNumbers SET LastSeconds='" + seconds + "', LastNanos='" + nanos + "' WHERE number='" + number + "';");
        }

    }

    private List<NumberKey> get_generated() {
        Cursor cursor = database.rawQuery("Select * from GeneratedNumbers",null);
        List<NumberKey> generated = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long number = cursor.getLong(0);
            String key = cursor.getString(1);
            NumberKey nk = new NumberKey(key, number);
            generated.add(nk);
            cursor.moveToNext();
        }
        return generated;
    }

    private List<ReceivedNumber> get_received() {
        Cursor cursor = database.rawQuery("Select * from ReceivedNumbers",null);
        List<ReceivedNumber> received = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long number = cursor.getLong(0);
            int firstSeconds = cursor.getInt(1);
            int firstNanos = cursor.getInt(2);
            int lastSeconds = cursor.getInt(3);
            int lastNanos = cursor.getInt(4);
            String location = cursor.getString(5);
            Instant firstInstant = Instant.ofEpochSecond(firstSeconds, firstNanos);
            Instant lastInstant = Instant.ofEpochSecond(lastSeconds, lastNanos);
            ReceivedNumber rn = new ReceivedNumber(number, firstInstant, lastInstant, location);
            received.add(rn);
            cursor.moveToNext();
        }
        return received;
    }

    private void delete_from_generated(long number) {
        database.execSQL("DELETE FROM GeneratedNumbers WHERE number='" + number + "';");
    }


    /* ====================================================================== */
    /* ====[                          SERVER                            ]==== */
    /* ====================================================================== */

    public void sendInfected(View view) throws JSONException {
        if (signed == null) {
            Toast.makeText(this, "Please ask health authority for a signature before sending the numbers.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class, SERVER_URL);
        JSONObject json = new JSONObject();
        json.put("data", signed);

        System.out.println(json);

        Call<Void> call = apiInterface.sendInfected(json);


        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                resultText.setText(R.string.send_numbers_ok);

                // Delete numbers that were already sent to the server
                List<NumberKey> numberKeys = signed.getNk_array();
                for (NumberKey nk : numberKeys)
                    delete_from_generated(nk.getNumber());

                signed = null; // signed batch sent, no longer valid

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultText.setText(R.string.send_numbers_nok);
                t.printStackTrace();
            }
        });
    }

    public void getInfected(View view) throws JSONException{
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class, SERVER_URL);

        long seconds = lastUpdate != null ? lastUpdate.getEpochSecond() : 0;
        long nanos = lastUpdate != null ? lastUpdate.getNano() : 0;

        JSONObject json = new JSONObject();
        json.put("lastUpdateSeconds", seconds);
        json.put("lastUpdateNanos", nanos);

        Call<Object> call = apiInterface.getInfected(json);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                Object o = response.body();
                try {
                    JSONObject jsonObj = new JSONObject(String.valueOf(o));
                    JSONArray pairs = (JSONArray) jsonObj.get("data");

                    lastUpdate = Instant.now();

                    if (pairs.length() == 0) {
                        resultText.setText(R.string.no_new_numbers);
                        return;
                    }

                    List<NumberKey> received = new ArrayList<>();

                    String text = "New numbers received:\n";
                    for (int i = 0; i < pairs.length(); i++) {
                        JSONObject pair = pairs.getJSONObject(i);
                        String key = pair.getString("Key"); // base64 public key
                        long number = pair.getLong("Number");
                        received.add(new NumberKey(key, number));
                        text += "Number " + number + "\n";
                    }

                    resultText.setText(text);
                    System.out.println(response.body());

                    /* ------------ CHECK IF I WAS IN CONTACT WITH INFECTED -------------- */

                    List<ReceivedNumber> saved = get_received(); // Numbers saved from other users

                    List<ReceivedNumber> infected = new ArrayList<>();
                    for (ReceivedNumber savedNumber : saved) {
                        if (received.contains(savedNumber)) {
                            infected.add(savedNumber);
                        }
                        else
                            Log.e(TAG, String.valueOf(savedNumber));
                    }
                    if (!infected.isEmpty()) {
                        String logText = "> Found contact with infected person:\n\n";
                        for (ReceivedNumber infectedNumber : infected) {
                            logText += infectedNumber.toString() + "\n";
                        }
                        Log.i(TAG, logText);
                        resultText.setText(logText);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                resultText.setText(R.string.receive_numbers_nok);
                t.printStackTrace();
            }
        });


    }

    public void getSignature(View view) throws NoSuchAlgorithmException {
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class, HEALTH_URL);

        List<NumberKey> generated = get_generated();
        Long numbers = 0L;
        for (NumberKey nk : generated){
            numbers += nk.getNumber() % 1000000; // 1 million
        }

        String strNumbers = numbers.toString();
        System.out.println(numbers.toString());
        System.out.println(numbers.toString().length());

        System.out.println(numbers.toString().getBytes(StandardCharsets.UTF_8));

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(numbers.toString().getBytes(StandardCharsets.UTF_8));
        String base64hash = Base64.getEncoder().encodeToString(hash);

        Call<String> call = apiInterface.getSignature(base64hash);
        System.out.println(base64hash);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String signature = String.valueOf(response.body());
                resultText.setText(R.string.signature_ok);
                System.out.println(signature);

                // Create new signed batch
                SignedBatch b = new SignedBatch(generated, generated.size(), signature);
                signed = b;
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                resultText.setText(R.string.signature_ok);
                t.printStackTrace();
            }
        });


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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, Constants.REQUEST_LOCATION);
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



    public void scan(View v) {
        _ScanButton.setEnabled(false);

        _bleScanner.startScanning();

        String toastText = "Scanning for " + TimeUnit.SECONDS.convert(Constants.SCAN_PERIOD, TimeUnit.MILLISECONDS) + " seconds.";
        Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();

        // Will stop the scanning after a set time.
        _Handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                _bleScanner.stopScanning();
                _ScanButton.setEnabled(true);
            }
        }, Constants.SCAN_PERIOD);
    }

    public void advertise(View v) {
        _AdvertiseButton.setEnabled(false);

        String number_str = String.valueOf( _lastGenerated );
        byte[] ts = getCurrentTimeInBytes();
        _bleAdvertiser.startAdvertising(number_str.getBytes(StandardCharsets.UTF_8), ts, null);

        String toastText = "Advertising for " + TimeUnit.SECONDS.convert(Constants.ADVERTISE_PERIOD, TimeUnit.MILLISECONDS) + " seconds.";
        Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();

        // Will stop the advertising after a set time.
        _Handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                _bleAdvertiser.stopAdvertising();
                _AdvertiseButton.setEnabled(true);
            }
        }, Constants.ADVERTISE_PERIOD);
    }

    @Override
    public void onClick(View v) {
        if( v.getId() == R.id.scan_btn ) {
            scan(v);
        }
        else if( v.getId() == R.id.advertise_btn ) {
            advertise(v);
        }
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

    class GenerateNumber extends TimerTask {
        public void run() {
            Random rnd = new Random();
            long n = (long) (1000000000000000L + rnd.nextFloat() * 9000000000000000L);
            KeyPair key = generateKeyPair();

            byte[] encodedPublicKey = key.getPublic().getEncoded();
            String b64PublicKey = Base64.getEncoder().encodeToString(encodedPublicKey);
            String b64PublicKey_noSlashes = b64PublicKey.replace("/", "-"); // Replace slashes or it all goes to hell
            add_generated(n, b64PublicKey_noSlashes);
            _lastGenerated = n;

            if (_Advertising) { //FIXME
                // Restart Advertise with new identifier.
                _bleAdvertiser.stopAdvertising();
                byte[] ts = getCurrentTimeInBytes();
                _bleAdvertiser.startAdvertising(String.valueOf(n).getBytes(StandardCharsets.UTF_8), ts, null);
            }

        }
    }

    public KeyPair generateKeyPair() {
        try {

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair keyPair = keyGen.generateKeyPair();

            return keyPair;

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public byte[] getCurrentTimeInBytes() {
        int dateInSec = (int) (System.currentTimeMillis() / 1000);
        return ByteBuffer.allocate(4).putInt(dateInSec).array();
    }

    public void storeReceivedNumber(long number, Instant received_ts) {
        Instant current_ts = Instant.now();
        long current_seconds = current_ts.getEpochSecond();
        long received_seconds = received_ts.getEpochSecond();
        long advertise_seconds = Constants.ADVERTISE_PERIOD / 1000;
        if ( Math.abs(current_seconds - received_seconds) > advertise_seconds + 1 ) { // 1 sec fresh
            resultText.setText(R.string.number_not_fresh);
            Log.e(TAG, "NOT FRESH!");
            return;
        }

        _location = new LocationTrack(this);
        Location current_location = _location.getLocation();

        add_received(number, current_ts, current_location);
    }

} // class MainActivity
