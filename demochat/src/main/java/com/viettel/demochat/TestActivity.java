package com.viettel.demochat;

import com.viettel.ipcclib.chat.ChatActivity;
import com.viettel.ipcclib.constants.Configs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Created by Macbook on 3/17/17.
 */

public class TestActivity extends FragmentActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ChatActivity.start(TestActivity.this, Configs.ROOM_URL, Configs.DOMAIN_TEST, 28);
      }
    });
  }
}
