/*
 *  Copyright 2013 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.viettel.ipcclib;

import com.viettel.ipcclib.model.Message;
import com.viettel.ipcclib.model.MessageData;
import com.viettel.ipcclib.model.Service;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.List;

/**
 * AppRTCClient is the interface representing an AppRTC client.
 */
public interface AppRTCClient {

  /**
   * Struct holding the connection parameters of an AppRTC room.
   */
  class RoomConnectionParameters {

    public String roomUrl;
    public String from;
    public boolean initiator;
    public String to;

    public RoomConnectionParameters(
        String roomUrl,
        String from,
        boolean initiator) {
          this.roomUrl = roomUrl;
          this.from = from;
          this.initiator = initiator;
    }

  }

  /**
   * Asynchronously connect to an AppRTC room URL using supplied connection
   * parameters. Once connection is established onConnectedToRoom()
   * callback with room parameters is invoked.
   */
  void connectToWebsocket(RoomConnectionParameters connectionParameters);

  /**
   * Send offer SDP to the other participant.
   */
  void call(final SessionDescription sdp);
  /**
   * Send offer SDP to the other participant.
   */
  //public void sendOfferSdp(final SessionDescription sdp);

  /**
   * Send answer SDP to the other participant.
   */
  void sendOfferSdp(final SessionDescription sdp, final boolean isScreensharing);

  /**
   * Send Ice candidate to the other participant.
   */
  void sendLocalIceCandidate(final IceCandidate candidate, final boolean isScreensharing);

  /**
   * Disconnect from room.
   */
  void reconnect();

  void register();

  void login();

  void initUser();
  void makeCall();
    void joinRoom();

  /**
   * Send stop message to peer
   */
  void sendStopToPeer();
  /**
   * Disconnect from room.
   */
  void sendDisconnectToPeer();

  void sendTextMessage(String messageText);

  void leaveConversation();

  void updateUserInfo(String username, String email);

  /**
   * Struct holding the signaling parameters of an AppRTC room.
   */
  class SignalingParameters {
    public static List<PeerConnection.IceServer> iceServers;

    public SignalingParameters(
        List<PeerConnection.IceServer> iceServers) {
      this.iceServers = iceServers;
    }
  }

  /**
   * Callback interface for messages delivered on signaling channel.
   *
   * <p>Methods are guaranteed to be invoked on the UI thread of |activity|.
   */
  interface SignalingEvents {
    /**
     * Callback fired once the room's signaling parameters
     * SignalingParameters are extracted.
     */
    void onConnectedToRoom(final SignalingParameters params);

    void onUserListUpdate(String response);

    void onReciveCall();
    void onIncomingCall(String from);
    void onIncomingScreenCall(Message from); //Screensharing only

    void onStartCommunication(final SessionDescription sdp);
    void onStartScreenCommunication(final SessionDescription sdp); //Screensharing only

    /**
     * Callback fired once remote SDP is received.
     */
    void onRemoteDescription(final SessionDescription sdp);
    void onRemoteScreenDescription(final SessionDescription sdp);

    /**
     * Callback fired once remote Ice candidate is received.
     */
    void onRemoteIceCandidate(final IceCandidate candidate);
    void onRemoteScreenIceCandidate(final IceCandidate candidate);

    /**
     * Callback fired once channel is closed.
     */
    void onChannelClose();
    void onChannelScreenClose();

    /**
     * Callback fired once channel error happened.
     */
    void onChannelError(final String description);

    void onConversationReady();

    void onAgentMissedChat();

    void endVideoCall();

    void onNoAgentResponse();

    void onMessageCome(MessageData message);

    void onAgentEndConversation(String agentName);

    void onServiceListResponse(List<Service> services);

    void onConnected();

    void onAgentTyping(String name, boolean typing);

    void onAgentJoinConversation(String fullName);

    void onEndVideoCallFromWeb();
  }
}
