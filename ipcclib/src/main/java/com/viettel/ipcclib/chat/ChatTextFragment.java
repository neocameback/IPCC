package com.viettel.ipcclib.chat;

import com.viettel.ipcclib.R;
import com.viettel.ipcclib.common.WSFragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ChatTextFragment extends WSFragment implements View.OnClickListener, TextWatcher, ConversationView {
  private static final String TAG = ChatTextFragment.class.getSimpleName();

  EditText mContentChatEt;
  TextView mSendTv;
  RecyclerView mConversationRv;
  ProgressBar mLoadEarlierPb;

  private ChatTextProcess mChatProcess;

  private List<String> missingPermissions;

  // List of mandatory application permissions.
  private static final String[] MANDATORY_PERMISSIONS = {
      "android.permission.MODIFY_AUDIO_SETTINGS",
      "android.permission.RECORD_AUDIO",
      "android.permission.CAMERA",
      "android.permission.INTERNET"
  };

  @Override
  public void onRequestPermissionsResult(
      int requestCode,
      String permissions[],
      int[] grantResults) {

    if (grantResults.length > 0
        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(getActivity(), MANDATORY_PERMISSIONS[0] + " Permission Granted!", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(getActivity(), MANDATORY_PERMISSIONS[0] + " Permission Denied!", Toast.LENGTH_SHORT).show();
    }
    missingPermissions.remove(0); //remove missing permission from array and request next left permission
    requestPermission();
  }


  //http://stackoverflow.com/questions/35484767/activitycompat-requestpermissions-not-showing-dialog-box
  //https://developer.android.com/training/permissions/requesting.html
  private void requestPermission() {
    if (missingPermissions.size() > 0)
      requestPermissions(new String[]{missingPermissions.get(0)}, missingPermissions.size());
  }

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
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    missingPermissions = new ArrayList();

    for (String permission : MANDATORY_PERMISSIONS) {
      if (getActivity().checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
        missingPermissions.add(permission);
      }
    }
    requestPermission();

    init();
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
    if (v.getId() == R.id.chat_text_send_tv) {

      if (mContentChatEt.getText().toString() != null && !mContentChatEt.getText().toString().equals("")) {
        //// TODO: 3/16/2017
        int random = (int) (Math.random() * 2 + 1);
        mChatProcess.addMessage(new Message(1, random, mContentChatEt.getText().toString(), "wed at 10:43 PM"));
        mContentChatEt.setText("");
        mSendTv.setSelected(false);
      }
    }
  }

  private static int firstVisibleInListview;

  @Override
  public void initView(ConversationAdapter adapter) {
    mConversationRv.setAdapter(adapter);
    final LinearLayoutManager mLinearLayoutManager = (LinearLayoutManager) mConversationRv.getLayoutManager();
  }

  @Override
  public void smoothScrollToBottom(int i) {
    mConversationRv.smoothScrollToPosition(i);
  }


  public static ChatTextFragment newInstance() {
    return new ChatTextFragment();
  }
}
