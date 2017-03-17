package com.viettel.ipcclib.constants;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Macbook on 3/16/17.
 */
public enum ConversationId {
  @SerializedName("endVideoCall")
  endVideoCall,
  @SerializedName("joinRoom")
  joinRoom,
  @SerializedName("existingParticipants")
  existingParticipants,
  @SerializedName("iceCandidate")
  iceCandidate,
  @SerializedName("receiveVideoAnswer")
  receiveVideoAnswer,
  @SerializedName("newParticipantArrived")
  newParticipantArrived,
  @SerializedName("requestVideoCall")
  requestVideoCall,
  @SerializedName("receiveVideoFrom")
  receiveVideoFrom,
  @SerializedName("onIceCandidate")
  onIceCandidate,
  @SerializedName("cancelVideoCall")
  cancelVideoCall,
}
