package com.viettel.ipcclib.chat;

/**
 * Process chat
 * Created by Alan's on 3/16/2017.
 */

public class ChatTextProcessImpl implements ChatTextProcess {
  private ConversationView mConversationView;
  private ConversationAdapter mConversationAdapter;

  public ChatTextProcessImpl(ConversationView mConversationView) {
    this.mConversationView = mConversationView;
    mConversationAdapter = new ConversationAdapter(mConversationModels.getmGroupMessages());
    mConversationView.initView(mConversationAdapter);
  }

  @Override
  public ConversationAdapter getConversationAdapter() {
    return mConversationAdapter;
  }

  private ConversationModel mConversationModels = new ConversationModel();

  @Override
  public void getConversation(long time) {

  }

  @Override
  public void addMessage(Message message) {
    mConversationModels.addMessage(message);
    mConversationAdapter.notifyDataSetChanged();
    mConversationView.smoothScrollToBottom(mConversationAdapter.getItemCount()-1);
  }
}
