package com.viettel.ipcclib.chat;


public interface ChatTextProcess {
  void getConversation(long time);

  void addMessage(Message message);

  ConversationNotGroupAdapter getConversationNotGroupAdapter();
}
