package de.lespace.apprtc.chat;

/**
 * Process chat
 * Created by Alan's on 3/16/2017.
 */

public class ChatTextProcessImpl implements ChatTextProcess {
  private ConversationView mConversationView;
  private ConversationNotGroupAdapter mConversationNotGroupAdapter;

  public ChatTextProcessImpl(ConversationView mConversationView) {
    this.mConversationView = mConversationView;
    mConversationNotGroupAdapter = new ConversationNotGroupAdapter(mConversationModels.getMessages());
    mConversationView.initView(mConversationNotGroupAdapter);
  }

  @Override
  public ConversationNotGroupAdapter getConversationNotGroupAdapter() {
    return mConversationNotGroupAdapter;
  }

  private ConversationModel mConversationModels = new ConversationModel();

  @Override
  public void getConversation(long time) {

  }

  @Override
  public void addMessage(Message message) {
    mConversationModels.addMessage(message);
    mConversationNotGroupAdapter.notifyDataSetChanged();
    mConversationView.smoothScrollToBottom(mConversationNotGroupAdapter.getItemCount() - 1);
  }
}
