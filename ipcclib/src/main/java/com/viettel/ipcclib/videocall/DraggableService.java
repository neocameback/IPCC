package com.viettel.ipcclib.videocall;

import com.viettel.ipcclib.R;

import org.webrtc.SurfaceViewRenderer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * service for drag view
 * Created by hungdn on 3/16/2017.
 */

public class DraggableService extends Service {
  private RelativeLayout mTextureViewll;
  private SurfaceViewRenderer mAutoFitTextureView;
  private TextView mShowFullTv;
  // Binder given to clients
  private final IBinder mBinder = new LocalBinder();

  public SurfaceViewRenderer getTextureView() {
    return mAutoFitTextureView;
  }

  public void removeTextureView() {
    window.removeView(mTextureViewll);
  }

  public TextView getShowFullTv() {
    return mShowFullTv;
  }

  public void stopService() {
    mAutoFitTextureView.release();
    window.removeView(mTextureViewll);
    stopSelf();
  }

  public void updateShowFullVideoText(boolean isShow) {
    if (mShowFullTv == null) return;
    if (isShow)
      mShowFullTv.setVisibility(View.VISIBLE);
    else mShowFullTv.setVisibility(View.INVISIBLE);

    mTextureViewll.invalidate();
  }

  /**
   * Class used for the client Binder.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
    public DraggableService getService() {
      // Return this instance of LocalService so clients can call public methods
      return DraggableService.this;
    }
  }

  private WindowManager.LayoutParams params;
  private WindowManager window;

  private int width;

  @Override
  public void onCreate() {
    super.onCreate();
    mTextureViewll = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.small_textureview, null);

    mAutoFitTextureView = (SurfaceViewRenderer) mTextureViewll.findViewById(R.id.remote_video_view);
    mShowFullTv = (TextView) mTextureViewll.findViewById(R.id.drag_video_show_full_tv);

    window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    Display display = window.getDefaultDisplay();
    width = display.getWidth();

//		chatHead = new ImageView(this);
//		chatHead.setImageResource(R.drawable.face1);

    params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT);

    params.gravity = Gravity.TOP | Gravity.LEFT;
    params.x = width;
    params.y = 200;

    //this code is for dragging the chat head
    mTextureViewll.setOnTouchListener(new View.OnTouchListener() {
      private int initialX;
      private int initialY;
      private float initialTouchX;
      private float initialTouchY;

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            initialX = params.x;
            initialY = params.y;
            initialTouchX = event.getRawX();
            initialTouchY = event.getRawY();
            return true;
          case MotionEvent.ACTION_UP:
            if (event.getRawX() > width / 2) {
              params.x = width;
            } else {
              params.x = 0;
            }

            params.y = initialY
                + (int) (event.getRawY() - initialTouchY);
            window.updateViewLayout(mTextureViewll, params);
            return true;
          case MotionEvent.ACTION_MOVE:
            params.x = initialX
                + (int) (event.getRawX() - initialTouchX);
            params.y = initialY
                + (int) (event.getRawY() - initialTouchY);
            window.updateViewLayout(mTextureViewll, params);
            return true;
        }
        return false;
      }
    });
//    if (mTextureViewll )
    window.addView(mTextureViewll, params);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

//    if (mTextureViewll != null)
//      window.removeView(mTextureViewll);
//    window.removeView(mTextureViewll);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    // TODO Auto-generated method stub
    return mBinder;
  }

}
