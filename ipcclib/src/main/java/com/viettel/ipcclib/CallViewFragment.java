/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.viettel.ipcclib;

import com.viettel.ipcclib.chat.ChatActivity;
import com.viettel.ipcclib.constants.Configs;
import com.viettel.ipcclib.videocall.DraggableService;

import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.RendererCommon;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import static com.viettel.ipcclib.RTCConnection.STAT_CALLBACK_PERIOD;
import static com.viettel.ipcclib.common.WSFragment.appRtcClient;
import static com.viettel.ipcclib.common.WSFragment.peerConnectionClient;
import static com.viettel.ipcclib.common.WSFragment.peerConnectionClient2;
import static com.viettel.ipcclib.common.WSFragment.peerConnectionParameters;
import static com.viettel.ipcclib.common.WSFragment.roomConnectionParameters;


/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
public class CallViewFragment extends Fragment implements
    CallFragment.OnCallEvents,
    PeerConnectionClient.PeerConnectionEvents,
    WebSocketChannelClient.WebSocketChannelEvents
    // AppRTCClient.SignalingEvents,
    //    PeerConnectionClient.PeerConnectionEvents,
    //     WebSocketChannelClient.WebSocketChannelEvents
{

  public String from = "";
  private static final String TAG = "RTCConnection";
  public boolean iceConnected;
  public boolean isError;
  public static SharedPreferences sharedPref;
  public int runTimeMs;
  public boolean activityRunning;

  public static AppRTCAudioManager audioManager = null;
  public boolean callControlFragmentVisible = true;

  // Screen video screen position
  private static final int SCREEN_X = 0;
  private static final int SCREEN_Y = 0;
  private static final int SCREEN_WIDTH = 100;
  private static final int SCREEN_HEIGHT = 100;
  private Intent intent = null;
  private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
  // private AppRTCClient appRtcClient;
  private ScalingType scalingType;
  private Toast logToast;
  private boolean commandLineRun;
  private boolean sendDisconnectToPeer = true;
  private long callStartedTimeMs = 0;
  // Controls
  public CallFragment callFragment;
  public HudFragment hudFragment;
  public static EglBase rootEglBase;
  //  public PercentFrameLayout localRenderLayout;F
  public PercentFrameLayout remoteRenderLayout;
  public PercentFrameLayout screenRenderLayout;

  //  public SurfaceViewRenderer localRender;
  public SurfaceViewRenderer remoteRender;
  public SurfaceViewRenderer screenRender;
  private GestureDetectorCompat mDetector;
  private static boolean broadcastIsRegistered;
  private View mRoot;

  ChatActivity mChatActivity;

  public void showFullVideoText(boolean isShow) {
    if (mBound)
      mService.updateShowFullVideoText(isShow);
  }

  @Override
  public void onStart() {
    super.onStart();
    activityRunning = true;
    // Video is not paused for screencapture. See onPause.

  }

  public void releaseContextCall() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mChatActivity = (ChatActivity) getActivity();
//    Thread.setDefaultUncaughtExceptionHandler(
//        new UnhandledExceptionHandler(this));

    // Set window styles for fullscreen-window size. Needs to be done before
    // adding content.
//    getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
//    getActivity().getWindow().addFlags(
//        LayoutParams.FLAG_FULLSCREEN
//            | LayoutParams.FLAG_KEEP_SCREEN_ON
//            | LayoutParams.FLAG_DISMISS_KEYGUARD
//            | LayoutParams.FLAG_SHOW_WHEN_LOCKED
//            | LayoutParams.FLAG_TURN_SCREEN_ON);
//
//    getActivity().getWindow().getDecorView().setSystemUiVisibility(
//        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//            | View.SYSTEM_UI_FLAG_FULLSCREEN
//            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

//    setContentView(R.layout.activity_call);

  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mRoot = inflater.inflate(R.layout.activity_call, container, false);
    iceConnected = false;

    scalingType = ScalingType.SCALE_ASPECT_FIT;


//    if (callFragment == null) {
//      callFragment = new CallFragment();
//      hudFragment = new HudFragment();
//    }

    // Create UI controls.
//    localRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
//    if (remoteRender == null) {
    remoteRender = (SurfaceViewRenderer) mRoot.findViewById(R.id.remote_video_view);
//    }
//    if (screenRender == null)
    screenRender = (SurfaceViewRenderer) mRoot.findViewById(R.id.remote_screen_view);

//    localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
    remoteRenderLayout = (PercentFrameLayout) mRoot.findViewById(R.id.remote_video_layout);

    screenRenderLayout = (PercentFrameLayout) mRoot.findViewById(R.id.remote_screen_layout);


    // Show/hide call control fragment on view click.
    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        toggleCallControlFragmentVisibility();
      }
    };

//    localRender.setOnClickListener(listener);
    remoteRender.setOnClickListener(listener);
    screenRender.setOnClickListener(listener); //screensharing

    // Create video renderers.
    rootEglBase = EglBase.create();
//    localRender.init(rootEglBase.getEglBaseContext(), null);
    remoteRender.init(rootEglBase.getEglBaseContext(), null);
    screenRender.init(rootEglBase.getEglBaseContext(), null);

//    localRender.setZOrderMediaOverlay(true);
    screenRender.setZOrderMediaOverlay(true);
    updateVideoView();
//    localRender.setVisibility(View.GONE);
//    localRenderLayout.setVisibility(View.GONE);
//    setResult(RESULT_CANCELED);

    if (!broadcastIsRegistered) {
      getActivity().registerReceiver(broadcast_reciever, new IntentFilter("finish_CallActivity"));
      getActivity().registerReceiver(broadcast_reciever, new IntentFilter("finish_screensharing"));
      broadcastIsRegistered = true;
    }

    // For command line execution run connection for <runTimeMs> and exit.
//    if (commandLineRun && runTimeMs > 0) {
//      (new Handler()).postDelayed(new Runnable() {
//        @Override
//        public void run() {
//          disconnect(false);
//        }
//      }, runTimeMs);
//    }

    // Create and audio manager that will take care of audio routing,
    // audio modes, audio device enumeration etc.
    audioManager = AppRTCAudioManager.create(getActivity(), new Runnable() {
          // This method will be called each time the audio state (number and
          // type of devices) has been changed.
          @Override
          public void run() {
            onAudioManagerChangedState();
          }
        }
    );

    // Store existing audio settings and change audio mode to
    // MODE_IN_COMMUNICATION for best possible VoIP performance.
    Log.d(TAG, "Initializing the audio manager...");
    audioManager.init();
    bindDragService();
    return mRoot;
  }

  private void onAudioManagerChangedState() {
    // TODO(henrika): disable video if AppRTCAudioManager.AudioDevice.EARPIECE
    // is active.
  }


  public void updateVideoView() {

    screenRenderLayout.setPosition(SCREEN_X, SCREEN_Y, SCREEN_WIDTH, SCREEN_HEIGHT);
    screenRender.setScalingType(scalingType);
    screenRender.setMirror(false);

    remoteRenderLayout.setPosition(SCREEN_X, SCREEN_Y, SCREEN_WIDTH, SCREEN_HEIGHT);
    remoteRender.setScalingType(scalingType);
    remoteRender.setMirror(false);

    if (mDragSerfaceView != null) {
      mDragSerfaceView.setScalingType(scalingType);
      mDragSerfaceView.setMirror(false);
      mDragSerfaceView.requestLayout();

      if (iceConnected) {
//      localRenderLayout.setPosition(
//          LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
//      localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mDragSerfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

      } else {
//      localRenderLayout.setPosition(
//          LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING);
        mDragSerfaceView.setScalingType(scalingType);
      }
//    localRenderLayout.setVisibility(View.GONE);
      mDragSerfaceView.setMirror(true);

      mDragSerfaceView.requestLayout();
    }


    remoteRender.requestLayout();
    screenRender.requestLayout();
  }


  @Override
  public void onPeerConnectionStatsReady(final StatsReport[] reports) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!isError && iceConnected) {
          hudFragment.updateEncoderStatistics(reports);
        }
      }
    });
  }

  @Override
  public void onIceConnected() {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        logAndToast("ICE connected, delay=" + delta + "ms");
        iceConnected = true;
        callConnected();
      }
    });
  }

  @Override
  public void onCameraSwitch() {
    if (peerConnectionClient != null) {
      peerConnectionClient.switchCamera();
    }
//    if (peerConnectionClient != null) {
//      boolean renderVideo = !peerConnectionClient.renderVideo;
//      peerConnectionClient.setVideoEnabled(renderVideo);
//      logAndToast(renderVideo ? "video enabled" : "video disabled");
//    }
  }

  @Override
  public void onAudioMute() {

    boolean muted = audioManager.setMicrophoneMute(true);
    logAndToast(muted ? "muted" : "unmuted");
  }

  @Override
  public void onBack() {
    mChatActivity.onBackPressed();
  }

  @Override
  public void onCaptureFormatChange(int width, int height, int framerate) {
    if (peerConnectionClient != null) {
      peerConnectionClient.changeCaptureFormat(width, height, framerate);
    }
  }

  @Override
  public void onVideoScalingSwitch(RendererCommon.ScalingType scalingType) {
    this.scalingType = scalingType;
    updateVideoView();
  }

  @Override
  public void onDestroy() {
//    disconnect(sendDisconnectToPeer);

    unbindSerice();
    getActivity().unregisterReceiver(broadcast_reciever);
    broadcastIsRegistered = false;
    activityRunning = false;
    audioManager.close();
    super.onDestroy();
  }

  // CallFragment.OnCallEvents interface implementation.
  @Override
  public void onCallHangUp() {
    disconnect(true);
  }

  public void onChannelError(String description) {
    reportError(description);
  }

  // Should be called from UI thread
  private void callConnected() {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    Log.i(TAG, "Call connected: delay=" + delta + "ms");
    if (peerConnectionClient == null || isError) {
      Log.w(TAG, "Call is connected in closed or error state");
      return;
    }
    // Update video view.
    updateVideoView();
    // Enable statistics callback.
    peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
  }

  public void disconnect(boolean sendRemoteHangup) {

//    if (localRender != null) {
//      localRender.release();
//      localRender = null;
//    }
    if (audioManager != null) {
      audioManager.close();
      audioManager = null;
    }
    if (remoteRender != null) {
      remoteRender.release();
      remoteRender = null;
    }

    if (screenRender != null) {
      screenRender.release();
      screenRender = null;
    }

    if (mDragSerfaceView != null) {
      mDragSerfaceView.release();
      mDragSerfaceView = null;
    }

    if (appRtcClient != null && sendRemoteHangup) {
      appRtcClient.sendDisconnectToPeer(); //send bye message to peer only when initiator
      sendDisconnectToPeer = false;
       appRtcClient = null;
    }

    //DON'T DO THAT if(appRtcClient != null) appRtcClient = null;

    if (peerConnectionClient != null) {
      peerConnectionClient.close();
      peerConnectionClient = null;
    }

    if (peerConnectionClient2 != null) {
      peerConnectionClient2.close();
      peerConnectionClient2 = null;
    }
    rootEglBase.release();
    if (sendRemoteHangup) {
      unbindSerice();
      mChatActivity.hangoutVideoCall();
    }
  }

  private void unbindSerice() {
    if (mBound)
      mChatActivity.unbindService(mConnection);
    mBound = false;
    mService.stopService();
  }

  // Helper functions.
  public void toggleCallControlFragmentVisibility() {
    if (!iceConnected || !callFragment.isAdded()) {
      return;
    }
    // Show/hide call control fragment
    callControlFragmentVisible = !callControlFragmentVisible;
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    if (callControlFragmentVisible) {
      ft.show(callFragment);
      ft.show(hudFragment);
    } else {
      ft.hide(callFragment);
      ft.hide(hudFragment);
    }
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ft.commit();
  }

  BroadcastReceiver broadcast_reciever = new BroadcastReceiver() {

    @Override
    public void onReceive(Context arg0, Intent intent) {

      String action = intent.getAction();
      if (action.equals("finish_CallActivity")) {
        sendDisconnectToPeer = false;
        getActivity().finish();
      }

      if (action.equals("finish_screensharing")) {
        //  http://stackoverflow.com/questions/37385522/how-to-change-surfaceviews-z-order-runtime-in-android
        if (screenRender != null) {
          //

          peerConnectionClient2.close();
          screenRenderLayout.removeView(screenRender);
          //  screenRender.release();
          // screenRenderLayout.add(screenRender);
          screenRenderLayout.addView(screenRender, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
          // screenRender.setVisibility(View.GONE);
          screenRender.setZOrderMediaOverlay(true);
          // screenRender.release();
          //screenRender = null;
          // screenRender = (SurfaceViewRenderer) findViewById(R.id.remote_screen_view);
          //  screenRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_screen_layout);
          //  screenRender.init(rootEglBase.getEglBaseContext(), null);
          //  screenRender.setZOrderMediaOverlay(true);
                /*
                remoteRenderLayout.removeView(remoteRender);
                localRenderLayout.removeView(localRender);
               localRender.setVisibility(View.GONE);
                remoteRender.setVisibility(View.GONE);
                localRender.setZOrderMediaOverlay(true);
                remoteRender.setZOrderMediaOverlay(false);*/
          // localRenderLayout.addView(mLocalRender, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
          // remoteRenderLayout.addView(mRemoteRender, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
          // localRender.setVisibility(View.VISIBLE);
          // remoteRender.setVisibility(View.VISIBLE);
          // localRender.setZOrderMediaOverlay(true);
          //  screenRender.setVisibility(View.INVISIBLE);

              /* if (peerConnectionClient2 != null) {
                  peerConnectionClient2.close();
                  peerConnectionClient2 = null;
                }*/

          //   screenRenderLayout.setPosition(SCREEN_X, SCREEN_Y, SCREEN_WIDTH, SCREEN_HEIGHT);
          //   screenRender.setScalingType(scalingType);
          //   screenRender.setMirror(false);
          //   screenRender.requestLayout();
          //  updateVideoView();
        }

      }
    }
  };


  @Override
  public void onWebSocketMessage(String message) {

  }

  @Override
  public void onWebSocketClose() {

  }

  @Override
  public void onWebSocketConnected() {

  }

  // TODO drag
  private DraggableService mService;
  boolean mBound = false;
  private static SurfaceViewRenderer mDragSerfaceView;


  /**
   * Defines callbacks for service binding, passed to bindService()
   */
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      DraggableService.LocalBinder binder = (DraggableService.LocalBinder) service;
      mService = binder.getService();
      mBound = true;
      mDragSerfaceView = mService.getTextureView();

      mDragSerfaceView.setZOrderMediaOverlay(true);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity().getApplication())) {
        return;
      }
      // show video call fragment when click drag layout
      mService.getShowFullTv().setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mChatActivity.showCallVideoFragment();
        }
      });
      initDragAndMakeCall();
//      reopenCamera();
//      mService.setImageView(mImageView);
    }


    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }
  };

  private void initDragAndMakeCall() {
//    if (callFragment == null) {
    callFragment = new CallFragment();
    callFragment.setCallEvents(this);
    hudFragment = new HudFragment();
    // Send intent arguments to fragments.
    callFragment.setArguments(getArguments());
    hudFragment.setArguments(getArguments());

    // Activate call and HUD fragments and start the call.
//    }
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    ft.add(R.id.call_fragment_container, callFragment);
    ft.add(R.id.hud_fragment_container, hudFragment);
    ft.commit();

    setupPeerConnection();
//    updateVideoView();
    // Create offer. Offer SDP will be sent to answering client in
    // PeerConnectionEvents.onLocalDescription event.
  }

  private void setupPeerConnection() {
    // setup video
    if (peerConnectionClient == null) {
      mDragSerfaceView.init(rootEglBase.getEglBaseContext(), null);
      initPeerConnectionParameters();
      peerConnectionClient = PeerConnectionClient.getInstance(true);
      peerConnectionClient.createPeerConnectionFactory(
          getActivity(), peerConnectionParameters, this);

      peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(),
          mDragSerfaceView, remoteRender, screenRender,
          roomConnectionParameters.initiator);
      logAndToast("Creating OFFER...");
      peerConnectionClient.createOffer();
      appRtcClient.makeCall();
    } else {
//      peerConnectionClient.setScreenSharingConnection(true);
//      peerConnectionClient.createPeerConnectionFactory(
//          CallActivity.this, peerConnectionParameters, CallActivity.this);
//      peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(),
//          mDragSerfaceView, remoteRender, screenRender,
//          roomConnectionParameters.initiator);

      peerConnectionClient.replaceRemoteRender(remoteRender);
      peerConnectionClient.replaceScreenRender(screenRender);

      remoteRender.requestLayout();
      screenRender.requestLayout();

//      screenRenderLayout.removeView(screenRender);
      //  screenRender.release();
      // screenRenderLayout.add(screenRender);
//      screenRenderLayout.addView(screenRender, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//      updateVideoView();

//      peerConnectionClient = PeerConnectionClient.getInstance(false);
//      peerConnectionClient.setPeerConnectionParameters(peerConnectionParameters);
////      peerConnectionClient.createPeerConnectionFactory(
////          CallActivity.this, peerConnectionParameters, CallActivity.this);
//      iceConnected = true;
//      peerConnectionClient.createPeerConnectionFactoryScreen(this);
//      peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(),
//          mDragSerfaceView, remoteRender, screenRender, false);
//

//      peerConnectionClient.setVideoEnabled(true);
//      peerConnectionClient.setScreenRender(screenRender);
//      peerConnectionClient.setScreenSharingConnection(true);
//      peerConnectionClient.startVideoSource();
//      screenRender.postInvalidate();


    }
//    updateVideoView();
    // Create offer. Offer SDP will be sent to answering client in
    // PeerConnectionEvents.onLocalDescription event.
  }

  private void initPeerConnectionParameters() {
    sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    String keyprefFrom = getString(R.string.pref_from_key);
    String keyprefVideoCallEnabled = getString(R.string.pref_videocall_key);
    String keyprefResolution = getString(R.string.pref_resolution_key);
    String keyprefFps = getString(R.string.pref_fps_key);
    String keyprefCaptureQualitySlider = getString(R.string.pref_capturequalityslider_key);
    String keyprefVideoBitrateType = getString(R.string.pref_startvideobitrate_key);
    String keyprefVideoBitrateValue = getString(R.string.pref_startvideobitratevalue_key);
    String keyprefVideoCodec = getString(R.string.pref_videocodec_key);
    String keyprefHwCodecAcceleration = getString(R.string.pref_hwcodec_key);
    String keyprefCaptureToTexture = getString(R.string.pref_capturetotexture_key);
    String keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
    String keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
    String keyprefAudioCodec = getString(R.string.pref_audiocodec_key);
    String keyprefNoAudioProcessingPipeline = getString(R.string.pref_noaudioprocessing_key);
    String keyprefAecDump = getString(R.string.pref_aecdump_key);
    String keyprefOpenSLES = getString(R.string.pref_opensles_key);
    String keyprefDisplayHud = getString(R.string.pref_displayhud_key);
    String keyprefTracing = getString(R.string.pref_tracing_key);
    String keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
    String keyprefRoom = getString(R.string.pref_room_key);
    String keyprefRoomList = getString(R.string.pref_room_list_key);
    String from = sharedPref.getString(keyprefFrom, getString(R.string.pref_from_default));
//    String roomUrl = sharedPref.getString(
//        keyprefRoomServerUrl, getString(R.string.pref_room_server_url_default));
//    roomUrl = "wss://192.168.0.117:8898";
    // Video call enabled flag.
    boolean videoCallEnabled = sharedPref.getBoolean(keyprefVideoCallEnabled,
        Boolean.valueOf(getString(R.string.pref_videocall_default)));

    // Get default codecs.
    String videoCodec = sharedPref.getString(keyprefVideoCodec, getString(R.string.pref_videocodec_default));
    String audioCodec = sharedPref.getString(keyprefAudioCodec, getString(R.string.pref_audiocodec_default));

    // Check HW codec flag.
    boolean hwCodec = sharedPref.getBoolean(keyprefHwCodecAcceleration, Boolean.valueOf(getString(R.string.pref_hwcodec_default)));

    // Check Capture to texture.
    boolean captureToTexture = sharedPref.getBoolean(keyprefCaptureToTexture, Boolean.valueOf(getString(R.string.pref_capturetotexture_default)));

    // Check Disable Audio Processing flag.
    boolean noAudioProcessing = sharedPref.getBoolean(keyprefNoAudioProcessingPipeline, Boolean.valueOf(getString(R.string.pref_noaudioprocessing_default)));

    // Check Disable Audio Processing flag.
    boolean aecDump = sharedPref.getBoolean(keyprefAecDump, Boolean.valueOf(getString(R.string.pref_aecdump_default)));

    // Check OpenSL ES enabled flag.
    boolean useOpenSLES = sharedPref.getBoolean(
        keyprefOpenSLES,
        Boolean.valueOf(getString(R.string.pref_opensles_default)));

    // Get video resolution from settings.
    int videoWidth = 0;
    int videoHeight = 0;
    String resolution = sharedPref.getString(keyprefResolution,
        getString(R.string.pref_resolution_default));
    String[] dimensions = resolution.split("[ x]+");
    if (dimensions.length == 2) {
      try {
        videoWidth = Integer.parseInt(dimensions[0]);
        videoHeight = Integer.parseInt(dimensions[1]);
      } catch (NumberFormatException e) {
        videoWidth = 0;
        videoHeight = 0;
        Log.e(TAG, "Wrong video resolution setting: " + resolution);
      }
    }

    // Get camera fps from settings.
    int cameraFps = 0;
    String fps = sharedPref.getString(keyprefFps,
        getString(R.string.pref_fps_default));
    String[] fpsValues = fps.split("[ x]+");
    if (fpsValues.length == 2) {
      try {
        cameraFps = Integer.parseInt(fpsValues[0]);
      } catch (NumberFormatException e) {
        Log.e(TAG, "Wrong camera fps setting: " + fps);
      }
    }

    // Check capture quality slider flag.
    boolean captureQualitySlider = sharedPref.getBoolean(keyprefCaptureQualitySlider,
        Boolean.valueOf(getString(R.string.pref_capturequalityslider_default)));

    // Get video and audio start bitrate.
    int videoStartBitrate = 0;
    String bitrateTypeDefault = getString(
        R.string.pref_startvideobitrate_default);
    String bitrateType = sharedPref.getString(
        keyprefVideoBitrateType, bitrateTypeDefault);
    if (!bitrateType.equals(bitrateTypeDefault)) {
      String bitrateValue = sharedPref.getString(keyprefVideoBitrateValue, getString(R.string.pref_startvideobitratevalue_default));
      videoStartBitrate = Integer.parseInt(bitrateValue);
    }
    int audioStartBitrate = 0;
    bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
    bitrateType = sharedPref.getString(
        keyprefAudioBitrateType, bitrateTypeDefault);
    if (!bitrateType.equals(bitrateTypeDefault)) {
      String bitrateValue = sharedPref.getString(keyprefAudioBitrateValue,
          getString(R.string.pref_startaudiobitratevalue_default));
      audioStartBitrate = Integer.parseInt(bitrateValue);
    }

    // Check statistics display option.
    boolean displayHud = sharedPref.getBoolean(keyprefDisplayHud, Boolean.valueOf(getString(R.string.pref_displayhud_default)));

    boolean tracing = sharedPref.getBoolean(keyprefTracing, Boolean.valueOf(getString(R.string.pref_tracing_default)));

//    Log.d(TAG, "Connecting from " + from + " at URL " + roomUrl);

//    if (validateUrl(roomUrl)) {
//      Uri uri = Uri.parse(roomUrl);
//      intent = new Intent(this, CallActivity.class);
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

    // Check for mandatory permissions.
    peerConnectionParameters = new PeerConnectionClient.PeerConnectionParameters(
        videoCallEnabled,
        tracing,
        videoWidth, videoHeight, cameraFps, videoStartBitrate, videoCodec, hwCodec,
        captureToTexture, audioStartBitrate, audioCodec, noAudioProcessing,
        aecDump, useOpenSLES);

    roomConnectionParameters = new AppRTCClient.RoomConnectionParameters(Configs.ROOM_URL, from, false);
  }

  @Override
  public void onStop() {
    super.onStop();
//    if (mBound) {
////      closeCamera();
//      mService.removeTextureView();
//      unbindService(mConnection);
//      mBound = false;
//    }
  }

//  private void requestCameraPermission() {
//    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
//      new ConfirmationDialog().show(getFragmentManager(), FRAGMENT_DIALOG);
//    } else {
//      requestPermissions(new String[]{Manifest.permission.CAMERA},
//          REQUEST_CAMERA_PERMISSION);
//    }
//  }


  private void bindDragService() {
    if (mBound) return;

    Intent intent = new Intent(getActivity(), DraggableService.class);
    getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
  }

  // Log |msg| and Toast about it.
  public void logAndToast(String msg) {
    Log.d(TAG, msg);
    if (logToast != null) {
      logToast.cancel();
    }
    logToast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
    logToast.show();
  }

  @Override
  public void onWebSocketError(String description) {
    logAndToast(description);
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

  private void disconnectWithErrorMessage(final String errorMessage) {
    if (commandLineRun || !activityRunning) {
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
            appRtcClient.sendOfferSdp(sdp, (peerConnectionClient2 != null));
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
          appRtcClient.sendLocalIceCandidate(candidate, (peerConnectionClient2 != null));
        }
      }
    });
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
  public void onPeerConnectionError(final String description) {
    reportError(description);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (mService != null) {
      mService.onResume();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mService != null) {
      mService.onPause();
    }
  }
}