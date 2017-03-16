package de.lespace.apprtc.chat;


public interface ConversationView {
  void initView(ConversationAdapter adapter);

  void smoothScrollToBottom(int i);
}
