package de.lespace.apprtc.chat;


public interface ConversationView {
  void initView(ConversationNotGroupAdapter adapter);

  void smoothScrollToBottom(int i);
}
