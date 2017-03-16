package de.lespace.apprtc.chat;

import java.util.ArrayList;
import java.util.List;


public class Mapping {
  public static List<GroupMessage> listMessagesToGroupMessage(List<Message> messages) {
    List<GroupMessage> groupMessages = new ArrayList<>();
    GroupMessage mGroupMessage = null;
    for (Message m : messages) {
      if (mGroupMessage == null) {
        mGroupMessage = new GroupMessage();
        mGroupMessage.setmUserId(m.getUserId());
        mGroupMessage.addMessage(m);
      } else {
        if (mGroupMessage.getmUserId() == m.getUserId()) {
          mGroupMessage.addMessage(m);
        } else {
          groupMessages.add(mGroupMessage);
          mGroupMessage = null;
        }
      }
    }
    return groupMessages;
  }
}
