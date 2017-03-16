/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package de.lespace.apprtc;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.lespace.apprtc.constants.Configs;
import de.lespace.apprtc.model.Message;
import de.lespace.apprtc.util.LooperExecutor;


/**
 * Handles the initial setup where the user selects which room to join.
 */
public class ConnectChatActivity extends RTCConnection
        implements AppRTCClient.SignalingEvents {

  private static final String TAG = "ConnectActivity";
  private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
  private static boolean commandLineRun = false;


//  private ImageButton connectButton;
//  private String keyprefFrom;
//  private String keyprefVideoCallEnabled;
//  private String keyprefResolution;
//  private String keyprefFps;
//  private String keyprefCaptureQualitySlider;
//  private String keyprefVideoBitrateType;
//  private String keyprefVideoBitrateValue;
//  private String keyprefVideoCodec;
//  private String keyprefAudioBitrateType;
//  private String keyprefAudioBitrateValue;
//  private String keyprefAudioCodec;
//  private String keyprefHwCodecAcceleration;
//  private String keyprefCaptureToTexture;
//  private String keyprefNoAudioProcessingPipeline;
//  private String keyprefAecDump;
//  private String keyprefOpenSLES;
//  private String keyprefDisplayHud;
//  private String keyprefTracing;
//  private String keyprefRoomServerUrl;
//  private String keyprefRoom;
//  private String keyprefRoomList;
  private ListView roomListView;
  private List<String> missingPermissions;
  private Intent intent = null;

  private BroadcastReceiver bringToFrontBroadcastReceiver;

  private boolean isBringToFrontReceiverRegistered;

  // List of mandatory application permissions.
  private static final String[] MANDATORY_PERMISSIONS = {
          "android.permission.MODIFY_AUDIO_SETTINGS",
          "android.permission.RECORD_AUDIO",
          "android.permission.CAMERA",
          "android.permission.INTERNET"
  };
  /**
   * ATTENTION: This was auto-generated to implement the App Indexing API.
   * See https://g.co/AppIndexing/AndroidStudio for more information.
   */
  private GoogleApiClient client;

  @Override
  public void onRequestPermissionsResult(
          int requestCode,
          String permissions[],
          int[] grantResults) {

    if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(ConnectChatActivity.this, MANDATORY_PERMISSIONS[0] + " Permission Granted!", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(ConnectChatActivity.this, MANDATORY_PERMISSIONS[0] + " Permission Denied!", Toast.LENGTH_SHORT).show();
    }
    missingPermissions.remove(0); //remove missing permission from array and request next left permission
    requestPermission();
  }


  //http://stackoverflow.com/questions/35484767/activitycompat-requestpermissions-not-showing-dialog-box
  //https://developer.android.com/training/permissions/requesting.html
  private void requestPermission() {
    if (missingPermissions.size() > 0)
      ActivityCompat.requestPermissions(this, new String[]{missingPermissions.get(0)}, missingPermissions.size());
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_connect);
    // Get setting keys.
    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//    keyprefFrom = getString(R.string.pref_from_key);
//    keyprefVideoCallEnabled = getString(R.string.pref_videocall_key);
//    keyprefResolution = getString(R.string.pref_resolution_key);
//    keyprefFps = getString(R.string.pref_fps_key);
//    keyprefCaptureQualitySlider = getString(R.string.pref_capturequalityslider_key);
//    keyprefVideoBitrateType = getString(R.string.pref_startvideobitrate_key);
//    keyprefVideoBitrateValue = getString(R.string.pref_startvideobitratevalue_key);
//    keyprefVideoCodec = getString(R.string.pref_videocodec_key);
//    keyprefHwCodecAcceleration = getString(R.string.pref_hwcodec_key);
//    keyprefCaptureToTexture = getString(R.string.pref_capturetotexture_key);
//    keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
//    keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
//    keyprefAudioCodec = getString(R.string.pref_audiocodec_key);
//    keyprefNoAudioProcessingPipeline = getString(R.string.pref_noaudioprocessing_key);
//    keyprefAecDump = getString(R.string.pref_aecdump_key);
//    keyprefOpenSLES = getString(R.string.pref_opensles_key);
//    keyprefDisplayHud = getString(R.string.pref_displayhud_key);
//    keyprefTracing = getString(R.string.pref_tracing_key);
//    keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
//    keyprefRoom = getString(R.string.pref_room_key);
//    keyprefRoomList = getString(R.string.pref_room_list_key);
//    from = sharedPref.getString(keyprefFrom, getString(R.string.pref_from_default));
//    String roomUrl = sharedPref.getString(
//            keyprefRoomServerUrl, getString(R.string.pref_room_server_url_default));
    int counter = 0;
    missingPermissions = new ArrayList();

    for (String permission : MANDATORY_PERMISSIONS) {
      if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
        counter++;
        missingPermissions.add(permission);
      }
    }
    requestPermission();

    //Bring Call2Front when
    bringToFrontBroadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {

        Intent intentStart = new Intent(getApplicationContext(), ConnectChatActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        startActivity(intentStart);
        //  newFragment.show(transaction,"loading");

      //  showDialog();
      }
    };

    registerBringToFrontReceiver();

    if (checkPlayServices()) {
      // Start IntentService to register this application with GCM.
      Intent intent = new Intent(this, RegistrationIntentService.class);
      startService(intent);
    }


    ImageButton connectButton = (ImageButton) findViewById(R.id.connect_button);
    connectButton.setOnClickListener(connectListener);

    // If an implicit VIEW intent is launching the app, go directly to that URL.
    //final Intent intent = getIntent();
    Uri wsurl = Uri.parse(Configs.ROOM_URL);
    //intent.getData();
    Log.d(TAG, "connecting to:" + wsurl.toString());
    if (wsurl == null) {
      logAndToast(getString(R.string.missing_wsurl));
      Log.e(TAG, "Didn't get any URL in intent!");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }

    if (from == null || from.length() == 0) {
      logAndToast(getString(R.string.missing_from));
      Log.e(TAG, "Incorrect from in intent!");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }

    roomConnectionParameters = new AppRTCClient.RoomConnectionParameters(wsurl.toString(), from, false);

    Log.i(TAG, "creating appRtcClient with roomUri:" + wsurl.toString() + " from:" + from);
    // Create connection client and connection parameters.
    appRtcClient = new WebSocketRTCClient(this, new LooperExecutor());

    connectToWebsocket();
    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
  }

  @Override
  protected void onActivityResult(
          int requestCode, int resultCode, Intent data) {
    if (requestCode == CONNECTION_REQUEST && commandLineRun) {
      Log.d(TAG, "Return: " + resultCode);
      setResult(resultCode);
      commandLineRun = false;
      finish();
    }
  }

  private final OnClickListener connectListener = new OnClickListener() {
    @Override
    public void onClick(View view) {
      commandLineRun = false;
      connectToUser(0);
    }
  };

  private void connectToUser(int runTimeMs) {
//      initTurnServer();
      appRtcClient.initUser();
      String to = "112";
      roomConnectionParameters.initiator = true;
      roomConnectionParameters.to = to;


  }

  @Override
  public void onConnectedToRoom(final AppRTCClient.SignalingParameters params) {
  }

  @Override
  public void onUserListUpdate(final String response) {

    runOnUiThread(new Runnable() {


      @Override
      public void run() {
        try {
          JSONArray mJSONArray = new JSONArray(response);
          roomList = new ArrayList();
          adapter.clear();
          adapter.notifyDataSetChanged();


          for(int i = 0; i < mJSONArray.length();i++){
            String username = mJSONArray.getString(i);
            if (username.length() > 0
                    && !roomList.contains(username)
                    && !username.equals(roomConnectionParameters.from)) {
              roomList.add(username);
              adapter.add(username);
            }
          }
          adapter.notifyDataSetChanged();
        }catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  public void onReciveCall() {
    Intent newIntent = new Intent(this, CallActivity.class);
    newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    newIntent.putExtra("keep", true);
    newIntent.putExtras(intent);
    startActivityForResult(newIntent, CONNECTION_REQUEST);
  }

  @Override
  public void onIncomingCall(final String from) {


    // Notify UI that registration has completed, so the progress indicator can be hidden.
/*
        //Send Broadcast message to Service
        Intent registrationComplete = new Intent(QuickstartPreferences.INCOMING_CALL);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);

        startActivity(intent);*/

       /* Intent intent = new Intent(this, ConnectActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Intent intent = new Intent(this,CallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);*/
    // r.stop();
    //startActivity(intent);


    roomConnectionParameters.to = from;
    roomConnectionParameters.initiator = false;
    DialogFragment newFragment = new RTCConnection.CallDialogFragment();


    Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

    if(alert == null){
      // alert is null, using backup
      alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

      // I can't see this ever being null (as always have a default notification)
      // but just incase
      if(alert == null) {
        // alert backup is null, using 2nd backup
        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
      }
    }
    r = RingtoneManager.getRingtone(getApplicationContext(), alert);
    //  r.play();

    FragmentTransaction transaction = getFragmentManager().beginTransaction();
    transaction.add(newFragment, "loading");
    transaction.commitAllowingStateLoss();

  }

  @Override
  public void onIncomingScreenCall(Message from) {
    // super.onIncomingScreenCall()
    logAndToast("Creating OFFER for Screensharing Caller");
    //do nothing here - just in CallActivity

    peerConnectionClient2 = PeerConnectionClient.getInstance(true);

    peerConnectionClient2.createPeerConnectionFactoryScreen(this);

    peerConnectionClient2.createPeerConnectionScreen(peerConnectionClient.getRenderEGLContext(),peerConnectionClient.getScreenRender());
    // Create offer. Offer SDP will be sent to answering client in
    // PeerConnectionEvents.onLocalDescription event.
    peerConnectionClient2.createOffer();

  }


  @Override
  public void onStartCommunication(final SessionDescription sdp) {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (peerConnectionClient == null) {
          Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
          return;
        }
        logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
        peerConnectionClient.setRemoteDescription(sdp);
      }
    });
  }


  @Override
  public void onStartScreenCommunication(final SessionDescription sdp) {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (peerConnectionClient2 == null) {
          Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
          return;
        }
        logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
        peerConnectionClient2.setRemoteDescription(sdp);
      }
    });
  }

  @Override
  public void onRemoteDescription(final SessionDescription sdp) {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (peerConnectionClient == null) {
          Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
          return;
        }
        logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
        peerConnectionClient.setRemoteDescription(sdp);
      }
    });
  }

  @Override
  public void onRemoteIceCandidate(final IceCandidate candidate) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (peerConnectionClient == null) {
          Log.e(TAG,
                  "Received ICE candidate for non-initilized peer connection.");
          return;
        }
        peerConnectionClient.addRemoteIceCandidate(candidate);
      }
    });
  }

  @Override
  public void onRemoteScreenDescription(final SessionDescription sdp) {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (peerConnectionClient2 == null) {
          Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
          return;
        }
        logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
        peerConnectionClient2.setRemoteDescription(sdp);
      }
    });
  }

  @Override
  public void onRemoteScreenIceCandidate(final IceCandidate candidate) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (peerConnectionClient2 == null) {
          Log.e(TAG,
                  "Received ICE candidate for non-initilized peer connection.");
          return;
        }
        peerConnectionClient2.addRemoteIceCandidate(candidate);
      }
    });
  }

/*  public void onChannelClose() {

  }
*/
  @Override
  public void onChannelClose() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        logAndToast("stopCommunication from remotereceived; finishing CallActivity");
        disconnect(false);
      }
    });
  }

  @Override
  public void onChannelScreenClose() {
    Intent intent = new Intent("finish_screensharing");
    sendBroadcast(intent);
  }

  @Override
  public void onChannelError(String description) {
    logAndToast(description);
  }

  @Override
  public void onWebSocketMessage(String message) {
    //do nothing
  }

  @Override
  public void onWebSocketClose() {

  }

  private void registerBringToFrontReceiver() {
    if (!isBringToFrontReceiverRegistered) {
      LocalBroadcastManager.getInstance(this).registerReceiver(bringToFrontBroadcastReceiver,
              new IntentFilter(QuickstartPreferences.INCOMING_CALL));
      isBringToFrontReceiverRegistered = true;
    }
  }


  /**
   * Check the device to make sure it has the Google Play Services APK. If
   * it doesn't, display a dialog that allows users to download the APK from
   * the Google Play Store or enable it in the device's system settings.
   */
  private boolean checkPlayServices() {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
    if (resultCode != ConnectionResult.SUCCESS) {
      if (apiAvailability.isUserResolvableError(resultCode)) {
        apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                .show();
      } else {
        Log.i(TAG, "GooglePlayServices are not available.");
        finish();
      }
      return false;
    }
    return true;
  }

  /**
   * ATTENTION: This was auto-generated to implement the App Indexing API.
   * See https://g.co/AppIndexing/AndroidStudio for more information.
   */
  public Action getIndexApiAction() {
    Thing object = new Thing.Builder()
            .setName("Connect Page") // TODO: Define a title for the content shown.
            // TODO: Make sure this auto-generated URL is correct.
            .setUrl(Uri.parse("https://webrtc.a-fkd.de/jWebrtc"))
            .build();
    return new Action.Builder(Action.TYPE_VIEW)
            .setObject(object)
            .setActionStatus(Action.STATUS_TYPE_COMPLETED)
            .build();
  }

  @Override
  public void onStart() {
    super.onStart();

    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
    client.connect();
    AppIndex.AppIndexApi.start(client, getIndexApiAction());
  }

  @Override
  public void onStop() {
    super.onStop();

    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
    AppIndex.AppIndexApi.end(client, getIndexApiAction());
    client.disconnect();
  }


  public static class CallDialogFragment extends DialogFragment {

    public CallDialogFragment() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Build the dialog and set up the button click handlers
      // 1. Instantiate an AlertDialog.Builder with its constructor
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

// 2. Chain together various setter methods to set the dialog characteristics
      builder.setMessage(R.string.calldialog_question).setTitle(R.string.calldialog_title);
      // Add the buttons
      builder.setPositiveButton(R.string.calldialog_answer, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {

          Intent intent = new Intent(getActivity(), CallActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          r.stop();
          startActivity(intent);
        }
      });
      builder.setNegativeButton(R.string.calldialog_hangung, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          // User cancelled the dialog send stop message to peer.
          r.stop();
          appRtcClient.sendStopToPeer();
          ;
        }
      });

// 3. Get the AlertDialog from create()
      AlertDialog dialog = builder.create();

      return builder.create();
    }
  }
}
