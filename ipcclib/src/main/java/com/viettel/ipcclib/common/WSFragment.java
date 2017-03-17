package com.viettel.ipcclib.common;

import com.viettel.ipcclib.AppRTCClient;
import com.viettel.ipcclib.CallActivity;
import com.viettel.ipcclib.PeerConnectionClient;
import com.viettel.ipcclib.R;
import com.viettel.ipcclib.WebSocketRTCClient;
import com.viettel.ipcclib.chat.ChatActivity;
import com.viettel.ipcclib.constants.Configs;
import com.viettel.ipcclib.constants.QuickstartPreferences;
import com.viettel.ipcclib.model.MessageData;
import com.viettel.ipcclib.model.Service;
import com.viettel.ipcclib.util.LooperExecutor;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;

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

import java.util.List;

import static com.viettel.ipcclib.RTCConnection.peerConnectionClient;
import static com.viettel.ipcclib.RTCConnection.peerConnectionClient2;

/**
 * Created by Macbook on 3/17/17.
 */

public class WSFragment extends Fragment {
  private static final String TAG = WSFragment.class.getSimpleName();
  public static final int CONNECTION_REQUEST = 1;
  protected Toast logToast;
  public boolean isError;
  private Intent intent = null;
  private boolean isBringToFrontReceiverRegistered;
  public boolean iceConnected;
  private BroadcastReceiver bringToFrontBroadcastReceiver;

  private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
  private WebSocketRTCClient appRtcClient;
  private long callStartedTimeMs;

  // Log |msg| and Toast about it.
  protected void logAndToast(String msg) {
    Log.d(TAG, msg);
    if (logToast != null) {
      logToast.cancel();
    }
    logToast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
    logToast.show();
  }

  public boolean isContextAvailable() {
    return getActivity() != null && !getActivity().isFinishing();
  }

  // Disconnect from remote resources, dispose of local resources, and exit.
  public void disconnect(boolean sendRemoteHangup) {
    Intent intent = new Intent("finish_CallActivity");
    getActivity().sendBroadcast(intent);
  }

  private void disconnectWithErrorMessage(final String errorMessage) {
    if (!isContextAvailable()) {
      Log.e(TAG, "Critical error: " + errorMessage);
      disconnect(true);
    } else {
      new AlertDialog.Builder(getActivity())
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

        Intent intentStart = new Intent(getActivity().getApplicationContext(),
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
    initWebSocket(Configs.ROOM_URL);
  }

  public void initWebSocket(String wsurl) {
    String from = "nandi";
    roomConnectionParameters = new AppRTCClient.RoomConnectionParameters(wsurl, from, false);

    Log.i(TAG, "creating appRtcClient with roomUri:" + wsurl + " from:" + from);
    // Create connection client and connection parameters.
    appRtcClient = new WebSocketRTCClient(mSignalingEvents, new LooperExecutor());

    connectToWebsocket();
  }

  public void connectToWebsocket() {
    if (appRtcClient == null) {
      Log.e(TAG, "AppRTC client is not allocated for a call.");
      return;
    }
    callStartedTimeMs = System.currentTimeMillis();

    // Start room connection.
    appRtcClient.connectToWebsocket(roomConnectionParameters);
  }

  private void connectToUser(int runTimeMs) {
//      initTurnServer();
    appRtcClient.initUser();
    String to = "112";
    roomConnectionParameters.initiator = true;
    roomConnectionParameters.to = to;
  }

  private void registerBringToFrontReceiver() {
    if (!isBringToFrontReceiverRegistered) {
      LocalBroadcastManager.getInstance(getActivity()).registerReceiver(bringToFrontBroadcastReceiver,
          new IntentFilter(QuickstartPreferences.INCOMING_CALL));
      isBringToFrontReceiverRegistered = true;
    }
  }

  // Listeners
  private PeerConnectionClient.PeerConnectionEvents mPeerConnectionEvents = new PeerConnectionClient.PeerConnectionEvents() {
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
      final long delta = System.currentTimeMillis() - callStartedTimeMs;
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (appRtcClient != null) {
            logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms");
//                    if (roomConnectionParameters.initiator) {
//                        appRtcClient.call(sdp);
//                    } else
            {
              appRtcClient.sendOfferSdp(sdp,(peerConnectionClient2!=null));
            }
          }
        }
      });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (appRtcClient != null) {
            appRtcClient.sendLocalIceCandidate(candidate,(peerConnectionClient2!=null));
          }
        }
      });
    }
    @Override
    public void onIceConnected() {

    }

    @Override
    public void onIceDisconnected() {
      getActivity().runOnUiThread(new Runnable() {
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
      getActivity().runOnUiThread(new Runnable() {
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

  // Response
  private AppRTCClient.SignalingEvents mSignalingEvents = new AppRTCClient.SignalingEvents() {
    @Override
    public void onConnectedToRoom(AppRTCClient.SignalingParameters params) {

    }

    @Override
    public void onUserListUpdate(String response) {

    }

    @Override
    public void onReciveCall() {
      Intent newIntent = new Intent(getActivity(), CallActivity.class);
      newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      newIntent.putExtra("keep", true);
      newIntent.putExtras(intent);
      startActivityForResult(newIntent, CONNECTION_REQUEST);
    }

    @Override
    public void onIncomingCall(String from) {

    }

    @Override
    public void onIncomingScreenCall(com.viettel.ipcclib.model.Message from) {
// super.onIncomingScreenCall()
      logAndToast("Creating OFFER for Screensharing Caller");
      //do nothing here - just in CallActivity

      peerConnectionClient2 = PeerConnectionClient.getInstance(true);

      peerConnectionClient2.createPeerConnectionFactoryScreen(mPeerConnectionEvents);

      peerConnectionClient2.createPeerConnectionScreen(peerConnectionClient.getRenderEGLContext(),peerConnectionClient.getScreenRender());
      // Create offer. Offer SDP will be sent to answering client in
      // PeerConnectionEvents.onLocalDescription event.
      peerConnectionClient2.createOffer();
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
      getActivity().runOnUiThread(new Runnable() {
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
      getActivity().runOnUiThread(new Runnable() {
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
      getActivity().runOnUiThread(new Runnable() {
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
      getActivity().runOnUiThread(new Runnable() {
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
      getActivity().runOnUiThread(new Runnable() {
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
      getActivity().sendBroadcast(intent);
    }

    @Override
    public void onChannelError(String description) {

    }

    @Override
    public void onConversationReady() {

    }

    @Override
    public void onAgentMissedChat() {

    }

    @Override
    public void endVideoCall() {

    }

    @Override
    public void onNoAgentResponse() {

    }

    @Override
    public void onMessageCome(MessageData message) {

    }

    @Override
    public void onAgentEndConversation() {

    }

    @Override
    public void onServiceListResponse(List<Service> services) {

    }
  };

}
