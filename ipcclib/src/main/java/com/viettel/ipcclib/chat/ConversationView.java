package com.viettel.ipcclib.chat;


public interface ConversationView {
  void initView(ConversationAdapter adapter);

  void smoothScrollToBottom(int i);
}
