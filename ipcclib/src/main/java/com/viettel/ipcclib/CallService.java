/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.viettel.ipcclib;

import com.viettel.ipcclib.model.MessageData;

import org.webrtc.RendererCommon.ScalingType;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Fragment for call control.
 */
public class CallService extends Service {

  private View controlView;

  private TextView contactView;
  private ImageButton disconnectButton;
  private ImageButton audioButton;
  private ImageButton cameraSwitchButton;
  private ImageButton videoScalingButton;
  private TextView captureFormatText;
  private SeekBar captureFormatSlider;
  private OnCallEvents callEvents;
  private ScalingType scalingType;
  private boolean videoCallEnabled = true;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  protected void onWSAgentJoinConversation(String fullName) {

  }

  protected void onWSAgentEndConversation(String agentName) {

  }

  protected void onWSNotFoundAgentAvailable() {

  }

  protected void onWSMessageReceived(MessageData message) {

  }

  protected void onWSAgentTyping(String name, boolean typing) {

  }

  protected void onWSConnected() {

  }

  protected void onWSChannelError(String description) {

  }

  /**
   * Call control interface for container activity.
   */
  public interface OnCallEvents {
    public void onCallHangUp();

    public void onCameraSwitch();

    public void onAudioMute();

    public void onVideoScalingSwitch(ScalingType scalingType);

    public void onCaptureFormatChange(int width, int height, int framerate);
  }

  @Override
  public void onCreate() {
    controlView =
        LayoutInflater.from(this).inflate(R.layout.fragment_call, null);

    // Create UI controls.
    contactView =
        (TextView) controlView.findViewById(R.id.contact_name_call);

    disconnectButton =
        (ImageButton) controlView.findViewById(R.id.button_call_disconnect);

    audioButton =
        (ImageButton) controlView.findViewById(R.id.button_audio_on_off);

    cameraSwitchButton =
        (ImageButton) controlView.findViewById(R.id.button_call_switch_camera);
    videoScalingButton =
        (ImageButton) controlView.findViewById(R.id.button_call_scaling_mode);
    captureFormatText =
        (TextView) controlView.findViewById(R.id.capture_format_text_call);
    captureFormatSlider =
        (SeekBar) controlView.findViewById(R.id.capture_format_slider_call);

    // Add buttons click events.
    disconnectButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        callEvents.onCallHangUp();
      }
    });

    audioButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        callEvents.onAudioMute();
      }
    });

    cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        callEvents.onCameraSwitch();

      }
    });

    videoScalingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (scalingType == ScalingType.SCALE_ASPECT_FILL) {
          videoScalingButton.setBackgroundResource(
              R.drawable.ic_close);
          scalingType = ScalingType.SCALE_ASPECT_FIT;
        } else {
          videoScalingButton.setBackgroundResource(
              R.drawable.ic_close);
          scalingType = ScalingType.SCALE_ASPECT_FILL;
        }
        callEvents.onVideoScalingSwitch(scalingType);
      }
    });
    scalingType = ScalingType.SCALE_ASPECT_FILL;
    WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    Display display = window.getDefaultDisplay();
    int width = display.getWidth();

//		chatHead = new ImageView(this);
//		chatHead.setImageResource(R.drawable.face1);

    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT);

    params.gravity = Gravity.TOP | Gravity.LEFT;
    params.x = width;
    params.y = 200;
    window.addView(controlView,params);
  }
//  @Override
//  public View onStartCommaView(LayoutInflater inflater, ViewGroup container,
//
//    return controlView;
//  }

//  @Override
//  public void onStart() {
//    super.onStart();
//
//    boolean captureSliderEnabled = false;
//    Bundle args = getArguments();
//    if (args != null) {
//      String contactName = args.getString(CallActivity.EXTRA_FROM);
//      contactView.setText(contactName);
//      videoCallEnabled = args.getBoolean(CallActivity.EXTRA_VIDEO_CALL, true);
//      captureSliderEnabled = videoCallEnabled
//          && args.getBoolean(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, false);
//    }
//    if (!videoCallEnabled) {
//      cameraSwitchButton.setVisibility(View.INVISIBLE);
//    }
//    if (captureSliderEnabled) {
//      captureFormatSlider.setOnSeekBarChangeListener(
//          new CaptureQualityController(captureFormatText, callEvents));
//    } else {
//      captureFormatText.setVisibility(View.GONE);
//      captureFormatSlider.setVisibility(View.GONE);
//    }
//  }

//  @Override
//  public void onAttach(Activity activity) {
//    super.onAttach(activity);
//    callEvents = (OnCallEvents) activity;
//  }

}
