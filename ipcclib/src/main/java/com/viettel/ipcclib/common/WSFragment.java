package com.viettel.ipcclib.common;

import com.viettel.ipcclib.AppRTCClient;
import com.viettel.ipcclib.PeerConnectionClient;
import com.viettel.ipcclib.R;
import com.viettel.ipcclib.WebSocketRTCClient;
import com.viettel.ipcclib.chat.ChatActivity;
import com.viettel.ipcclib.constants.QuickstartPreferences;
import com.viettel.ipcclib.model.MessageData;
import com.viettel.ipcclib.model.Service;
import com.viettel.ipcclib.util.LooperExecutor;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import static com.viettel.ipcclib.RtcClient.appRtcClient;
import static com.viettel.ipcclib.RtcClient.peerConnectionClient;
import static com.viettel.ipcclib.RtcClient.peerConnectionClient2;
import static com.viettel.ipcclib.RtcClient.roomConnectionParameters;

/**
 * Created by Macbook on 3/17/17.
 */

public abstract class WSFragment extends Fragment {
  private static final boolean DEBUG = true;
  private static final String TAG = WSFragment.class.getSimpleName();
  public static final int CONNECTION_REQUEST = 1;
  protected Toast logToast;
  public boolean isError;
  //  private Intent intent = null;
  private boolean isBringToFrontReceiverRegistered;
  public boolean iceConnected;
  private BroadcastReceiver bringToFrontBroadcastReceiver;

//  public static AppRTCClient.RoomConnectionParameters roomConnectionParameters;
//  public static WebSocketRTCClient appRtcClient;
  private long callStartedTimeMs;

//  public static PeerConnectionClient peerConnectionClient = null;
//  public static PeerConnectionClient peerConnectionClient2 = null;
//
//  public static PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
//  public static AppRTCClient.SignalingParameters signalingParam;
  protected Activity mContext;
  protected int serviceId;
  private String domain;
  private String endPoint;


  // Log |msg| and Toast about it.
  protected void logAndToast(String msg) {
    if (!DEBUG) {
      return;
    }

    Log.d(TAG, msg);
    if (logToast != null) {
      logToast.cancel();
    }
    if (isContextAvailable()) {
      logToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
      logToast.show();
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mContext = (Activity) context;
//    new Handler().postDelayed(new Runnable() {
//      @Override
//      public void run() {
//        init();
//      }
//    }, 300);
    init();
  }

  @Override
  public void onDetach() {
    super.onDetach();
//    mContext = null;
  }

  //  private void initIntent() {
//    if (validateUrl(Configs.ROOM_URL)) {
//      Uri uri = Uri.parse(Configs.ROOM_URL);
//      intent = new Intent(this, ConnectActivity.class);
//      intent.setData(uri);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, cameraFps);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, videoStartBitrate);
//      intent.putExtra(CallActivity.EXTRA_VIDEOCODEC, videoCodec);
//      intent.putExtra(CallActivity.EXTRA_HWCODEC_ENABLED, hwCodec);
//      intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
//      intent.putExtra(CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
//      intent.putExtra(CallActivity.EXTRA_AECDUMP_ENABLED, aecDump);
//      intent.putExtra(CallActivity.EXTRA_OPENSLES_ENABLED, useOpenSLES);
//      intent.putExtra(CallActivity.EXTRA_AUDIO_BITRATE, audioStartBitrate);
//      intent.putExtra(CallActivity.EXTRA_AUDIOCODEC, audioCodec);
//      intent.putExtra(CallActivity.EXTRA_DISPLAY_HUD, displayHud);
//      intent.putExtra(CallActivity.EXTRA_TRACING, tracing);
//      intent.putExtra(CallActivity.EXTRA_CMDLINE, commandLineRun);
//      intent.putExtra(CallActivity.EXTRA_RUNTIME, runTimeMs);
//    }
//  }
//
//  public boolean validateUrl(String url) {
//    //if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
//    if (isWSUrl(url) || isWSSUrl(url)) {
//      return true;
//    }
//
//    new AlertDialog.Builder(mContext)
//        .setTitle(getText(R.string.invalid_url_title))
//        .setMessage(getString(R.string.invalid_url_text, url))
//        .setCancelable(false)
//        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
//          public void onClick(DialogInterface dialog, int id) {
//            dialog.cancel();
//          }
//        }).create().show();
//    return false;
//  }

  public boolean isContextAvailable() {
    return mContext != null && (mContext instanceof Activity && !mContext.isFinishing());
  }

  // Disconnect from remote resources, dispose of local resources, and exit.
  public void disconnect(boolean sendRemoteHangup) {
    Intent intent = new Intent("finish_CallActivity");
    mContext.sendBroadcast(intent);
  }

  private void disconnectWithErrorMessage(final String errorMessage) {
    if (!isContextAvailable()) {
      Log.e(TAG, "Critical error: " + errorMessage);
      disconnect(true);
    } else {
      new AlertDialog.Builder(mContext)
          .setTitle(getText(R.string.channel_error_title))
          .setMessage(errorMessage)
          .setCancelable(false)
          .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              dialog.cancel();
              disconnect(true);
            }
          }).create().show();
    }
  }

  protected void init() {
    //Bring Call2Front when
    bringToFrontBroadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {

        Intent intentStart = new Intent(mContext.getApplicationContext(),
            ChatActivity.class);
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
    initWebSocket(endPoint);
  }

  public void initWebSocket(String wsurl) {
    String from = "nandi";
    if (roomConnectionParameters == null)
      roomConnectionParameters = new AppRTCClient.RoomConnectionParameters(wsurl, from, false);

    Log.i(TAG, "creating appRtcClient with roomUri:" + wsurl + " from:" + from);
    // Create connection client and connection parameters.
    if (appRtcClient == null) {
      appRtcClient = new WebSocketRTCClient(mSignalingEvents, new LooperExecutor());
      // todo fake
      appRtcClient.setServiceId(serviceId);
      appRtcClient.setDomain(domain);
    }

    connectToWebsocket();
  }

  public void connectToWebsocket() {
    if (appRtcClient == null) {
      Log.e(TAG, "AppRTC client is not allocated for a call.");
      return;
    }

    appRtcClient.disconnect();
    callStartedTimeMs = System.currentTimeMillis();

    // Start room connection.
    appRtcClient.connectToWebsocket(roomConnectionParameters);
  }

//  private void connectToUser(int runTimeMs) {
//    initTurnServer();
//    appRtcClient.initUser();
//    String to = "112";
//    roomConnectionParameters.initiator = true;
//    roomConnectionParameters.to = to;
//  }

  private void registerBringToFrontReceiver() {
    if (!isBringToFrontReceiverRegistered) {
      LocalBroadcastManager.getInstance(mContext).registerReceiver(bringToFrontBroadcastReceiver,
          new IntentFilter(QuickstartPreferences.INCOMING_CALL));
      isBringToFrontReceiverRegistered = true;
    }
  }

  private void initTurnServer() {
    String dataJsonTurn = "{\n" +
        "    \"params\": {\n" +
        "        \"pc_config\": {\n" +
        "            \"iceServers\": [\n" +
        "                           {\n" +
        "                           \"username\": \"\",\n" +
        "                           \"password\": \"\",\n" +
        "                           \"urls\": [\n" +
        "                                    \"stun:10.60.96.57:8488\"\n" +
        "                                    ]\n" +
        "                           },\n" +
        "                           {\n" +
        "                           \"username\": \"viettel\",\n" +
        "                           \"password\": \"123456aA\",\n" +
        "                           \"urls\": [\n" +
        "                                    \"turn:10.60.96.57:8488\"\n" +
        "                                    ]\n" +
        "                           }\n" +
        "                           ]\n" +
        "        }\n" +
        "    },\n" +
        "    \"result\": \"SUCCESS\"\n" +
        "}\n";
    JSONObject appConfig = null;
    try {
      appConfig = new JSONObject(dataJsonTurn);
      String result = appConfig.getString("result");
      Log.i(TAG, "client debug ");
      if (!result.equals("SUCCESS")) {
        return;
      }

      String params = appConfig.getString("params");
      appConfig = new JSONObject(params);
      LinkedList<PeerConnection.IceServer> iceServers = WebSocketRTCClient.iceServersFromPCConfigJSON(appConfig.getString("pc_config"));
      AppRTCClient.SignalingParameters signalingParameters = new AppRTCClient.SignalingParameters(iceServers);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  protected void sendMessage(String msg) {
    appRtcClient.sendTextMessage(msg);
  }

  protected void sendTypingStatus(boolean typing) {
    if (appRtcClient != null)
      appRtcClient.sendTypingStatus(typing);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (peerConnectionClient != null) {
      peerConnectionClient.stopVideoSource();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (peerConnectionClient != null) {
      peerConnectionClient.startVideoSource();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

//    if (appRtcClient != null) {
//      appRtcClient.sendStopToPeer();
//      appRtcClient.leaveConversation();
//      appRtcClient.sendDisconnectToPeer();
//      appRtcClient = null;
//    }
//
//    if (peerConnectionClient != null) {
//      peerConnectionClient.close();
//      peerConnectionClient = null;
//    }
//
//    if (peerConnectionClient2 != null) {
//      peerConnectionClient2.close();
//      peerConnectionClient2 = null;
//    }
//
//    roomConnectionParameters = null;
//    peerConnectionParameters = null;
//    signalingParam = null;

  }

  // Listeners
  private PeerConnectionClient.PeerConnectionEvents mPeerConnectionEvents = new PeerConnectionClient.PeerConnectionEvents() {
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
      final long delta = System.currentTimeMillis() - callStartedTimeMs;
      mContext.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (appRtcClient != null) {
            logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms");
//                    if (roomConnectionParameters.initiator) {
//                        appRtcClient.call(sdp);
//                    } else
            {
              appRtcClient.sendOfferSdp(sdp, (peerConnectionClient2 != null));
            }
          }
        }
      });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
      mContext.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (appRtcClient != null) {
            appRtcClient.sendLocalIceCandidate(candidate, (peerConnectionClient2 != null));
          }
        }
      });
    }

    @Override
    public void onIceConnected() {

    }

    @Override
    public void onIceDisconnected() {
      mContext.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          logAndToast("ICE disconnected");
          iceConnected = false;
          disconnect(false);
        }
      });
    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {

    }

    @Override
    public void onPeerConnectionError(final String description) {
      reportError(description);
    }

    public void reportError(final String description) {
      mContext.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (!isError) {
            isError = true;
            disconnectWithErrorMessage(description);
          }
        }
      });
    }
  };


  public AppRTCClient.SignalingEvents getSignalingEvents() {
    return mSignalingEvents;
  }

  // Response
  private AppRTCClient.SignalingEvents mSignalingEvents = new AppRTCClient.SignalingEvents() {
    @Override
    public void onConnectedToRoom(AppRTCClient.SignalingParameters params) {
      logAndToast("onConnectedToRoom");
    }

    @Override
    public void onUserListUpdate(String response) {

    }

    @Override
    public void onReciveCall() {
//      Intent newIntent = new Intent(mContext, CallActivity.class);
//      newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//      newIntent.putExtra("keep", true);
//      newIntent.putExtras(getIntent());
//      startActivityForResult(newIntent, CONNECTION_REQUEST);
      onWSReciveCall();
    }

//    public Intent getIntent() {
//      Uri uri = Uri.parse(endPoint);
//      Intent intent = new Intent(mContext, CallActivity.class);
//      intent.setData(uri);
//      return intent;
//    }

    @Override
    public void onIncomingCall(String from) {

    }

    @Override
    public void onIncomingScreenCall(com.viettel.ipcclib.model.Message from) {
// super.onIncomingScreenCall()
      logAndToast("Creating OFFER for Screensharing Caller");
      //do nothing here - just in CallActivity

      if (peerConnectionClient != null) {
        peerConnectionClient2 = PeerConnectionClient.getInstance(true);

        peerConnectionClient2.createPeerConnectionFactoryScreen(mPeerConnectionEvents);

        peerConnectionClient2.createPeerConnectionScreen(peerConnectionClient.getRenderEGLContext(), peerConnectionClient.getScreenRender());
        // Create offer. Offer SDP will be sent to answering client in
        // PeerConnectionEvents.onLocalDescription event.
        peerConnectionClient2.createOffer();
      }
    }


    @Override
    public void onStartCommunication(SessionDescription sdp) {

    }

    @Override
    public void onStartScreenCommunication(SessionDescription sdp) {

    }

    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
      final long delta = System.currentTimeMillis() - callStartedTimeMs;
      if (!isContextAvailable()) return;

      mContext.runOnUiThread(new Runnable() {
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
    public void onRemoteScreenDescription(final SessionDescription sdp) {
      final long delta = System.currentTimeMillis() - callStartedTimeMs;
      mContext.runOnUiThread(new Runnable() {
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
    public void onRemoteIceCandidate(final IceCandidate candidate) {
      mContext.runOnUiThread(new Runnable() {
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
    public void onRemoteScreenIceCandidate(final IceCandidate candidate) {
      mContext.runOnUiThread(new Runnable() {
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

    @Override
    public void onChannelClose() {
      mContext.runOnUiThread(new Runnable() {
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
      mContext.sendBroadcast(intent);
    }

    @Override
    public void onEndVideoCallFromWeb() {
    }

    @Override
    public void onChannelError(String description) {
      onWSChannelError(description);
    }

    @Override
    public void onConversationReady() {

    }

    @Override
    public void onAgentMissedChat() {
      onWSNotFoundAgentAvailable();
    }

    @Override
    public void endVideoCall() {
      onEndVideoCall();
    }

    @Override
    public void onNoAgentResponse() {

    }

    @Override
    public void onMessageCome(MessageData message) {
      onWSMessageReceived(message);
    }

    @Override
    public void onAgentEndConversation(String agentName) {
      onWSAgentEndConversation(agentName);
    }

    @Override
    public void onServiceListResponse(List<Service> services) {

    }

    @Override
    public void onConnected() {
      onWSConnected();
    }

    @Override
    public void onAgentTyping(String name, boolean typing) {
      onWSAgentTyping(name, typing);
    }

    @Override
    public void onAgentJoinConversation(String fullName) {
      onWSAgentJoinConversation(fullName);
    }
  };

  protected abstract void onWSReciveCall();

  protected abstract void onEndVideoCall();

  protected abstract void onWSChannelError(String description);

  protected abstract void onWSAgentJoinConversation(String fullName);

  protected abstract void onWSAgentEndConversation(String agentName);

  protected abstract void onWSNotFoundAgentAvailable();

  protected abstract void onWSMessageReceived(MessageData message);

  protected abstract void onWSAgentTyping(String name, boolean typing);

  protected abstract void onWSConnected();

  public WSFragment setServiceId(int serviceId) {
    this.serviceId = serviceId;
    return this;
  }

  public WSFragment setDomain(String domain) {
    this.domain = domain;
    return this;
  }

  public WSFragment setEndPoint(String endPoint) {
    this.endPoint = endPoint;
    return this;
  }
}
