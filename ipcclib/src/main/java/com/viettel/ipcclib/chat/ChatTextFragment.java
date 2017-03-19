package com.viettel.ipcclib.chat;

import com.viettel.ipcclib.CallActivity;
import com.viettel.ipcclib.R;
import com.viettel.ipcclib.common.WSFragment;
import com.viettel.ipcclib.model.MessageData;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ChatTextFragment extends WSFragment implements View.OnClickListener, TextWatcher, ConversationView {
  EditText mContentChatEt;
  TextView mSendTv;
  RecyclerView mConversationRv;
  ProgressBar mLoadEarlierPb;
  TextView mTopMessageTv;
  TextView mTypingTv;

  private boolean mIsTyping;

  private ChatTextProcess mChatProcess;
  private Handler mHanlder = new Handler();

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
        mContext.finish();
      }
    });

    v.findViewById(R.id.video_call_iv).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        checkPermissions();
      }
    });

    return v;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
//    init();
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

  private void appendMessage(final CharSequence message, final Message.Type type) {
    mHanlder.post(new Runnable() {
      @Override
      public void run() {
        mChatProcess.addMessage(new Message(message, new Date(), type));
      }
    });
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
//    if (!isContextAvailable()) return;
//
//    mContext.runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        mTopMessageTv.setVisibility(View.GONE);
//      }
//    });
    mHanlder.post(new Runnable() {
      @Override
      public void run() {
        mTopMessageTv.setVisibility(View.GONE);
      }
    });
  }

  @Override
  protected void onWSAgentTyping(final String name, final boolean typing) {
//    if (!isContextAvailable()) return;
//
//    mContext.runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//       setTyping(name, typing);
//      }
//    });

//    mHanlder.post(new Runnable() {
//      @Override
//      public void run() {
        setTyping(name, typing);
//      }
//    });
  }

  private void setTyping(String name, final boolean typing) {
    mHanlder.post(new Runnable() {
      @Override
      public void run() {
        if (typing) {
          mTypingTv.setVisibility(View.VISIBLE);
//      mTypingTv.setText(String.format(mContext.getString(R.string.someone_typing), name));
          mTypingTv.setText(String.format(mContext.getString(R.string.someone_typing), mContext.getString(R.string.name_agent)));
        } else {
          mTypingTv.setVisibility(View.GONE);
          mTypingTv.setText("");
        }
      }
    });
  }

  @Override
  protected void onWSMessageReceived(final MessageData message) {
//    if (!isContextAvailable()) return;
//
//    mContext.runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        appendMessage(message.getMessage(), Message.Type.OTHER);
////        setTyping(message.getUserName(), false);
//        setTyping(mContext.getString(R.string.name_agent), false);
//
//      }
//    });

//    mHanlder.post(new Runnable() {
//      @Override
//      public void run() {
        appendMessage(message.getMessage(), Message.Type.OTHER);
//        setTyping(message.getUserName(), false);
        setTyping(mContext.getString(R.string.name_agent), false);
//      }
//    });
  }

  @Override
  protected void onWSNotFoundAgentAvailable() {
//    if (!isContextAvailable()) return;
//
//    mContext.runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        appendMessage(mContext.getString(R.string.no_agent_available), Message.Type.NOTICE);
//      }
//    });

//    mHanlder.post(new Runnable() {
//      @Override
//      public void run() {
        appendMessage(mContext.getString(R.string.no_agent_available), Message.Type.NOTICE);
//      }
//    });
  }

  @Override
  protected void onWSAgentEndConversation(final String name) {
//    if (!isContextAvailable()) return;
//
//    mContext.runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
////        appendMessage(String.format(mContext.getString(R.string.end_chat), name), Message.Type.NOTICE);
//        appendMessage(mContext.getString(R.string.chat_ended), Message.Type.NOTICE);
//      }
//    });

//    mHanlder.post(new Runnable() {
//      @Override
//      public void run() {
        appendMessage(mContext.getString(R.string.chat_ended), Message.Type.NOTICE);
//      }
//    });
  }

  @Override
  protected void onWSAgentJoinConversation(final String name) {
//    if (!isContextAvailable()) return;
//
//    mContext.runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
////        appendMessage(String.format(mContext.getString(R.string.format_join_chat), name), Message.Type.NOTICE);
//        String msg = String.format(mContext.getString(R.string.format_join_chat), mContext.getString(R.string.name_agent));
//        appendMessage(Html.fromHtml(msg), Message.Type.NOTICE);
//      }
//    });

    mHanlder.post(new Runnable() {
      @Override
      public void run() {
        String msg = String.format(mContext.getString(R.string.format_join_chat), mContext.getString(R.string.name_agent));
        appendMessage(Html.fromHtml(msg), Message.Type.NOTICE);
      }
    });
  }

  @Override
  protected void onWSChannelError(String description) {
    mHanlder.post(new Runnable() {
      @Override
      public void run() {
        if (mContext == null) return;
        mTopMessageTv.setText(R.string.channel_error_title);
        mTopMessageTv.setTextColor(ContextCompat.getColor(mContext, R.color.error));      }
    });
  }

  private static final int REQUEST_CAMERA_PERMISSION = 1;
  private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

  // List of mandatory application permissions.
  private static final String[] MANDATORY_PERMISSIONS = {
      "android.permission.MODIFY_AUDIO_SETTINGS",
      "android.permission.RECORD_AUDIO",
      "android.permission.CAMERA",
      "android.permission.INTERNET"
  };

  private void checkPermissions() {
    List<String> missingPermissions = new ArrayList<>();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity())) {


      //If the draw over permission is not available open the settings screen
      //to grant the permission.
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:" + getActivity().getPackageName()));
      startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
    } else {
      missingPermissions.clear();
      for (String permission : MANDATORY_PERMISSIONS) {
        if (getActivity().checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
          missingPermissions.add(permission);
        }
      }

      if (missingPermissions.isEmpty()) {
        startActivity(new Intent(mContext, CallActivity.class));
      } else {
        requestPermission(missingPermissions);
      }
    }
  }

  //http://stackoverflow.com/questions/35484767/activitycompat-requestpermissions-not-showing-dialog-box
  //https://developer.android.com/training/permissions/requesting.html
  private void requestPermission(List<String> missingPermissions) {
    String[] permissions = missingPermissions.toArray(new String[missingPermissions.size()]);
    if (missingPermissions.size() > 0)
      this.requestPermissions(permissions, REQUEST_CAMERA_PERMISSION);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    if (requestCode == REQUEST_CAMERA_PERMISSION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//        ErrorDialog.newInstance(getString(R.string.request_permission))
//            .show(getChildFragmentManager(), FRAGMENT_DIALOG);

        checkPermissions();

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      }
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
//
    if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
//      //Check if the permission is granted or not.
      if (resultCode == RESULT_OK) {
        checkPermissions();
//        initializeView();
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//            == PackageManager.PERMISSION_GRANTED) {
//          Intent intent = new Intent(this, DraggableService.class);
//          this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//        } else {
//          requestCameraPermission();
//        }
      } //Permission is not available
////        Toast.makeText(this,
////                "Draw over other app permission not available. Closing the application",
////                Toast.LENGTH_SHORT).show();
////
////        finish();
//      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onResume() {
    super.onResume();
  }
}
