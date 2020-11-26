/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pt.tecnico.contacttracing;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.protobuf.Timestamp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.okhttp.OkHttpChannelBuilder;
import pt.tecnico.examples.contacttracing.*;
import pt.tecnico.examples.contacttracing.ContactTracingGrpc.*;

public class ContactTracingActivity extends AppCompatActivity {
  private ManagedChannel channel;
  private ManagedChannel channelHealth;

  static private String trustCertCollectionFilePath;

  private EditText hostEdit;
  private EditText portEdit;
  private EditText hostHealthEdit;
  private EditText portHealthEdit;
  private Button startContactTracingButton;
  private Button exitContactTracingButton;
  private Button registerInfectedButton;
  private Button getInfectedButton;
  private Button generateSignatureButton;
  private TextView resultText;

  // FIXME : Guardar persistentemente
  static List<Integer> numbers = new ArrayList<Integer>();
  static List<Integer> keys = new ArrayList<Integer>();
  static Instant lastUpdate;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contact);
    hostEdit = (EditText) findViewById(R.id.host_edit_text);
    portEdit = (EditText) findViewById(R.id.port_edit_text);
    hostHealthEdit = (EditText) findViewById(R.id.health_host_edit_text);
    portHealthEdit = (EditText) findViewById(R.id.health_port_edit_text);
    startContactTracingButton = (Button) findViewById(R.id.start_contact_tracing_button);
    exitContactTracingButton = (Button) findViewById(R.id.exit_contact_tracing_button);
    registerInfectedButton = (Button) findViewById(R.id.register_infected_button);
    getInfectedButton = (Button) findViewById(R.id.get_infected_button);
    generateSignatureButton = (Button) findViewById(R.id.generate_signature_button);
    disableButtons();
    startContactTracingButton.setEnabled(true);
    resultText = (TextView) findViewById(R.id.result_text);
    resultText.setMovementMethod(new ScrollingMovementMethod());
    lastUpdate = Instant.now();

    trustCertCollectionFilePath = "res/health.csr";

  }


  public void startContactTracing(View view) {
    connectServer();
    hostEdit.setEnabled(false);
    portEdit.setEnabled(false);
    enableButtons();
    startContactTracingButton.setEnabled(false);

    // Generate numbers every 5 minutes
    Timer timer = new Timer();
    timer.schedule(new GenerateNumber(), 0, 1000*60*5);

  }

  public void exitContactTracing(View view) {
    channel.shutdown();
    disableButtons();
    hostEdit.setEnabled(true);
    portEdit.setEnabled(true);
    startContactTracingButton.setEnabled(true);
  }

  /* --------- DOMAIN METHODS ---------- */

  public void registerInfected(View view) {
    setResultText("");
    disableButtons();
    new GrpcTask(new RegisterInfectedRunnable(), channel, this).execute();
  }

  public void getInfected(View view) {
    setResultText("");
    disableButtons();
    new GrpcTask(new GetInfectedRunnable(), channel, this).execute();
  }

  public void generateSignature(View view) {
    try {
      connectHealthAuthority();
    } catch (SSLException e){
      setResultText("Couldn't connect to Health Authority: " + e.getMessage());
    }

    hostHealthEdit.setEnabled(false);
    portHealthEdit.setEnabled(false);

    setResultText("");
    disableButtons();
    new GrpcTask(new GenerateSignatureRunnable(), channelHealth, this).execute();
  }

  /* --------- CONNECT METHODS ---------- */

  public void connectServer(){
    String host = hostEdit.getText().toString();
    String portStr = portEdit.getText().toString();
    int port = TextUtils.isEmpty(portStr) ? 0 : Integer.valueOf(portStr);
    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(hostEdit.getWindowToken(), 0);
    channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
  }

  public void connectHealthAuthority() throws SSLException{
    String host = hostHealthEdit.getText().toString();
    String portStr = portHealthEdit.getText().toString();
    int port = TextUtils.isEmpty(portStr) ? 0 : Integer.valueOf(portStr);
    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(hostHealthEdit.getWindowToken(), 0);

    /* ----
    channelHealth = OkHttpChannelBuilder.forAddress(host, port)
            .sslSocketFactory(sslContext.getSocketFactory())
            .build();

     */

  }

  /*
  private SslContext buildSslContext() throws SSLException {
    SslContextBuilder builder = GrpcSslContexts.forClient();
    builder.trustManager(new File(trustCertCollectionFilePath));
    return builder.build();
  }
  */


  /* ---------- AUXILIARY METHODS ----------- */

  private void setResultText(String text) {
    resultText.setText(text);
  }

  private void disableButtons() {
    startContactTracingButton.setEnabled(false);
    exitContactTracingButton.setEnabled(false);
    registerInfectedButton.setEnabled(false);
    getInfectedButton.setEnabled(false);
    generateSignatureButton.setEnabled(false);
  }

  private void enableButtons() {
    startContactTracingButton.setEnabled(true);
    exitContactTracingButton.setEnabled(true);
    registerInfectedButton.setEnabled(true);
    getInfectedButton.setEnabled(true);
    generateSignatureButton.setEnabled(true);
  }

  class GenerateNumber extends TimerTask {
    public void run() {
      Random rnd = new Random();
      int n = 10000000 + rnd.nextInt(90000000);
      numbers.add(n);

      // FIXME : Generate Key
      int key = 123456789;
      keys.add(key);
    }
  }


  /* ====================================================================== */
  /* ====[                          GrpcTask                          ]==== */
  /* ====================================================================== */

  private static class GrpcTask extends AsyncTask<Void, Void, String> {
    private final GrpcRunnable grpcRunnable;
    private final ManagedChannel channel;
    private final WeakReference<ContactTracingActivity> activityReference;

    GrpcTask(GrpcRunnable grpcRunnable, ManagedChannel channel, ContactTracingActivity activity) {
      this.grpcRunnable = grpcRunnable;
      this.channel = channel;
      this.activityReference = new WeakReference<ContactTracingActivity>(activity);
    }

    @Override
    protected String doInBackground(Void... nothing) {
      try {
        String logs = grpcRunnable.run(ContactTracingGrpc.newBlockingStub(channel), ContactTracingGrpc.newStub(channel));
        return "Success!\n" + logs;
      } catch (Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        return "Failed... :\n" + sw;
      }
    }

    @Override
    protected void onPostExecute(String result) {
      ContactTracingActivity activity = activityReference.get();
      if (activity == null) {
        return;
      }
      activity.setResultText(result);
      activity.enableButtons();
    }

  } // private static class GrpcTask


  /* ====================================================================== */
  /* ====[                        GrpcRunnable                        ]==== */
  /* ====================================================================== */

  private interface GrpcRunnable {
    /** Perform a grpcRunnable and return all the logs. */
    String run(ContactTracingBlockingStub blockingStub, ContactTracingStub asyncStub) throws Exception;
  }

  /* ====[                  RegisterInfectedRunnable                  ]==== */

  private static class RegisterInfectedRunnable implements GrpcRunnable {
    @Override
    public String run(ContactTracingBlockingStub blockingStub, ContactTracingStub asyncStub) throws Exception {
      return registerInfected(blockingStub);
    }

    /** Blocking unary call. Calls registerInfected and prints the response. */
    private String registerInfected(ContactTracingBlockingStub blockingStub) throws StatusRuntimeException {
      StringBuffer logs = new StringBuffer();
      appendLogs(logs, "*** RegisterInfected ***");

      // Add numbers and keys to request
      RegisterInfectedRequest.Builder request = RegisterInfectedRequest.newBuilder();
      for (int i = 0; i < numbers.size(); i++) {
        int number = numbers.get(i);
        int key = keys.get(i);
        Infected responseData = Infected.newBuilder().setNumber(number).setKey(key).build();
        request.addInfected(responseData);
      }

      RegisterInfectedResponse response;
      response = blockingStub.registerInfected(request.build());
      appendLogs(logs, "Registered new infected");
      return logs.toString();
    }

  } // private static class RegisterInfectedRunnable


  /* ====[                       GetInfectedRunnable                       ]==== */

  private static class GetInfectedRunnable implements GrpcRunnable {

    @Override
    public String run(ContactTracingBlockingStub blockingStub, ContactTracingStub asyncStub) throws Exception {
      return getInfected(blockingStub);
    }

    /** Blocking unary call. Calls registerInfected and prints the response. */
    private String getInfected(ContactTracingBlockingStub blockingStub) throws StatusRuntimeException {
      StringBuffer logs = new StringBuffer();
      appendLogs(logs, "*** GetInfected started ***");

      GetInfectedRequest request = GetInfectedRequest.newBuilder()
              .setLastUpdate( instantToTimestamp(lastUpdate) )
              .build();

      GetInfectedResponse response = blockingStub.getInfected(request);
      lastUpdate = Instant.now();

      /* get response */
      List<Infected> new_data = response.getInfectedList();

      /* no updates */
      if (new_data.size() == 0){
        appendLogs(logs, "No new infected data available");
        return logs.toString();
      }

      /* return new updates */
      appendLogs(logs, "New infected data received:");
      for (Infected data : new_data)
        appendLogs(logs, "- Number: {0}, Key: {1}\n", data.getNumber(), data.getKey());

      return logs.toString();

    }

  } // private static class GetInfectedRunnable



  /* ====[                  GenerateSignatureRunnable                  ]==== */

  private static class GenerateSignatureRunnable implements GrpcRunnable {
    @Override
    public String run(ContactTracingBlockingStub blockingStub, ContactTracingStub asyncStub) throws Exception {
      return generateSignature(blockingStub);
    }

    /** Blocking unary call. Calls registerInfected and prints the response. */
    private String generateSignature(ContactTracingBlockingStub blockingStub) throws StatusRuntimeException {
      StringBuffer logs = new StringBuffer();
      appendLogs(logs, "*** Generating Signature ***");

      GenerateSignatureRequest.Builder request = GenerateSignatureRequest.newBuilder();
      for (int i = 0; i < numbers.size(); i++) {
        int number = numbers.get(i);
        int key = keys.get(i);
        Infected responseData = Infected.newBuilder().setNumber(number).setKey(key).build();

        request.addInfected(responseData);
      }

      GenerateSignatureResponse response;
      response = blockingStub.generateSignature(request.build());
      appendLogs(logs, "Received signature: {0}", response.getSignature());
      return logs.toString();
    }

  } // private static class GenerateSignatureRunnable


  private static void appendLogs(StringBuffer logs, String msg, Object... params) {
    if (params.length > 0) {
      logs.append(MessageFormat.format(msg, params));
    } else {
      logs.append(msg);
    }
    logs.append("\n");
  }

  private static Timestamp instantToTimestamp(Instant instant) {
    /* setup grpc timestamp */
    Timestamp timestamp = Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    return timestamp;
  }

} // public class ContactTracingActivity
