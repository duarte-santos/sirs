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
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.examples.contacttracing.*;
import pt.tecnico.examples.contacttracing.ContactTracingGrpc.*;

public class ContactTracingActivity extends AppCompatActivity {
  private ManagedChannel channel;

  private EditText hostEdit;
  private EditText portEdit;
  private Button startRouteGuideButton;
  private Button exitRouteGuideButton;
  private Button getFeatureButton;
  private Button listFeaturesButton;
  private Button recordRouteButton;
  private Button routeChatButton;
  private TextView resultText;

  static Instant lastUpdate;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contact);
    hostEdit = (EditText) findViewById(R.id.host_edit_text);
    portEdit = (EditText) findViewById(R.id.port_edit_text);
    startRouteGuideButton = (Button) findViewById(R.id.start_contact_tracing_button);
    exitRouteGuideButton = (Button) findViewById(R.id.exit_contact_tracing_button);
    getFeatureButton = (Button) findViewById(R.id.register_infected_button);
    listFeaturesButton = (Button) findViewById(R.id.list_features_button);
    recordRouteButton = (Button) findViewById(R.id.record_route_button);
    routeChatButton = (Button) findViewById(R.id.route_chat_button);
    resultText = (TextView) findViewById(R.id.result_text);
    resultText.setMovementMethod(new ScrollingMovementMethod());
    lastUpdate = Instant.now();
    disableButtons();
  }


  public void startContactTracing(View view) {
    String host = hostEdit.getText().toString();
    String portStr = portEdit.getText().toString();
    int port = TextUtils.isEmpty(portStr) ? 0 : Integer.valueOf(portStr);
    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(hostEdit.getWindowToken(), 0);
    channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    hostEdit.setEnabled(false);
    portEdit.setEnabled(false);
    startRouteGuideButton.setEnabled(false);
    enableButtons();
  }

  public void exitContactTracing(View view) {
    channel.shutdown();
    disableButtons();
    hostEdit.setEnabled(true);
    portEdit.setEnabled(true);
    startRouteGuideButton.setEnabled(true);
  }


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


  private void setResultText(String text) {
    resultText.setText(text);
  }

  private void disableButtons() {
    getFeatureButton.setEnabled(false);
    listFeaturesButton.setEnabled(false);
    recordRouteButton.setEnabled(false);
    routeChatButton.setEnabled(false);
    exitRouteGuideButton.setEnabled(false);
  }

  private void enableButtons() {
    exitRouteGuideButton.setEnabled(true);
    getFeatureButton.setEnabled(true);
    listFeaturesButton.setEnabled(true);
    recordRouteButton.setEnabled(true);
    routeChatButton.setEnabled(true);
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
      return registerInfected(409146138, 746188906, blockingStub);
    }

    /** Blocking unary call. Calls registerInfected and prints the response. */
    private String registerInfected(int number, int key, ContactTracingBlockingStub blockingStub) throws StatusRuntimeException {
      StringBuffer logs = new StringBuffer();
      appendLogs(logs, "*** RegisterInfected: number={0} key={1}", number, key);

      RegisterInfectedRequest request = RegisterInfectedRequest.newBuilder()
              .setNumber(number)
              .setKey(key)
              .build();

      RegisterInfectedResponse response;
      response = blockingStub.registerInfected(request);
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
