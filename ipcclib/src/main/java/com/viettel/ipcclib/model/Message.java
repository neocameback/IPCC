package com.viettel.ipcclib.model;

import com.google.gson.JsonElement;

import com.viettel.ipcclib.constants.MessageType;

/**
 * Created by Macbook on 3/16/17.
 */

public class Message {
  private MessageType service;
  private MessageData data;
  private JsonElement type;
  private AppConfig params;
  private String result;
  private String id;

  public MessageType getService() {
    return service;
  }

  public Message setService(MessageType service) {
    this.service = service;
    return this;
  }

  public MessageData getData() {
    return data;
  }

  public Message setData(MessageData data) {
    this.data = data;
    return this;
  }

  public JsonElement getType() {
    return type;
  }

  public Message setType(JsonElement type) {
    this.type = type;
    return this;
  }

  public AppConfig getParams() {
    return params;
  }

  public Message setParams(AppConfig params) {
    this.params = params;
    return this;
  }

  public String getResult() {
    return result;
  }

  public Message setResult(String result) {
    this.result = result;
    return this;
  }

  public String getId() {
    return id;
  }

  public Message setId(String id) {
    this.id = id;
    return this;
  }
}
