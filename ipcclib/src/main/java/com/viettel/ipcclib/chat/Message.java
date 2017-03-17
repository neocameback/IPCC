package com.viettel.ipcclib.chat;

/**
 * Message of a user in a time
 * Created by Alan's on 3/16/2017.
 */

public class Message {
  private long id;
  private long userId;
  private String content;
  private String time;

  public Message() {
  }

  public Message(long id, long userId, String content) {
    this.id = id;
    this.userId = userId;
    this.content = content;
  }

  public Message(long id, long userId, String content, String time) {
    this.id = id;
    this.userId = userId;
    this.content = content;
    this.time = time;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public boolean isOwner() {
    //// TODO: 3/16/2017  check if this message is created by this user
    //fake
    if (userId % 2 == 0)
      return false;
    return true;
  }
}
