package pt.tecnico.contacttracing;

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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import pt.tecnico.contacttracing.model.NumberKey;
import pt.tecnico.contacttracing.webservice.ApiInterface;
import pt.tecnico.contacttracing.webservice.ServiceGenerator;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Received NumberKeys
    static List<NumberKey> received = new ArrayList<>();

    // Generated
    static List<NumberKey> generated = new ArrayList<>();

    private String SERVER_URL = "https://10.0.2.2:8888/";
    private String HEALTH_URL = "https://10.0.2.2:9999/";
    //private String SERVER_URL = "https://localhost:8888/";
    //private String HEALTH_URL = "https://127.0.0.1:9999/"; FIXME run on physical

    private boolean _Scanning = false;
    private boolean _Advertising = false;

    private Scanner _bleScanner;
    private Advertiser _bleAdvertiser;

    private Button _ScanButton;
    private Button _AdvertiseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Contact Tracing");

        _ScanButton = (Button) findViewById(R.id.scan_btn);
        _AdvertiseButton = (Button) findViewById(R.id.advertise_btn);

        _ScanButton.setOnClickListener(this);
        _AdvertiseButton.setOnClickListener(this);

        /*
        if (savedInstanceState == null) {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

            checkLocationPermission();
            checkBluetoothSupport(btAdapter);

            _bleScanner = new Scanner(this, btAdapter);
            _bleAdvertiser = new Advertiser(this, btAdapter);
        }*/

        // Generate new number and MAC address every 5 minutes
        Timer timer = new Timer();
        timer.schedule(new GenerateNumber(), 0, 1000 * 10);

    }


    /* ====================================================================== */
    /* ====[                          SERVER                            ]==== */
    /* ====================================================================== */

    private void getHello(){
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class, SERVER_URL);
        Call<String> call = apiInterface.getHello();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                System.out.println("I received it!");
                System.out.println(response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                System.out.println("I did not received it :(");
                t.printStackTrace();
            }
        });
    }

    public void sendInfected(View view) throws JSONException {
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class, SERVER_URL);
        JSONObject json = new JSONObject();
        json.put("data", generated);

        System.out.println(json);

        Call<Void> call = apiInterface.sendInfected(json);


        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                System.out.println("I received it!");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                System.out.println("I did not received it :(");
                t.printStackTrace();
            }
        });
    }

     public void getInfected(View view){
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class, SERVER_URL);
        Call<Object> call = apiInterface.getInfected();

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                Object o = response.body();
                try {
                    JSONObject jsonObj = new JSONObject(String.valueOf(o));
                    JSONArray pairs = (JSONArray) jsonObj.get("data");

                    for (int i = 0; i < pairs.length(); i++) {
                        JSONObject pair = pairs.getJSONObject(i);
                        String key = pair.getString("Key"); // base64 public key
                        int number = pair.getInt("Number");
                        NumberKey nk = new NumberKey(key, number);
                        received.add(nk);
                    }

                    System.out.println("I received it!");
                    System.out.println(response.body());
                    System.out.println(received.get(0));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                System.out.println("I did not received it :(");
                t.printStackTrace();
            }
        });


    }

    public void getSignature(View view){
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class, HEALTH_URL);

        JSONArray numbers = new JSONArray();
        for (NumberKey n : generated) {
            numbers.put(n.getNumber());
        }

        Call<Object> call = apiInterface.getSignature(numbers);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                Object o = response.body();

                System.out.println("I received it!");
                System.out.println(response.body());
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                System.out.println("I did not received it :(");
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

    public void scan(View v){
        if (_Scanning) {
            _Scanning = false;
            _bleScanner.stopScanning();
            _ScanButton.setText(R.string.bt_start_scan);
        } else {
            _Scanning = true;
            _bleScanner.startScanning();
            _ScanButton.setText(R.string.bt_stop_scan);
        }
    }

    public void advertise(View v){
        if (_Advertising) {
            _Advertising = false;
            _bleAdvertiser.stopAdvertising();
            _AdvertiseButton.setText(R.string.bt_start_advertise);
        } else {
            _Advertising = true;
            String number_str = String.valueOf( generated.get(generated.size() - 1).getNumber() );
            _bleAdvertiser.startAdvertising(number_str);
            _AdvertiseButton.setText(R.string.bt_stop_advertise);
        }
    }

    @Override
    public void onClick(View v) {
        if( v.getId() == R.id.scan_btn) {
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
            int n = 10000000 + rnd.nextInt(90000000); //FIXME size of numbers
            KeyPair key = generateKeyPair();

            byte[] encodedPublicKey = key.getPublic().getEncoded();
            String b64PublicKey = Base64.getEncoder().encodeToString(encodedPublicKey);
            String b64PublicKey_noSlashes = b64PublicKey.replace("/", "-"); // Replace slashes or it all goes to hell

            NumberKey nk = new NumberKey(b64PublicKey_noSlashes, n);
            generated.add(nk);

            if (_Advertising) {
                // Restart Advertise with new identifier.
                _bleAdvertiser.stopAdvertising();
                _bleAdvertiser.startAdvertising(String.valueOf(n));
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

} // class MainActivity
