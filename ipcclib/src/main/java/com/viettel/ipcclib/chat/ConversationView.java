package com.viettel.ipcclib.chat;


public interface ConversationView {
  void initView(ConversationNotGroupAdapter adapter);

  void smoothScrollToBottom(int i);
}
