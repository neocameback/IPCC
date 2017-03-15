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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import android.util.Log;

import java.util.LinkedList;

import de.lespace.apprtc.WebSocketChannelClient.WebSocketChannelEvents;
import de.lespace.apprtc.WebSocketChannelClient.WebSocketConnectionState;
import de.lespace.apprtc.util.LooperExecutor;

/**
 * Negotiates signaling for chatting with apprtc.appspot.com "rooms".
 *
 * <p>To use: create an instance of this object (registering a message handler) and
 * call connectToWebsocket().  Once room connection is established
 * onConnectedToRoom() callback with room parameters is invoked.
 * Messages to other party (with local Ice candidates and answer SDP) can
 * be sent after WebSocket connection is established.
 */
public class WebSocketRTCClient implements AppRTCClient, WebSocketChannelEvents {

    private static final String TAG = "WebSocketRTCClient";
    private enum ConnectionState {
        NEW, CONNECTED, CLOSED, ERROR
    };

    private enum MessageType {
        MESSAGE, LEAVE
    };

    private final LooperExecutor executor;
    private boolean initiator;

    private WebSocketChannelClient wsClient;
    private WebSocketConnectionState socketState;
    private RoomConnectionParameters connectionParameters;

    private SignalingEvents signalingEvents;
    public static  int userId;
    public static int userId2;
    public static String userName;
    public static int conversationId;

    public WebSocketRTCClient(SignalingEvents events, LooperExecutor executor) {
        this.executor = executor;
        this.socketState = WebSocketConnectionState.NEW;
        this.signalingEvents = events;

     executor.requestStart();
  }
    // --------------------------------------------------------------------
    // WebSocketChannelEvents interface implementation.
    // All events are called by WebSocketChannelClient on a local looper thread
    // (passed to WebSocket client constructor).
    @Override
    public void onWebSocketMessage(final String msg){
        try {

            JSONObject json = new JSONObject(msg);

            if (json.has("params")) {
                Log.i(TAG, "Got appConfig"+msg+" parsing into roomParameters");
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
            if(json.has("type")){
                int type =json.getInt("type");
                switch (type){
                    case 37:
                        //user register
                        JSONObject userT= json.getJSONObject("data");
                        userId = userT.getInt("userId");
                        userName= userT.getString("name");
                        //make call;
                        socketState = WebSocketConnectionState.REGISTERED;
                        makeCall();
                        break;

                    case 113: //video call processing
                        JSONObject dataO= json.getJSONObject("data");

                        String idM= dataO.getString("id");
                        if(idM.equals("joinRoom")){
                            conversationId=dataO.getInt("conversationId");
                            joinRoom();
                        }
                        if(idM.equals("existingParticipants")) {
                            signalingEvents.onReciveCall();
                        }
                        if(idM.equals("iceCandidate")){
                            //Log.d(TAG, "iceCandidate "+dataO.toString());
                            int nameX= dataO.getInt("name");
                            String candidateJ=dataO.getString("candidate");
                            JSONObject candidateJson = new JSONObject(candidateJ);
                            String sdpMid=candidateJson.getString("sdpMid");
                            int sdpMLineIndex=candidateJson.getInt("sdpMLineIndex");
                            String candidateStr=candidateJson.getString("candidate");
                            IceCandidate candidate = new IceCandidate(
                                    sdpMid,
                                    sdpMLineIndex,
                                    candidateStr);
                            if(nameX==userId) {
                                signalingEvents.onRemoteIceCandidate(candidate);
                            }else{
                                signalingEvents.onRemoteScreenIceCandidate(candidate);
                            }
                        }
                        if(idM.equals("receiveVideoAnswer")){
                            int nameX= dataO.getInt("name");
                            Log.d(TAG, "sending sdpAnswer: "+dataO.getString("sdpAnswer"));
                            SessionDescription sdp = new SessionDescription(
                                    SessionDescription.Type.ANSWER,dataO.getString("sdpAnswer"));

                            if(nameX==userId) {
                                signalingEvents.onRemoteDescription(sdp);
                            }else{
                                signalingEvents.onRemoteScreenDescription(sdp);
                            }
                        }
                        if(idM.equals("newParticipantArrived")){
                            userId2 = dataO.getInt("name");
                            signalingEvents.onIncomingScreenCall(json);
                        }
                        break;
                }
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
          }catch(Exception e){
              reportError("WebSocketerror: " + e.toString());
          }
      }
    });
  }

    public void sendStopToPeer(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonMessage = new JSONObject();
                    jsonPut(jsonMessage, "id" , "stop");

                    wsClient.send(jsonMessage.toString());
                }catch(Exception e){
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
                }catch(Exception e){
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
                WebSocketRTCClient.jsonPut(data,"visitorId",-1);
                WebSocketRTCClient.jsonPut(data,"visitorName","");
                WebSocketRTCClient.jsonPut(data,"ip_address","192.168.0.132:8939");
                WebSocketRTCClient.jsonPut(data, "host", "192.168.0.132:8939");
                WebSocketRTCClient.jsonPut(data, "os", "");
                WebSocketRTCClient.jsonPut(data, "browser", "");
                WebSocketRTCClient.jsonPut(data, "device_type", "");
                WebSocketRTCClient.jsonPut(data, "domain","LANT_TEST1");
                WebSocketRTCClient.jsonPut(data, "country_name", "");
                JSONObject json = new JSONObject();

                WebSocketRTCClient.jsonPut(json,"service",37);
                WebSocketRTCClient.jsonPut(json,"data",data);
                wsClient.send(json.toString());
            }
        });

    }

    @Override
    public void makeCall() {
        executor.execute(new Runnable() {
            @Override
            public void run() {

                JSONObject data= new JSONObject();
                WebSocketRTCClient.jsonPut(data,"service_id",28);
                WebSocketRTCClient.jsonPut(data,"conversationId",-1);
                WebSocketRTCClient.jsonPut(data,"message","");
                WebSocketRTCClient.jsonPut(data,"domain",14);
                WebSocketRTCClient.jsonPut(data,"id","requestVideoCall");
                JSONObject json= new JSONObject();
                WebSocketRTCClient.jsonPut(json,"data",data);
                WebSocketRTCClient.jsonPut(json,"service",113);
                wsClient.send(json.toString());
            }
        });

    }

    @Override
    public void joinRoom() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                JSONObject data= new JSONObject();
                WebSocketRTCClient.jsonPut(data,"service_id",28);
                WebSocketRTCClient.jsonPut(data,"conversationId",conversationId);
                WebSocketRTCClient.jsonPut(data,"message","");
                WebSocketRTCClient.jsonPut(data,"domain",14);
                WebSocketRTCClient.jsonPut(data,"id","joinRoom");
                JSONObject json= new JSONObject();
                WebSocketRTCClient.jsonPut(json,"data",data);
                WebSocketRTCClient.jsonPut(json,"service",113);
                wsClient.send(json.toString());
            }
        });
    }

    // Connects to websocket - function runs on a local looper thread.
      private void connectToWebsocketInternal() {

          String connectionUrl = getConnectionUrl(connectionParameters);

          socketState = WebSocketConnectionState.NEW;
          wsClient = new WebSocketChannelClient(executor, this);
          wsClient.connect(connectionUrl);
          socketState = WebSocketConnectionState.CONNECTED;
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
            JSONObject data= new JSONObject();
            WebSocketRTCClient.jsonPut(data,"service_id",28);
            WebSocketRTCClient.jsonPut(data,"conversationId",conversationId);
            WebSocketRTCClient.jsonPut(data,"domain",14);
            WebSocketRTCClient.jsonPut(data,"id","endVideoCall");
            JSONObject json= new JSONObject();
            WebSocketRTCClient.jsonPut(json,"data",data);
            WebSocketRTCClient.jsonPut(json,"service",113);
            wsClient.send(json.toString());
            //wsClient.disconnect(true);
        }
      }

      // Helper functions to get connection, sendSocketMessage message and leave message URLs
      private String getConnectionUrl(RoomConnectionParameters connectionParameters) {
            return connectionParameters.roomUrl+"/websocket";
      }

    // Return the list of ICE servers described by a WebRTCPeerConnection
    // configuration string.
    public static LinkedList<PeerConnection.IceServer> iceServersFromPCConfigJSON(String pcConfig) throws JSONException {
        JSONObject json = new JSONObject(pcConfig);
        Log.d(TAG, "current pcConfig: " + pcConfig);
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
        JSONArray iceServersArray = json.getJSONArray("iceServers");
        for (int i = 0; i < iceServersArray.length(); i++) {
            JSONObject iceJson  = iceServersArray.getJSONObject(i);

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

      public void call(final SessionDescription sdp)  {
          executor.execute(new Runnable() {
              @Override
              public void run() {
//                  if (socketState != WebSocketConnectionState.REGISTERED) {
//                      reportError("Sending offer SDP in non registered state.");
//                      return;
//                  }

                  JSONObject json = new JSONObject();
                  JSONObject dataJ = new JSONObject();
                  jsonPut(dataJ,"service_id",28);
                  jsonPut(dataJ,"conversationId",conversationId);
                  jsonPut(dataJ,"domain",14);
                  jsonPut(dataJ,"id","receiveVideoFrom");
                  jsonPut(dataJ,"sender",userId);
                  jsonPut(dataJ, "sdpOffer", sdp.description);
                  jsonPut(json,"service",113);
                  jsonPut(json,"data",dataJ);
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
              jsonPut(dataJ,"service_id",28);
              jsonPut(dataJ,"conversationId",conversationId);
              jsonPut(dataJ,"domain",14);
              jsonPut(dataJ,"id","receiveVideoFrom");
              if(isScreenSharing)
              jsonPut(dataJ,"sender",userId2);else{
                  jsonPut(dataJ,"sender",userId);
              }
              jsonPut(dataJ, "sdpOffer", sdp.description);
              jsonPut(json,"service",113);
              jsonPut(json,"data",dataJ);
              wsClient.send(json.toString());

          }
        });
      }

      // Send Ice candidate to the other participant.
      @Override
      public void sendLocalIceCandidate(final IceCandidate candidate,  final boolean isScreenSharing) {
        executor.execute(new Runnable() {
          @Override
          public void run() {

            JSONObject json = new JSONObject();
              JSONObject data= new JSONObject();
              JSONObject candidateJ =new JSONObject();
              jsonPut(candidateJ, "candidate", candidate.sdp);
              jsonPut(candidateJ, "sdpMid", candidate.sdpMid);
              jsonPut(candidateJ, "sdpMLineIndex", candidate.sdpMLineIndex);
              jsonPut(data, "id", "onIceCandidate");
              if(!isScreenSharing)  jsonPut(data,"name",userId);
              else   jsonPut(data,"name",userId2);
              jsonPut(data, "service_id",28);
              jsonPut(data, "conversationId",conversationId);
              jsonPut(data, "domain",14);
              jsonPut(data,"candidate",candidateJ);
              jsonPut(json,"service",113);
              jsonPut(json,"data",data);
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
        if (socketState != WebSocketConnectionState.ERROR) {
                socketState = WebSocketConnectionState.ERROR;
                signalingEvents.onChannelError(errorMessage);
        }
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
