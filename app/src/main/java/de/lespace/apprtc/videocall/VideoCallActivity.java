package de.lespace.apprtc.videocall;

import android.app.Activity;
import android.os.Bundle;

import de.lespace.apprtc.R;

/**
 * activity video call demo
 * Created by Quannv on 3/15/2017.
 */

public class VideoCallActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    if (null == savedInstanceState) {
      getFragmentManager().beginTransaction()
          .replace(R.id.container, VideoCallFragment.newInstance())
          .commit();
    }
  }

}
