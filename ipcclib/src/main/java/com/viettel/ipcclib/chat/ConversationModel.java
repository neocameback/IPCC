package com.viettel.ipcclib.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * A conversation
 * Created by Alan's on 3/16/2017.
 */

public class ConversationModel {
  private String mAvatarPartner;
  private List<GroupMessage> mGroupMessages;

  public ConversationModel() {
    mGroupMessages = new ArrayList<>();
  }

  public ConversationModel(String mAvatarPartner, List<GroupMessage> mGroupMessages) {
    this.mAvatarPartner = mAvatarPartner;
    this.mGroupMessages = new ArrayList<>();
    this.mGroupMessages.addAll(mGroupMessages);
  }

  public String getmAvatarPartner() {
    return mAvatarPartner;
  }

  public void setmAvatarPartner(String mAvatarPartner) {
    this.mAvatarPartner = mAvatarPartner;
  }

  public List<GroupMessage> getmGroupMessages() {
    return mGroupMessages;
  }

  public void setmGroupMessages(List<GroupMessage> mGroupMessages) {
    this.mGroupMessages = mGroupMessages;
  }

  public void addMessage(Message m) {
    if (mGroupMessages.size() == 0) {
      List<Message> messages = new ArrayList<>();
      messages.add(m);
      GroupMessage groupMessage = new GroupMessage(m.getUserId(), mAvatarPartner, messages);
      mGroupMessages.add(groupMessage);
    } else {
      GroupMessage last = mGroupMessages.get(mGroupMessages.size() - 1);
      if (last.getmUserId() == m.getUserId())
        last.addMessage(m);
      else {
        List<Message> messages = new ArrayList<>();
        messages.add(m);
        GroupMessage groupMessage = new GroupMessage(m.getUserId(), mAvatarPartner, messages);
        mGroupMessages.add(groupMessage);
      }
    }
  }
}