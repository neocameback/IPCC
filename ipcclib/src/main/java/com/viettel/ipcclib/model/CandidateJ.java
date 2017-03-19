package com.viettel.ipcclib.model;

/**
 * Created by Macbook on 3/16/17.
 */

public class CandidateJ {
  private String candidate;
  private String sdpMid;
  private int sdpMLineIndex;

  public String getCandidate() {
    return candidate;
  }

  public CandidateJ setCandidate(String candidate) {
    this.candidate = candidate;
    return this;
  }

  public String getSdpMid() {
    return sdpMid;
  }

  public CandidateJ setSdpMid(String sdpMid) {
    this.sdpMid = sdpMid;
    return this;
  }

  public int getSdpMLineIndex() {
    return sdpMLineIndex;
  }

  public CandidateJ setSdpMLineIndex(int sdpMLineIndex) {
    this.sdpMLineIndex = sdpMLineIndex;
    return this;
  }
}
