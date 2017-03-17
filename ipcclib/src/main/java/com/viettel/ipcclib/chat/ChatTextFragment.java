package com.viettel.ipcclib.chat;

import com.viettel.ipcclib.CallActivity;
import com.viettel.ipcclib.R;
import com.viettel.ipcclib.common.WSFragment;
import com.viettel.ipcclib.model.MessageData;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

public class ChatTextFragment extends WSFragment implements View.OnClickListener, TextWatcher, ConversationView {
  EditText mContentChatEt;
  TextView mSendTv;
  RecyclerView mConversationRv;
  ProgressBar mLoadEarlierPb;
  TextView mTopMessageTv;
  TextView mTypingTv;

  private boolean mIsTyping;

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
    mTopMessageTv = (TextView) v.findViewById(R.id.top_messgage_tv);
    mTypingTv = (TextView) v.findViewById(R.id.chat_text_typing_hint_tv);

    mConversationRv.setLayoutManager(new LinearLayoutManager(getContext()));
    mConversationRv.setHasFixedSize(true);

    mContentChatEt.addTextChangedListener(this);
    mSendTv.setOnClickListener(this);
    mChatProcess = new ChatTextProcessImpl(this);

    v.findViewById(R.id.chat_text_back_img).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().finish();
      }
    });

    v.findViewById(R.id.video_call_iv).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(getActivity(), CallActivity.class));
      }
    });

    return v;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    init();
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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
    if (s.toString().length() > 0) {
      mSendTv.setSelected(true);
      if (!mIsTyping) {
        mIsTyping = true;
        sendTypingStatus(true);
      }
    } else {
      mSendTv.setSelected(false);
      sendTypingStatus(false);
      mIsTyping = false;
    }
  }

  @Override
  public void onClick(View v) {
//    switch (v.getId()) {
//      case R.id.chat_text_send_tv:
   if (v.getId() == R.id.chat_text_send_tv) {
     String msg = mContentChatEt.getText().toString();
        if (!msg.equals("")) {
          sendMessage(msg);
          appendMessage(mContentChatEt.getText().toString(), Message.Type.MINE);
          mContentChatEt.setText("");
          mSendTv.setSelected(false);
        }
//        break;
    }
  }

  private void appendMessage(CharSequence message, Message.Type type) {
    mChatProcess.addMessage(new Message(message, new Date(), type));
  }

  @Override
  public void initView(ConversationNotGroupAdapter adapter) {
    mConversationRv.setAdapter(adapter);
  }

  @Override
  public void smoothScrollToBottom(int i) {
    mConversationRv.smoothScrollToPosition(i);
  }


  public static ChatTextFragment newInstance() {
    return new ChatTextFragment();
  }


  @Override
  protected void onWSConnected() {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mTopMessageTv.setVisibility(View.GONE);
      }
    });
  }

  @Override
  protected void onWSAgentTyping(final String name, final boolean typing) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
       setTyping(name, typing);
      }
    });
  }

  private void setTyping(String name, boolean typing) {
    if (typing) {
      mTypingTv.setVisibility(View.VISIBLE);
//      mTypingTv.setText(String.format(getActivity().getString(R.string.someone_typing), name));
      mTypingTv.setText(String.format(getActivity().getString(R.string.someone_typing), getString(R.string.name_agent)));
    } else {
      mTypingTv.setVisibility(View.GONE);
      mTypingTv.setText("");
    }
  }

  @Override
  protected void onWSMessageReceived(final MessageData message) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        appendMessage(message.getMessage(), Message.Type.OTHER);
//        setTyping(message.getUserName(), false);
        setTyping(getString(R.string.name_agent), false);

      }
    });
  }

  @Override
  protected void onWSNotFoundAgentAvailable() {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        appendMessage(getString(R.string.no_agent_available), Message.Type.NOTICE);
      }
    });
  }

  @Override
  protected void onWSAgentEndConversation(final String name) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
//        appendMessage(String.format(getString(R.string.end_chat), name), Message.Type.NOTICE);
        appendMessage(getString(R.string.chat_ended), Message.Type.NOTICE);
      }
    });
  }

  @Override
  protected void onWSAgentJoinConversation(final String name) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
//        appendMessage(String.format(getString(R.string.format_join_chat), name), Message.Type.NOTICE);
        String msg = String.format(getString(R.string.format_join_chat), getString(R.string.name_agent));
        appendMessage(Html.fromHtml(msg), Message.Type.NOTICE);
      }
    });
  }
}
