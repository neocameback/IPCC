package de.lespace.apprtc.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.lespace.apprtc.R;

public class ChatTextFragment extends Fragment implements View.OnClickListener, TextWatcher, ConversationView {
  EditText mContentChatEt;
  TextView mSendTv;
  RecyclerView mConversationRv;
  ProgressBar mLoadEarlierPb;

  private ChatTextProcess mChatProcess;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_chattext, container, false);
    mContentChatEt = (EditText) v.findViewById(R.id.chat_text_content_et);
    mSendTv = (TextView) v.findViewById(R.id.chat_text_send_tv);
    mConversationRv = (RecyclerView) v.findViewById(R.id.chat_text_conversation_rv);
    mLoadEarlierPb = (ProgressBar) v.findViewById(R.id.chat_text_load_earlier_pb);

    mConversationRv.setLayoutManager(new LinearLayoutManager(getContext()));
    mConversationRv.setHasFixedSize(true);

    mContentChatEt.addTextChangedListener(this);
    mSendTv.setOnClickListener(this);
    mChatProcess = new ChatTextProcessImpl(this);
    return v;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    //// TODO: 3/16/2017 Get conversation data
    mChatProcess.getConversation(System.currentTimeMillis());
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {

  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {

  }

  @Override
  public void afterTextChanged(Editable s) {
    if (s.toString().length() > 0)
      mSendTv.setSelected(true);
    else mSendTv.setSelected(false);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.chat_text_send_tv:
        if (mContentChatEt.getText().toString() != null && !mContentChatEt.getText().toString().equals("")) {
          //// TODO: 3/16/2017
          int random = (int) (Math.random() * 2 + 1);
          mChatProcess.addMessage(new Message(1, random, mContentChatEt.getText().toString(), "wed at 10:43 PM"));
          mContentChatEt.setText("");
          mSendTv.setSelected(false);
        }
        break;
    }
  }

  @Override
  public void initView(ConversationNotGroupAdapter adapter) {
    mConversationRv.setAdapter(adapter);
  }

  @Override
  public void smoothScrollToBottom(int i) {
    mConversationRv.smoothScrollToPosition(i);
  }

}
