/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.viettel.ipcclib;


import com.viettel.ipcclib.constants.GsonWrapper;
import com.viettel.ipcclib.constants.MessageType;
import com.viettel.ipcclib.model.Message;
import com.viettel.ipcclib.model.MessageData;
import com.viettel.ipcclib.util.LooperExecutor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import android.util.Log;

import java.util.LinkedList;

import static com.viettel.ipcclib.constants.MessageType.CUSTOMER_RESUMING_REQUEST;
import static com.viettel.ipcclib.constants.MessageType.LEAVE_CONVERSATION;
import static com.viettel.ipcclib.constants.MessageType.MESSAGE;
import static com.viettel.ipcclib.constants.MessageType.PING;
import static com.viettel.ipcclib.constants.MessageType.SEND_IS_CHAT_TYPING;


/**
 * Negotiates signaling for chatting with apprtc.appspot.com "rooms".
 *
 * <p>To use: create an instance of this object (registering a message handler) and
 * call connectToWebsocket().  Once room connection is established
 * onConnectedToRoom() callback with room parameters is invoked.
 * Messages to other party (with local Ice candidates and answer SDP) can
 * be sent after WebSocket connection is established.
 */
public class WebSocketRTCClient implements AppRTCClient, WebSocketChannelClient.WebSocketChannelEvents {

  private static final String TAG = "WebSocketRTCClient";

  private enum ConnectionState {
    NEW, CONNECTED, CLOSED, ERROR
  }

  ;

//  private enum MessageType {
//    MESSAGE, LEAVE, PING
//  }
//
//  ;

  private final LooperExecutor executor;
  private boolean initiator;

  private WebSocketChannelClient wsClient;
  private WebSocketChannelClient.WebSocketConnectionState socketState;
  private RoomConnectionParameters connectionParameters;

  private SignalingEvents signalingEvents;
  public static int userId;
  public static int userId2;
  public static String userName;
  private int conversationId = -1;
  private String domain;
  private int serviceId;

  public WebSocketRTCClient(SignalingEvents events, LooperExecutor executor) {
    this.executor = executor;
    this.socketState = WebSocketChannelClient.WebSocketConnectionState.NEW;
    this.signalingEvents = events;

    executor.requestStart();
  }

  // --------------------------------------------------------------------
  // WebSocketChannelEvents interface implementation.
  // All events are called by WebSocketChannelClient on a local looper thread
  // (passed to WebSocket client constructor).
  @Override
  public void onWebSocketMessage(final String msg) {
    try {

      JSONObject json = new JSONObject(msg);

      if (json.has("params")) {
        Log.i(TAG, "Got appConfig" + msg + " parsing into roomParameters");
        //this.roomParametersFetcher.parseAppConfig(msg);

        Log.i(TAG, "app config: " + msg);
        try {
          JSONObject appConfig = new JSONObject(msg);

          String result = appConfig.getString("result");
          Log.i(TAG, "client debug ");
          if (!result.equals("SUCCESS")) {
            return;
          }

          String params = appConfig.getString("params");
          appConfig = new JSONObject(params);
          LinkedList<PeerConnection.IceServer> iceServers = iceServersFromPCConfigJSON(appConfig.getString("pc_config"));

          AppRTCClient.SignalingParameters signalingParameters = new SignalingParameters(iceServers);

          wsClient.register(connectionParameters.from);
        } catch (JSONException e) {
          signalingEvents.onChannelError("app config JSON parsing error: " + e.toString());
        }
        return;
      }

//            if (socketState != WebSocketConnectionState.REGISTERED && socketState != WebSocketConnectionState.CONNECTED){
//                Log.e(TAG, "websocket still in non registered state.");
//                return;
//            }


      String id = "";
      String response = "";
      if (json.has("type")) {
        int type = json.getInt("type");
        JSONObject data = json.getJSONObject("data");
        switch (type) {
          case 37:
            //user register
//            JSONObject userT = json.getJSONObject("data");
            userId = data.getInt("userId");
            userName = data.getString("name");
            //make call;
            socketState = WebSocketChannelClient.WebSocketConnectionState.REGISTERED;
//            makeCall();
            signalingEvents.onConnected();
            break;

//          case RESPONSE_SERVICE_LIST:
//            if (data.getServices().isJsonArray()) {
//
//              List<Service> services = GsonWrapper.getGson().fromJson(data.getServices(), new TypeToken<List<Service>>(){}.getType());
//              signalingEvents.onServiceListResponse(services);
//            }
//            break;

          case 72:
            signalingEvents.onAgentJoinConversation(data.getString("fullName"));
            break;

          case 4:
//            JSONObject dataO11 = json.getJSONObject("data");
            conversationId = data.getInt("conversationId");
            signalingEvents.onMessageCome(GsonWrapper.getGson().fromJson(data.toString(), MessageData.class));
            break;

          case 5: // Chat text
            conversationId = data.getInt("conversationId");
            signalingEvents.onConversationReady();
            break;

          case 106:
            signalingEvents.onAgentTyping("", data.getBoolean("isTyping"));
            break;

          case 9:
            conversationId = -1;
            signalingEvents.onEndVideoCallFromWeb();
            break;

          case 71:
            conversationId = -1;
            signalingEvents.onAgentEndConversation(data.getString("fullName"));
            break;

          case 113: //video call processing
//            JSONObject dataO = json.getJSONObject("data");

            String idM = data.getString("id");
            if (idM.equals("joinRoom")) {
              conversationId = data.getInt("conversationId");
              joinRoom();
            }
            if (idM.equals("existingParticipants")) {
              signalingEvents.onReciveCall();
            }
            if (idM.equals("iceCandidate")) {
              //Log.d(TAG, "iceCandidate "+dataO.toString());
              int nameX = data.getInt("name");
              String candidateJ = data.getString("candidate");
              JSONObject candidateJson = new JSONObject(candidateJ);
              String sdpMid = candidateJson.getString("sdpMid");
              int sdpMLineIndex = candidateJson.getInt("sdpMLineIndex");
              String candidateStr = candidateJson.getString("candidate");
              IceCandidate candidate = new IceCandidate(
                  sdpMid,
                  sdpMLineIndex,
                  candidateStr);
              if (nameX == userId) {
                signalingEvents.onRemoteIceCandidate(candidate);
              } else {
                signalingEvents.onRemoteScreenIceCandidate(candidate);
              }
            }
            if (idM.equals("receiveVideoAnswer")) {
              int nameX = data.getInt("name");
              Log.d(TAG, "sending sdpAnswer: " + data.getString("sdpAnswer"));
              SessionDescription sdp = new SessionDescription(
                  SessionDescription.Type.ANSWER, data.getString("sdpAnswer"));

              if (nameX == userId) {
                signalingEvents.onRemoteDescription(sdp);
              } else {
                signalingEvents.onRemoteScreenDescription(sdp);
              }
            }
            if (idM.equals("newParticipantArrived")) {
              userId2 = data.getInt("name");
              Message message = GsonWrapper.getGson().fromJson(msg, Message.class);
              signalingEvents.onIncomingScreenCall(message);
            }

            // End video call
            if (idM.equals("endVideoCall")) {
              signalingEvents.endVideoCall();
            }
            break;
        }
      }
      if (json.has("id")) id = json.getString("id");

      if (id.equals("registerResponse")) {

        response = json.getString("response"); //TODO if not accepted what todo?
        String message = json.getString("message");

        if (response.equals("accepted")) {
          socketState = WebSocketChannelClient.WebSocketConnectionState.REGISTERED;
        } else if (response.equals("rejected")) {
          signalingEvents.onChannelError("register rejected: " + message);
        } else if (response.equals("skipped")) {
          signalingEvents.onChannelError("register rejected: " + message);                                                                       // Log.e(TAG, "registration was skipped because: "+message);
        }
      }

      if (id.equals("registeredUsers")) {
        response = json.getString("response");
        signalingEvents.onUserListUpdate(response);
      }

      if (id.equals("callResponse")) {
        response = json.getString("response");

        if (response.startsWith("rejected")) {
          Log.d(TAG, "call got rejected: " + response);
          signalingEvents.onChannelClose();
        } else {
          Log.d(TAG, "sending sdpAnswer: " + response);
          SessionDescription sdp = new SessionDescription(
              SessionDescription.Type.ANSWER, json.getString("sdpAnswer"));

          signalingEvents.onRemoteDescription(sdp);
        }
      }
      if (id.equals("callScreenResponse")) {
        response = json.getString("response");

        if (response.startsWith("rejected")) {
          Log.d(TAG, "call got rejected: " + response);
          signalingEvents.onChannelScreenClose();
        } else {
          Log.d(TAG, "sending sdpAnswer: " + response);
          SessionDescription sdp = new SessionDescription(
              SessionDescription.Type.ANSWER, json.getString("sdpAnswer"));

          signalingEvents.onRemoteScreenDescription(sdp);
        }
      }

      if (id.equals("incomingCall")) {
        Log.d(TAG, "incomingCall " + json.toString());
        signalingEvents.onIncomingCall(json.getString("from"));
      }

      if (id.equals("incomingScreenCall")) {
        Log.d(TAG, "incomingScreenCall " + json.toString());
        Message message = GsonWrapper.getGson().fromJson(msg, Message.class);
        signalingEvents.onIncomingScreenCall(message);
      }

      if (id.equals("startCommunication")) {
        Log.d(TAG, "startCommunication " + json.toString());
        SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, json.getString("sdpAnswer"));
        signalingEvents.onStartCommunication(sdp);
      }
      if (id.equals("startScreenCommunication")) {
        Log.d(TAG, "startScreenCommunication " + json.toString());
        SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, json.getString("sdpAnswer"));
        // signalingEvents.onStartScreenCommunication(sdp); //remove if not needed!
        signalingEvents.onStartScreenCommunication(sdp);
      }
      if (id.equals("stopCommunication")) {
        Log.d(TAG, "stopCommunication " + json.toString());
        signalingEvents.onChannelClose();
      }
      if (id.equals("stopScreenCommunication")) {
        Log.d(TAG, "stopCommunication " + json.toString());
        signalingEvents.onChannelScreenClose();
      }
      if (id.equals("iceCandidateScreen")) {

        JSONObject candidateJson = json.getJSONObject("candidate");

        IceCandidate candidate = new IceCandidate(
            candidateJson.getString("sdpMid"),
            candidateJson.getInt("sdpMLineIndex"),
            candidateJson.getString("candidate"));

        signalingEvents.onRemoteScreenIceCandidate(candidate);

      }
      if (id.equals("iceCandidate")) {
        Log.d(TAG, "iceCandidate " + json.toString());

        JSONObject candidateJson = json.getJSONObject("candidate");

        IceCandidate candidate = new IceCandidate(
            candidateJson.getString("sdpMid"),
            candidateJson.getInt("sdpMLineIndex"),
            candidateJson.getString("candidate"));

        signalingEvents.onRemoteIceCandidate(candidate);
      }

      if (id.equals("stop")) {
        signalingEvents.onChannelClose();
      }
      if (id.equals("stopScreen")) {
        signalingEvents.onChannelScreenClose();
      }
    } catch (JSONException e) {
      reportError("WebSocket message JSON parsing error: " + e.toString());
    }
  }

  // --------------------------------------------------------------------
  // AppRTCClient interface implementation.
  // Asynchronously connect to an AppRTC room URL using supplied connection
  // parameters, retrieves room parameters and connect to WebSocket server.
  @Override
  public void connectToWebsocket(RoomConnectionParameters connectionParameters) {
    this.connectionParameters = connectionParameters;
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          connectToWebsocketInternal();
        } catch (Exception e) {
          reportError("WebSocketerror: " + e.toString());
        }
      }
    });
  }

  public void sendStopToPeer() {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          JSONObject jsonMessage = new JSONObject();
          jsonPut(jsonMessage, "id", "stop");

          wsClient.send(jsonMessage.toString());
        } catch (Exception e) {
          reportError("WebSocketerror: " + e.toString());
        }
      }
    });
  }

  @Override
  public void sendDisconnectToPeer() {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        endVideoCall();
      }
    });
  }

  @Override
  public void reconnect() {
    executor.execute(new Runnable() {
      @Override
      public void run() {

        endVideoCall();

        try {
          connectToWebsocketInternal();
        } catch (Exception e) {
          reportError("WebSocketerror: " + e.toString());
        }
      }
    });
  }

  @Override
  public void initUser() {
    //get info
    executor.execute(new Runnable() {
      @Override
      public void run() {
        JSONObject data = new JSONObject();
        WebSocketRTCClient.jsonPut(data, "visitorId", -1);
        WebSocketRTCClient.jsonPut(data, "visitorName", "");
        WebSocketRTCClient.jsonPut(data, "ip_address", "192.168.0.132:8939");
        WebSocketRTCClient.jsonPut(data, "host", "192.168.0.132:8939");
        WebSocketRTCClient.jsonPut(data, "os", "");
        WebSocketRTCClient.jsonPut(data, "browser", "");
        WebSocketRTCClient.jsonPut(data, "device_type", "");
        WebSocketRTCClient.jsonPut(data, "domain", domain);
        WebSocketRTCClient.jsonPut(data, "country_name", "");
        JSONObject json = new JSONObject();

        WebSocketRTCClient.jsonPut(json, "service", 37);
        WebSocketRTCClient.jsonPut(json, "data", data);
        wsClient.send(json.toString());
      }
    });

  }

  @Override
  public void makeCall() {
    executor.execute(new Runnable() {
      @Override
      public void run() {

        JSONObject data = new JSONObject();
        WebSocketRTCClient.jsonPut(data, "service_id", serviceId);
        WebSocketRTCClient.jsonPut(data, "conversationId", -1);
        WebSocketRTCClient.jsonPut(data, "message", "");
        WebSocketRTCClient.jsonPut(data, "domain", domain);
        WebSocketRTCClient.jsonPut(data, "id", "requestVideoCall");
        JSONObject json = new JSONObject();
        WebSocketRTCClient.jsonPut(json, "data", data);
        WebSocketRTCClient.jsonPut(json, "service", 113);
        wsClient.send(json.toString());
      }
    });

  }

  @Override
  public void joinRoom() {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        JSONObject data = new JSONObject();
        WebSocketRTCClient.jsonPut(data, "service_id", serviceId);
        WebSocketRTCClient.jsonPut(data, "conversationId", conversationId);
        WebSocketRTCClient.jsonPut(data, "message", "");
        WebSocketRTCClient.jsonPut(data, "domain", domain);
        WebSocketRTCClient.jsonPut(data, "id", "joinRoom");
        JSONObject json = new JSONObject();
        WebSocketRTCClient.jsonPut(json, "data", data);
        WebSocketRTCClient.jsonPut(json, "service", 113);
        wsClient.send(json.toString());
      }
    });
  }

  // Connects to websocket - function runs on a local looper thread.
  private void connectToWebsocketInternal() {

    String connectionUrl = getConnectionUrl(connectionParameters);

    socketState = WebSocketChannelClient.WebSocketConnectionState.NEW;
    wsClient = new WebSocketChannelClient(executor, this);
    wsClient.connect(connectionUrl);
    socketState = WebSocketChannelClient.WebSocketConnectionState.CONNECTED;
    Log.d(TAG, "wsClient connect " + connectionUrl);

  }

  // Disconnect from room and send bye messages - runs on a local looper thread.
  private void endVideoCall() {
    Log.d(TAG, "Disconnect. Room state: " + socketState);
//        if (socketState == WebSocketConnectionState.CONNECTED
//                || socketState == WebSocketConnectionState.NEW
//                || socketState == WebSocketConnectionState.REGISTERED)
    {
      Log.d(TAG, "Closing room.");
      JSONObject data = new JSONObject();
      WebSocketRTCClient.jsonPut(data, "service_id", serviceId);
      WebSocketRTCClient.jsonPut(data, "conversationId", conversationId);
      WebSocketRTCClient.jsonPut(data, "domain", domain);
      WebSocketRTCClient.jsonPut(data, "id", "endVideoCall");
      JSONObject json = new JSONObject();
      WebSocketRTCClient.jsonPut(json, "data", data);
      WebSocketRTCClient.jsonPut(json, "service", 113);
      wsClient.send(json.toString());
      //wsClient.disconnect(true);
    }
  }

  // Helper functions to get connection, sendSocketMessage message and leave message URLs
  private String getConnectionUrl(RoomConnectionParameters connectionParameters) {
    return connectionParameters.roomUrl + "/websocket";
  }

  // Return the list of ICE servers described by a WebRTCPeerConnection
  // configuration string.
  public static LinkedList<PeerConnection.IceServer> iceServersFromPCConfigJSON(String pcConfig) throws JSONException {
    JSONObject json = new JSONObject(pcConfig);
    Log.d(TAG, "current pcConfig: " + pcConfig);
    LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
    JSONArray iceServersArray = json.getJSONArray("iceServers");
    for (int i = 0; i < iceServersArray.length(); i++) {
      JSONObject iceJson = iceServersArray.getJSONObject(i);

      String username = iceJson.getString("username");
      String password = iceJson.getString("password");
      JSONArray iceUris = iceJson.getJSONArray("urls");

      for (int j = 0; j < iceUris.length(); j++) {
        String uri = iceUris.getString(j);
        Log.d(TAG, "adding ice server: " + uri + " username:" + username + " password:" + password);
        iceServers.add(new PeerConnection.IceServer(uri, username, password));
      }
    }
    return iceServers;
  }

  public void call(final SessionDescription sdp) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
//                  if (socketState != WebSocketConnectionState.REGISTERED) {
//                      reportError("Sending offer SDP in non registered state.");
//                      return;
//                  }

        JSONObject json = new JSONObject();
        JSONObject dataJ = new JSONObject();
        jsonPut(dataJ, "service_id", serviceId);
        jsonPut(dataJ, "conversationId", conversationId);
        jsonPut(dataJ, "domain", domain);
        jsonPut(dataJ, "id", "receiveVideoFrom");
        jsonPut(dataJ, "sender", userId);
        jsonPut(dataJ, "sdpOffer", sdp.description);
        jsonPut(json, "service", 113);
        jsonPut(json, "data", dataJ);
        wsClient.send(json.toString());

      }
    });
  }

  // Send local answer SDP to the other participant.
  @Override
  public void sendOfferSdp(final SessionDescription sdp, final boolean isScreenSharing) {
    executor.execute(new Runnable() {
      @Override
      public void run() {

        JSONObject json = new JSONObject();
        JSONObject dataJ = new JSONObject();
        jsonPut(dataJ, "service_id", serviceId);
        jsonPut(dataJ, "conversationId", conversationId);
        jsonPut(dataJ, "domain", domain);
        jsonPut(dataJ, "id", "receiveVideoFrom");
        if (isScreenSharing)
          jsonPut(dataJ, "sender", userId2);
        else {
          jsonPut(dataJ, "sender", userId);
        }
        jsonPut(dataJ, "sdpOffer", sdp.description);
        jsonPut(json, "service", 113);
        jsonPut(json, "data", dataJ);
        wsClient.send(json.toString());

      }
    });
  }

  // Send Ice candidate to the other participant.
  @Override
  public void sendLocalIceCandidate(final IceCandidate candidate, final boolean isScreenSharing) {
    executor.execute(new Runnable() {
      @Override
      public void run() {

        JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject candidateJ = new JSONObject();
        jsonPut(candidateJ, "candidate", candidate.sdp);
        jsonPut(candidateJ, "sdpMid", candidate.sdpMid);
        jsonPut(candidateJ, "sdpMLineIndex", candidate.sdpMLineIndex);
        jsonPut(data, "id", "onIceCandidate");
        if (!isScreenSharing) jsonPut(data, "name", userId);
        else jsonPut(data, "name", userId2);
        jsonPut(data, "service_id", serviceId);
        jsonPut(data, "conversationId", conversationId);
        jsonPut(data, "domain", domain);
        jsonPut(data, "candidate", candidateJ);
        jsonPut(json, "service", 113);
        jsonPut(json, "data", data);
        // Call receiver sends ice candidates to websocket server.
        wsClient.send(json.toString());
      }
    });
  }


  @Override
  public void onWebSocketClose() {
    signalingEvents.onChannelClose();
  }

  @Override
  public void onWebSocketError(String description) {
    reportError("WebSocket error: " + description);
  }

  // --------------------------------------------------------------------
  // Helper functions.
  private void reportError(final String errorMessage) {
    Log.e(TAG, errorMessage);
    executor.execute(new Runnable() {
      @Override
      public void run() {
        if (socketState != WebSocketChannelClient.WebSocketConnectionState.ERROR) {
          socketState = WebSocketChannelClient.WebSocketConnectionState.ERROR;
          signalingEvents.onChannelError(errorMessage);
        }
      }
    });
  }

  @Override
  public void sendEndVideoCall() {
//    mVideoState = endVideoCall;

    executor.execute(new Runnable() {
      @Override
      public void run() {
        endVideoCall();
      }
    });
  }

  @Override
  public void register() {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        MessageData data = new MessageData()
            .setVisitorName("")
            .setServiceId("")
            .setConversationId(-1)
            .setDomain(domain);

        Message message = new Message()
            .setData(data)
            .setService(CUSTOMER_RESUMING_REQUEST);

        wsClient.send(message);
      }
    });
  }

  @Override
  public void login() {
//    executor.execute(new Runnable() {
//      @Override
//      public void run() {
//        MessageData data = new MessageData()
//            .setVisitorName("")
//            .setServiceId("-1")
//            .setHost("10.61.138.224:8939")
//            .setConversationId(-1)
//            .setDomain(domain);
//
//        Message message = new Message()
//            .setData(data)
//            .setService(CUSTOMER_LOGIN);
//
//        wsClient.send(message);
//      }
//    });
  }

  @Override
  public void sendTextMessage(final String messageText) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        MessageData data = new MessageData()
            .setServiceId(serviceId + "")
            .setConversationId(conversationId)
            .setMessage(messageText)
            .setUserId(userId)
            .setVisitorId(userId)
            .setDomain(domain)
            .setHost("messageText");

        Message message = new Message()
            .setService(MESSAGE)
            .setData(data);
        // Call receiver sends ice candidates to websocket server.
        wsClient.send(message);
      }
    });
  }

  @Override
  public void leaveConversation() {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        MessageData data = new MessageData()
            .setVisitorId(userId)
            .setServiceId(serviceId + "")
            .setConversationId(conversationId);

        Message message = new Message()
            .setService(LEAVE_CONVERSATION)
            .setData(data);
        // Call receiver sends ice candidates to websocket server.
        wsClient.send(message);
      }
    });
  }

  @Override
  public void updateUserInfo(final String username, String email) {

    // todo
    executor.execute(new Runnable() {
      @Override
      public void run() {
        MessageData data = new MessageData()
            .setUserId(userId)
            .setUserName(username)
            .setType(PING)
            .setTyping(false)
            .setServiceId("" + serviceId)
            .setConversationId(conversationId);

        Message message = new Message()
            .setService(LEAVE_CONVERSATION)
            .setData(data);
        // Call receiver sends ice candidates to websocket server.
        wsClient.send(message);
      }
    });
  }

  @Override
  public void onWebSocketConnected() {
    connectToUser(0);
  }

  private void connectToUser(int runTimeMs) {
    initTurnServer();
    initUser();
//    login();
    String to = "112";
//    roomConnectionParameters.initiator = true;
//    roomConnectionParameters.to = to;
  }

  private void initTurnServer(){
    String dataJsonTurn="{\n" +
        "    \"params\": {\n" +
        "        \"pc_config\": {\n" +
        "            \"iceServers\": [\n" +
        "                           {\n" +
        "                           \"username\": \"\",\n" +
        "                           \"password\": \"\",\n" +
        "                           \"urls\": [\n" +
        "                                    \"stun:203.190.170.131:49352\"\n" +
        "                                    ]\n" +
        "                           },\n" +
        "                           {\n" +
        "                           \"username\": \"viettel\",\n" +
        "                           \"password\": \"123456aA\",\n" +
        "                           \"urls\": [\n" +
        "                                    \"turn:203.190.170.131:49352\"\n" +
        "                                    ]\n" +
        "                           }\n" +
        "                           ]\n" +
        "        }\n" +
        "    },\n" +
        "    \"result\": \"SUCCESS\"\n" +
        "}\n";
    // PreLive: 203.190.170.131:49352
    // Dev:  10.60.96.57:8488
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

  public WebSocketRTCClient setDomain(String domain) {
    this.domain = domain;
    return this;
  }

  public void setServiceId(int serviceId) {
    this.serviceId = serviceId;
  }

  public void disconnect() {
    if (wsClient != null) {
      wsClient.disconnect();
    }
  }

  public void sendTypingStatus(final boolean typing) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        MessageData data = new MessageData()
            .setServiceId(serviceId + "")
            .setConversationId(conversationId)
            .setUserId(userId)
            .setUserName(userName)
            .setTyping(typing)
            .setType(MessageType.PING)
            .setDomain(domain);

        Message message = new Message()
            .setService(SEND_IS_CHAT_TYPING)
            .setData(data);
        // Call receiver sends ice candidates to websocket server.
        wsClient.send(message);
      }
    });
  }

  // Put a |key|->|value| mapping in |json|.
  public static void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

}
