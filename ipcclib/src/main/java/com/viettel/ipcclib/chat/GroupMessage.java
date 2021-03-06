package com.viettel.ipcclib.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Sequence of one time chat of an user
 * Created by Administrator on 3/16/2017.
 */

public class GroupMessage {
  private String mUserAvatar;
  private List<Message> mMessages;

  public GroupMessage() {
    mMessages = new ArrayList<>();
  }

  public GroupMessage(String mUserAvatar, List<Message> mMessages) {
    this.mUserAvatar = mUserAvatar;
    this.mMessages = new ArrayList<>();
    this.mMessages.addAll(mMessages);
  }

  public void addMessage(Message m) {
    mMessages.add(m);
  }

  public String getmUserAvatar() {
    return mUserAvatar;
  }

  public void setmUserAvatar(String mUserAvatar) {
    this.mUserAvatar = mUserAvatar;
  }

  public List<Message> getmMessages() {
    return mMessages;
  }

  public void setmMessages(List<Message> mMessages) {
    this.mMessages = mMessages;
  }

  public boolean isOwner() {
    if (mMessages.get(0) != null)
      return mMessages.get(0).getType() == Message.Type.MINE;
    return false;
  }
}
