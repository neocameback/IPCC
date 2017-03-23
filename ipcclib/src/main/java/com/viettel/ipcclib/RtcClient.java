package com.viettel.ipcclib;

import org.webrtc.EglBase;

import android.content.SharedPreferences;

/**
 * Created by Macbook on 3/23/17.
 */

public class RtcClient {
  public static AppRTCAudioManager audioManager = null;
  public static SharedPreferences sharedPref;
  public static AppRTCClient.RoomConnectionParameters roomConnectionParameters;
  public static WebSocketRTCClient appRtcClient;

  public static EglBase rootEglBase = null;
  public static PeerConnectionClient peerConnectionClient = null;
  public static PeerConnectionClient peerConnectionClient2 = null;
  public static PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
  public static AppRTCClient.SignalingParameters signalingParam;

}
