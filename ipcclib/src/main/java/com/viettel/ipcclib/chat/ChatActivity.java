package com.viettel.ipcclib.chat;

import com.viettel.ipcclib.CallViewFragment;
import com.viettel.ipcclib.R;
import com.viettel.ipcclib.RtcClient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import static com.viettel.ipcclib.RtcClient.appRtcClient;


public class ChatActivity extends FragmentActivity implements ChatTextVideoListner {
  private static final String END_POINT = "END_POINT";
  private static final String DOMAIN = "DOMAIN";
  private static final String SERVICE_ID = "SERVICE_ID";
  private CallViewFragment mCallViewFragment;
  private ChatTextFragment mChatTextFragment;
//  private BroadcastReceiver mChatConnectReceiver = new BroadcastReceiver() {
//    @Override
//    public void onReceive(Context context, Intent intent) {
//      hangoutVideoCall();
//    }
//  };

  public static void start(Context context, String endPoint, String domain, int serviceId) {
    Intent intent = new Intent(context, ChatActivity.class);
    intent.putExtra(END_POINT, endPoint);
    intent.putExtra(DOMAIN, domain);
    intent.putExtra(SERVICE_ID, serviceId);

    context.startActivity(intent);
    RtcClient.rootEglBase = null;
    RtcClient.peerConnectionParameters = null;
    RtcClient.signalingParam = null;
    appRtcClient = null;
    RtcClient.audioManager = null;
    RtcClient.peerConnectionClient = null;
    RtcClient.peerConnectionClient2 = null;
    RtcClient.roomConnectionParameters = null;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);
    Intent intent = getIntent();
    if (intent == null) {
      finish();
      return;
    }

    int serviceId = intent.getIntExtra(SERVICE_ID, 0);
    String endPoint = intent.getStringExtra(END_POINT);
    String domain = intent.getStringExtra(DOMAIN);
    mChatTextFragment = new ChatTextFragment();
    mChatTextFragment.setDomain(domain)
        .setEndPoint(endPoint)
        .setServiceId(serviceId);

    getSupportFragmentManager().beginTransaction().replace(R.id.chat_container,
        mChatTextFragment
    ).commit();
//    registerReceiver(mChatConnectReceiver, new IntentFilter("finish_video_call"));
  }

  private boolean makingCall = false;
  @Override
  public void onCallVideoClicked() {
    if (makingCall) {
      return;
    }

    if (mCallViewFragment == null) {
      mCallViewFragment = new CallViewFragment();
      appRtcClient.makeCall();
      makingCall = true;
    } else {
      if (mCallViewFragment.isHidden()) {
        showCallVideoFragment();
      } else {
        getSupportFragmentManager().beginTransaction()
            .hide(mCallViewFragment).commit();
        mCallViewFragment.showFullVideoText(true);
      }
    }

  }

  @Override
  public void onBackPressed() {
    if (mCallViewFragment != null) {
      if (!mCallViewFragment.isHidden()) {
        getSupportFragmentManager().beginTransaction()
            .hide(mCallViewFragment).commit();
        getSupportFragmentManager().beginTransaction().show(mChatTextFragment).commit();
        mCallViewFragment.showFullVideoText(true);
        return;
      }
    }
    super.onBackPressed();
  }

  public void showCallVideoFragment() {
    if (mCallViewFragment != null && mCallViewFragment.isHidden())
      getSupportFragmentManager().beginTransaction()
          .show(mCallViewFragment).commit();
    mCallViewFragment.showFullVideoText(false);
  }

  private void addCallVideoFragment() {
    getSupportFragmentManager().beginTransaction()
        .add(R.id.chat_container, mCallViewFragment)
//          .addToBackStack(CallViewFragment.class.getSimpleName())
        .commit();
    mCallViewFragment.showFullVideoText(false);
  }

  public void hangoutVideoCall() {
    makingCall = false;
    if (mCallViewFragment != null) {
      try {
//        mCallViewFragment.onCallHangUp();
        getSupportFragmentManager().beginTransaction().remove(mCallViewFragment).commitAllowingStateLoss();
      } catch (IllegalStateException ex){ex.printStackTrace();}

//      getSupportFragmentManager().popBackStack();
      mCallViewFragment = null;
    }
  }
  public void disconnectVideoCall(){
    if (mCallViewFragment!=null){
      mCallViewFragment.onCallHangUp();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
//    unregisterReceiver(mChatConnectReceiver);
//    android.os.Process.killProcess(android.os.Process.myPid());
    System.gc();
    System.gc();

  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    //No call for super(). Bug on API Level > 11.
//    outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
//    super.onSaveInstanceState(outState);
  }

  public void onWSReciveCall() {
    makingCall = false;
    addCallVideoFragment();
  }
}
