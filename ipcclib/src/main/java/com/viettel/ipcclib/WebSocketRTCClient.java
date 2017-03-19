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


import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import com.viettel.ipcclib.constants.ConversationId;
import com.viettel.ipcclib.constants.GsonWrapper;
import com.viettel.ipcclib.constants.MessageType;
import com.viettel.ipcclib.model.AppConfig;
import com.viettel.ipcclib.model.CandidateJ;
import com.viettel.ipcclib.model.Message;
import com.viettel.ipcclib.model.MessageData;
import com.viettel.ipcclib.model.Service;
import com.viettel.ipcclib.util.LooperExecutor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import android.text.TextUtils;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import static com.viettel.ipcclib.constants.ConversationId.endVideoCall;
import static com.viettel.ipcclib.constants.ConversationId.existingParticipants;
import static com.viettel.ipcclib.constants.ConversationId.iceCandidate;
import static com.viettel.ipcclib.constants.ConversationId.joinRoom;
import static com.viettel.ipcclib.constants.ConversationId.newParticipantArrived;
import static com.viettel.ipcclib.constants.ConversationId.receiveVideoAnswer;
import static com.viettel.ipcclib.constants.ConversationId.receiveVideoFrom;
import static com.viettel.ipcclib.constants.ConversationId.requestVideoCall;
import static com.viettel.ipcclib.constants.GsonWrapper.getGson;
import static com.viettel.ipcclib.constants.MessageType.CUSTOMER_LOGIN;
import static com.viettel.ipcclib.constants.MessageType.CUSTOMER_RESUMING_REQUEST;
import static com.viettel.ipcclib.constants.MessageType.LEAVE_CONVERSATION;
import static com.viettel.ipcclib.constants.MessageType.MESSAGE;
import static com.viettel.ipcclib.constants.MessageType.PING;
import static com.viettel.ipcclib.constants.MessageType.SEND_IS_CHAT_TYPING;
import static com.viettel.ipcclib.constants.MessageType.VIDEO_CALL;


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

  public WebSocketRTCClient setDomain(String domain) {
    this.domain = domain;
    return this;
  }

  public enum VideoState {
    WAITING, JOINED, LIVE, ERROR, END
  }

  ;

//  private enum MessageType {
//    MESSAGE, LEAVE
//  }

  ;

  private final LooperExecutor executor;
  private boolean initiator;
  private ConversationId mVideoState = endVideoCall;
  private WebSocketChannelClient wsClient;
  private WebSocketChannelClient.WebSocketConnectionState socketState;
  private RoomConnectionParameters connectionParameters;

  private SignalingEvents signalingEvents;
  private int userId;
  private int userId2;
  private String userName;
  private int conversationId = -1;
  private String domain;
  private int serviceId;

  private MessageData agentData;

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
//    try {
    Message message = getGson().fromJson(msg, Message.class);
//      JSONObject json = new JSONObject(msg);

    AppConfig params = message.getParams();
    if (params != null) {
      Log.i(TAG, "Got appConfig" + msg + " parsing into roomParameters");
      //this.roomParametersFetcher.parseAppConfig(msg);

      Log.i(TAG, "app config: " + msg);
      try {
//          JSONObject appConfig = new JSONObject(msg);

        String result = message.getResult();
        Log.i(TAG, "client debug ");
        if (!result.equals("SUCCESS")) {
          return;
        }

//          String params = appConfig.getString("params");
//          appConfig = new JSONObject(params);
        LinkedList<PeerConnection.IceServer> iceServers = iceServersFromPCConfigJSON(params.getPcConfig());

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
    JsonElement typeJson = message.getType();

    if (typeJson == null || message.getData() == null || !TextUtils.isDigitsOnly(typeJson.getAsString())) {
      return;
    }

    String id = "";
    String response = "";

    MessageData data = message.getData();
    MessageType type = MessageType.getValue(typeJson.getAsInt());
    switch (type) {
      case CUSTOMER_LOGIN:
        //user register
//            JSONObject userT = json.getJSONObject("data");
        userId = data.getUserId();
//            userId = userT.getInt("userId");
//            userName = userT.getString("name");
        userName = data.getName().getAsString();
        //make call;
        socketState = WebSocketChannelClient.WebSocketConnectionState.REGISTERED;
        signalingEvents.onConnected();
        // todo use video
//        makeCall();
        break;

      case AGENT_INFO_CUSTOMER:
        agentData = message.getData();
        signalingEvents.onAgentJoinConversation(agentData.getFullName());
        break;

      case HAVE_MESSAGE:
        signalingEvents.onMessageCome(data);
        break;

      case MESSAGE:
        conversationId = message.getData().getConversationId();
        signalingEvents.onConversationReady();
        break;

      case SEND_IS_CHAT_TYPING:
        signalingEvents.onAgentTyping(data.getFullName(), data.isTyping());
        break;

      case RESPONSE_AGENT_MISS_CHAT_TO_CUSTOMER:
        signalingEvents.onAgentMissedChat();
        break;

      case VIDEO_CALL: //video call processing
//            JSONObject dataO = json.getJSONObject("data");
        processVideoCall(message);
        break;

      case AGENT_END_CONVERSATION_CUSTOMER:
        conversationId = -1;
        signalingEvents.onAgentEndConversation(data.getFullName());
        break;

      case RESPONSE_SERVICE_LIST:
        if (data.getServices().isJsonArray()) {

          List<Service> services = GsonWrapper.getGson().fromJson(data.getServices(), new TypeToken<List<Service>>(){}.getType());
          signalingEvents.onServiceListResponse(services);
        }
        break;
    }
            /*
            if(json.has("id")) id = json.getString("id");

            if(id.equals("registerResponse")){

                response = json.getString("response"); //TODO if not accepted what todo?
                String message = json.getString("message");

                if(response.equals("accepted"))      {
                    socketState = WebSocketConnectionState.REGISTERED;
                }

                else if(response.equals("rejected"))      {
                    signalingEvents.onChannelError("register rejected: " + message);
                }

                else if(response.equals("skipped")) {
                    signalingEvents.onChannelError("register rejected: " + message);                                                                       // Log.e(TAG, "registration was skipped because: "+message);
                }
            }

            if(id.equals("registeredUsers")){
                response = json.getString("response");
                signalingEvents.onUserListUpdate(response);
            }

            if(id.equals("callResponse")){
                response = json.getString("response");

                if(response.startsWith("rejected")) {
                    Log.d(TAG, "call got rejected: "+response);
                    signalingEvents.onChannelClose();
                }else{
                    Log.d(TAG, "sending sdpAnswer: "+response);
                    SessionDescription sdp = new SessionDescription(
                            SessionDescription.Type.ANSWER,json.getString("sdpAnswer"));

                    signalingEvents.onRemoteDescription(sdp);
                }
            }
            if(id.equals("callScreenResponse")){
                response = json.getString("response");

                if(response.startsWith("rejected")) {
                    Log.d(TAG, "call got rejected: "+response);
                    signalingEvents.onChannelScreenClose();
                }else{
                    Log.d(TAG, "sending sdpAnswer: "+response);
                    SessionDescription sdp = new SessionDescription(
                            SessionDescription.Type.ANSWER,json.getString("sdpAnswer"));

                    signalingEvents.onRemoteScreenDescription(sdp);
                }
            }

            if(id.equals("incomingCall")){
                Log.d(TAG, "incomingCall "+json.toString());
                signalingEvents.onIncomingCall(json.getString("from"));
            }

            if(id.equals("incomingScreenCall")){
                Log.d(TAG, "incomingScreenCall "+json.toString());
                signalingEvents.onIncomingScreenCall(json);
            }

            if(id.equals("startCommunication")){
                Log.d(TAG, "startCommunication "+json.toString());
                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER,json.getString("sdpAnswer"));
                signalingEvents.onStartCommunication(sdp);
            }
            if(id.equals("startScreenCommunication")){
                Log.d(TAG, "startScreenCommunication "+json.toString());
                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER,json.getString("sdpAnswer"));
                   // signalingEvents.onStartScreenCommunication(sdp); //remove if not needed!
                signalingEvents.onStartScreenCommunication(sdp);
            }
            if(id.equals("stopCommunication")){
                Log.d(TAG, "stopCommunication "+json.toString());
                signalingEvents.onChannelClose();
            }
            if(id.equals("stopScreenCommunication")){
                Log.d(TAG, "stopCommunication "+json.toString());
                signalingEvents.onChannelScreenClose();
            }
            if(id.equals("iceCandidateScreen")){

                JSONObject candidateJson = json.getJSONObject("candidate");

                IceCandidate candidate = new IceCandidate(
                        candidateJson.getString("sdpMid"),
                        candidateJson.getInt("sdpMLineIndex"),
                        candidateJson.getString("candidate"));

                signalingEvents.onRemoteScreenIceCandidate(candidate);

            }
            if(id.equals("iceCandidate")){
                Log.d(TAG, "iceCandidate "+json.toString());

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
            */
//    } catch (JSONException e) {
//      reportError("WebSocket message JSON parsing error: " + e.toString());
//    }
  }

  private void processVideoCall(Message message) {
    MessageData dataO = message.getData();
    if (dataO == null) {
      return;
    }

    ConversationId idM = dataO.getId();
    if (idM == null) {
      return;
    }
    switch (idM) {
      case endVideoCall:
        mVideoState = endVideoCall;
        if (socketState == WebSocketChannelClient.WebSocketConnectionState.NEW) {
          signalingEvents.onNoAgentResponse();
        } else {
          signalingEvents.endVideoCall();
        }
        break;

      case joinRoom:
        mVideoState = joinRoom;
        conversationId = dataO.getConversationId();
        joinRoom();
        break;

      case existingParticipants:
        mVideoState = existingParticipants;
        signalingEvents.onReciveCall();
        break;

      case iceCandidate:
        mVideoState = iceCandidate;
        //Log.d(TAG, "iceCandidate "+dataO.toString());
        int name = dataO.getName().getAsInt();
//              int nameX = dataO.getInt("name");
//              String candidateJ = dataO.getString("candidate");
//              String candidateJ = dataO.getCandidate();
        String candidateString = dataO.getCandidate();
        CandidateJ candidateJ = GsonWrapper.getGson().fromJson(candidateString, CandidateJ.class);
//              JSONObject candidateJson = new JSONObject(candidateJ);
        String sdpMid = candidateJ.getSdpMid();
        int sdpMLineIndex = candidateJ.getSdpMLineIndex();
        String candidateStr = candidateJ.getCandidate();
//              String sdpMid = candidateJson.getString("sdpMid");
//              int sdpMLineIndex = candidateJson.getInt("sdpMLineIndex");
//              String candidateStr = candidateJson.getString("candidate");
        IceCandidate candidate = new IceCandidate(
            sdpMid,
            sdpMLineIndex,
            candidateStr);
        if (name == userId) {
          signalingEvents.onRemoteIceCandidate(candidate);
        } else {
          signalingEvents.onRemoteScreenIceCandidate(candidate);
        }
        break;

      case receiveVideoAnswer:
        mVideoState = receiveVideoAnswer;
        int nameX = dataO.getName().getAsInt();
//              Log.d(TAG, "sending sdpAnswer: " + dataO.getString("sdpAnswer"));
        SessionDescription sdp = new SessionDescription(
//                  SessionDescription.Type.ANSWER, dataO.getString("sdpAnswer"));
            SessionDescription.Type.ANSWER, dataO.getSdpAnswer());

        if (nameX == userId) {
          signalingEvents.onRemoteDescription(sdp);
        } else {
          signalingEvents.onRemoteScreenDescription(sdp);
        }
        break;

      case newParticipantArrived:
        mVideoState = newParticipantArrived;
        userId2 = dataO.getName().getAsInt();
        signalingEvents.onIncomingScreenCall(message);
//              userId2 = dataO.getInt("name");
//              signalingEvents.onIncomingScreenCall(json);
        break;
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

  public void setServiceId(int serviceId) {
    this.serviceId = serviceId;
  }

  public void sendStopToPeer() {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
//          JSONObject jsonMessage = new JSONObject();
//          jsonPut(jsonMessage, "id", "stop");
          Message message = new Message()
              .setId("stop");

          wsClient.send(message);
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
        disconnectFromRoomInternal();
      }
    });
  }

  @Override
  public void reconnect() {
    executor.execute(new Runnable() {
      @Override
      public void run() {

        disconnectFromRoomInternal();

        try {
          connectToWebsocketInternal();
        } catch (Exception e) {
          reportError("WebSocketerror: " + e.toString());
        }
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
  public void initUser() {
    //get info
    executor.execute(new Runnable() {
      @Override
      public void run() {
//        JSONObject data = new JSONObject();
//        WebSocketRTCClient.jsonPut(data, "visitorId", -1);
//        WebSocketRTCClient.jsonPut(data, "visitorName", "");
//        WebSocketRTCClient.jsonPut(data, "ip_address", "192.168.0.132:8939");
//        WebSocketRTCClient.jsonPut(data, "host", "192.168.0.132:8939");
//        WebSocketRTCClient.jsonPut(data, "os", "");
//        WebSocketRTCClient.jsonPut(data, "browser", "");
//        WebSocketRTCClient.jsonPut(data, "device_type", "");
//        WebSocketRTCClient.jsonPut(data, "domain", "LANT_TEST1");
//        WebSocketRTCClient.jsonPut(data, "country_name", "");
//        JSONObject json = new JSONObject();
//
//        WebSocketRTCClient.jsonPut(json, "service", 37);
//        WebSocketRTCClient.jsonPut(json, "data", data);

        // Todo question
        MessageData data = new MessageData()
            .setVisitorId(-1)
            .setVisitorName("")
            .setIpAddress("192.168.0.132:8939")
            .setHost("192.168.0.132:8939")
            .setOs("")
            .setBrowser("")
            .setDeviceType("")
            .setConversationId(-1)
            .setDomain(domain)
            .setCountryName("");

        Message message = new Message()
            .setData(data)
            .setService(CUSTOMER_LOGIN);

        wsClient.send(message);
      }
    });

  }

  @Override
  public void makeCall() {
    executor.execute(new Runnable() {
      @Override
      public void run() {

        MessageData data = new MessageData()
            .setServiceId(serviceId + "")
            .setConversationId(conversationId)
            .setMessage("")
            .setDomain(domain)
            .setId(requestVideoCall);
        Message message = new Message()
            .setData(data)
            .setService(VIDEO_CALL);
//        JSONObject data = new JSONObject();
//        WebSocketRTCClient.jsonPut(data, "service_id", serviceId);
//        WebSocketRTCClient.jsonPut(data, "conversationId", -1);
//        WebSocketRTCClient.jsonPut(data, "message", "");
//        WebSocketRTCClient.jsonPut(data, "domain", 14);
//        WebSocketRTCClient.jsonPut(data, "id", "requestVideoCall");
//        JSONObject json = new JSONObject();
//        WebSocketRTCClient.jsonPut(json, "data", data);
//        WebSocketRTCClient.jsonPut(json, "service", 113);
//        wsClient.send(json.toString());
        wsClient.send(message);
      }
    });

  }

  @Override
  public void joinRoom() {
    executor.execute(new Runnable() {
      @Override
      public void run() {
//        JSONObject data = new JSONObject();
//        WebSocketRTCClient.jsonPut(data, "service_id", serviceId);
//        WebSocketRTCClient.jsonPut(data, "conversationId", conversationId);
//        WebSocketRTCClient.jsonPut(data, "message", "");
//        WebSocketRTCClient.jsonPut(data, "domain", 14);
//        WebSocketRTCClient.jsonPut(data, "id", "joinRoom");
//        JSONObject json = new JSONObject();
//        WebSocketRTCClient.jsonPut(json, "data", data);
//        WebSocketRTCClient.jsonPut(json, "service", 113);

        // todo question
        MessageData data = new MessageData()
            .setServiceId(serviceId + "")
            .setConversationId(conversationId)
            .setMessage("")
            .setDomain(domain)
            .setId(joinRoom);

        Message message = new Message()
            .setData(data)
            .setService(VIDEO_CALL);

        wsClient.send(message);
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
  private void disconnectFromRoomInternal() {
    Log.d(TAG, "Disconnect. Room state: " + socketState);
//        if (socketState == WebSocketConnectionState.CONNECTED
//                || socketState == WebSocketConnectionState.NEW
//                || socketState == WebSocketConnectionState.REGISTERED)
    {
      Log.d(TAG, "Closing room.");
//      JSONObject data = new JSONObject();
//      WebSocketRTCClient.jsonPut(data, "service_id", serviceId);
//      WebSocketRTCClient.jsonPut(data, "conversationId", conversationId);
//      WebSocketRTCClient.jsonPut(data, "domain", 14);
//      WebSocketRTCClient.jsonPut(data, "id", "endVideoCall");
//      JSONObject json = new JSONObject();
//      WebSocketRTCClient.jsonPut(json, "data", data);
//      WebSocketRTCClient.jsonPut(json, "service", 113);

      // todo
      MessageData data = new MessageData()
          .setServiceId(serviceId + "")
          .setConversationId(conversationId)
          .setDomain(domain)
          .setId(endVideoCall);

      Message message = new Message()
          .setData(data)
          .setService(VIDEO_CALL);

      wsClient.send(message);
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

//        JSONObject json = new JSONObject();
//        JSONObject dataJ = new JSONObject();
//        jsonPut(dataJ, "service_id", serviceId);
//        jsonPut(dataJ, "conversationId", conversationId);
//        jsonPut(dataJ, "domain", 14);
//        jsonPut(dataJ, "id", "receiveVideoFrom");
//        jsonPut(dataJ, "sender", userId);
//        jsonPut(dataJ, "sdpOffer", sdp.description);
//        jsonPut(json, "service", 113);
//        jsonPut(json, "data", dataJ);

        MessageData data = new MessageData()
            .setServiceId("" + serviceId)
            .setConversationId(conversationId)
            .setDomain(domain)
            .setId(receiveVideoFrom)
            .setSender(userId + "")
            .setSdpOffer(sdp.description);

        // todo
        Message message = new Message()
            .setData(data)
            .setService(VIDEO_CALL);

        wsClient.send(message);

      }
    });
  }

  // Send local answer SDP to the other participant.
  @Override
  public void sendOfferSdp(final SessionDescription sdp, final boolean isScreenSharing) {
//    if (mVideoState == endVideoCall || mVideoState == cancelVideoCall ) {
//      return;
//    }
    executor.execute(new Runnable() {
      @Override
      public void run() {

//        JSONObject json = new JSONObject();
//        JSONObject dataJ = new JSONObject();
//        jsonPut(dataJ, "service_id", serviceId);
//        jsonPut(dataJ, "conversationId", conversationId);
//        jsonPut(dataJ, "domain", 14);
//        jsonPut(dataJ, "id", "receiveVideoFrom");
//        if (isScreenSharing)
//          jsonPut(dataJ, "sender", userId2);
//        else {
//          jsonPut(dataJ, "sender", userId);
//        }
//        jsonPut(dataJ, "sdpOffer", sdp.description);
//        jsonPut(json, "service", 113);
//        jsonPut(json, "data", dataJ);

        MessageData data = new MessageData()
            .setServiceId(serviceId + "")
            .setConversationId(conversationId)
            .setDomain(domain)
            .setId(receiveVideoFrom)
            .setSdpOffer(sdp.description);

        if (isScreenSharing) {
          data.setSender(userId2 + "");
        } else {
          data.setSender(userId + "");
        }

        Message message = new Message()
            .setService(VIDEO_CALL)
            .setData(data);
        wsClient.send(message);

      }
    });
  }

  // Send Ice candidate to the other participant.
  @Override
  public void sendLocalIceCandidate(final IceCandidate candidate, final boolean isScreenSharing) {
//    if (mVideoState == endVideoCall || mVideoState == existingParticipants || mVideoState == cancelVideoCall) {
//      return;
//    }
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

//        CandidateJ candidateJ = new CandidateJ()
//            .setCandidate(candidate.sdp)
//            .setSdpMid(candidate.sdpMid)
//            .setSdpMLineIndex(candidate.sdpMLineIndex);
//
//        JsonObject candidateJson = new JsonObject();
//        candidateJson.addProperty("candidate", candidate.sdp);
//        candidateJson.addProperty("sdpMid", candidate.sdpMid);
//        candidateJson.addProperty("sdpMLineIndex", candidate.sdpMLineIndex);
//
//        MessageData data = new MessageData()
//            .setId(onIceCandidate)
//            .setServiceId(serviceId + "")
//            .setConversationId(conversationId)
//            .setDomain(domain)
//            .setCandidate(getGson().toJson(candidateJson))
//            ;
//        if (!isScreenSharing) {
//          data.setName(getGson().toJsonTree(userId));
//        } else {
//          data.setName(getGson().toJsonTree(userId2));
//        }

//        Message message = new Message()
//            .setService(VIDEO_CALL)
//            .setData(data);
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

  @Override
  public void onWebSocketConnected() {
    connectToUser(0);
  }


  public void disconnect() {
    if (wsClient != null) {
      wsClient.disconnect();
    }
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
        "                                    \"stun:10.60.96.56:8478?transport=udp\",\n" +
        "                                    \"stun:10.60.96.56:8478?transport=tcp\",\n" +
        "                                    \"stun:stun.l.google.com:19302\",\n" +
        "                                    \"stun:stun1.l.google.com:19302\",\n" +
        "                                    \"stun:stun2.l.google.com:19302\",\n" +
        "                                    \"stun:stun3.l.google.com:19302\",\n" +
        "                                    \"stun:stun4.l.google.com:19302\",\n" +
        "                                    \"stun:stun.ekiga.net\",\n" +
        "                                    \"stun:stun.ideasip.com\",\n" +
        "                                    \"stun:stun.schlund.de\",\n" +
        "                                    \"stun:stun.voiparound.com\",\n" +
        "                                    \"stun:stun.voipbuster.com\",\n" +
        "                                    \"stun:stun.voipstunt.com\",\n" +
        "                                    \"stun:stun.voxgratia.org\",\n" +
        "                                    \"stun:stun.services.mozilla.com\"\n" +
        "                                    ]\n" +
        "                           },\n" +
        "                           {\n" +
        "                           \"username\": \"viettel\",\n" +
        "                           \"password\": \"123456aA\",\n" +
        "                           \"urls\": [\n" +
        "                                    \"turn:10.60.96.56:8478\"\n" +
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

  @Override
  public void leaveConversation() {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        MessageData data = new MessageData()
            .setVisitorId(userId)
            .setServiceId("-1")
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

  // Put a |key|->|value| mapping in |json|.
  public static void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

}
