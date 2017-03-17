package com.viettel.ipcclib.chat;

import java.util.Date;

/**
 * Message of a user in a time
 * Created by Alan's on 3/16/2017.
 */

public class Message {
  private Type mType;
  private CharSequence content;
  private Date time;

  public Message(CharSequence content, Date time, Type type) {
    this.content = content;
    this.time = time;
    mType = type;
  }

  public CharSequence getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public Type getType() {
    return mType;
  }

  public Message setType(Type type) {
    mType = type;
    return this;
  }


  public enum Type {
    MINE, OTHER, NOTICE
  }
}
