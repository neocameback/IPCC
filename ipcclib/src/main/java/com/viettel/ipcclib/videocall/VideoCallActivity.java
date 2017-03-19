package com.viettel.ipcclib.videocall;

import com.viettel.ipcclib.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * activity video call demo
 * Created by Quannv on 3/15/2017.
 */

public class VideoCallActivity extends FragmentActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    if (null == savedInstanceState) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.container, VideoCallFragment.newInstance())
          .commit();
    }
  }

}
