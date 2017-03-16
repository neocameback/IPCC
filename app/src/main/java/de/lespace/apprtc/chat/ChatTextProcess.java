package de.lespace.apprtc.chat;


public interface ChatTextProcess {
  void getConversation(long time);

  void addMessage(Message message);

  ConversationAdapter getConversationAdapter();
}
