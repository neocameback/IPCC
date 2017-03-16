package de.lespace.apprtc.model;

import de.lespace.apprtc.constants.MessageType;

/**
 * Created by Macbook on 3/16/17.
 */

public class Message {
  private MessageType service;
  private MessageData data;
  private MessageType type;
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

  public MessageType getType() {
    return type;
  }

  public Message setType(MessageType type) {
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
