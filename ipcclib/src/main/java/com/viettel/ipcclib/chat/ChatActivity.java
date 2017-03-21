package com.viettel.ipcclib.chat;

import com.viettel.ipcclib.CallViewFragment;
import com.viettel.ipcclib.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;


public class ChatActivity extends FragmentActivity implements ChatTextVideoListner{
  private static final String END_POINT = "END_POINT";
  private static final String DOMAIN = "DOMAIN";
  private static final String SERVICE_ID = "SERVICE_ID";
  private CallViewFragment mCallViewFragment;

  public static void start(Context context, String endPoint, String domain, int serviceId) {
    Intent intent = new Intent(context, ChatActivity.class);
    intent.putExtra(END_POINT, endPoint);
    intent.putExtra(DOMAIN, domain);
    intent.putExtra(SERVICE_ID, serviceId);

    context.startActivity(intent);
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
    getSupportFragmentManager().beginTransaction().replace(R.id.chat_container,
        new ChatTextFragment()
        .setDomain(domain)
        .setEndPoint(endPoint)
        .setServiceId(serviceId)
    ).commit();
  }

  @Override
  public void onCallVideoClicked() {
    if (mCallViewFragment == null) {
      mCallViewFragment = new CallViewFragment();
      getSupportFragmentManager().beginTransaction().add(R.id.chat_container, mCallViewFragment)
          .commit();
    } else {
      if (mCallViewFragment.isHidden()) {
        getSupportFragmentManager().beginTransaction()
            .show(mCallViewFragment).commit();
      } else {
        getSupportFragmentManager().beginTransaction()
            .hide(mCallViewFragment).commit();
      }
    }

  }

  @Override
  public void onBackPressed() {
    if (mCallViewFragment != null && !mCallViewFragment.isHidden()) {
      getSupportFragmentManager().beginTransaction()
          .hide(mCallViewFragment).commit();
      return;
    }
    super.onBackPressed();
  }
}
