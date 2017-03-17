package com.viettel.ipcclib.chat;

import com.viettel.ipcclib.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;


public class ChatActivity extends FragmentActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);

    getSupportFragmentManager().beginTransaction().replace(R.id.chat_container, new ChatTextFragment()).commit();
  }
}
