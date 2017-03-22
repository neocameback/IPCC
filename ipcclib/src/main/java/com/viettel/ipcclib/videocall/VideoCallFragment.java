package com.viettel.ipcclib.videocall;

import com.viettel.ipcclib.AppRTCClient;
import com.viettel.ipcclib.PeerConnectionClient;
import com.viettel.ipcclib.R;
import com.viettel.ipcclib.constants.Configs;

import org.webrtc.SurfaceViewRenderer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static com.viettel.ipcclib.RTCConnection.isWSSUrl;
import static com.viettel.ipcclib.RTCConnection.isWSUrl;
import static com.viettel.ipcclib.common.WSFragment.peerConnectionParameters;
import static com.viettel.ipcclib.common.WSFragment.roomConnectionParameters;

;

/**
 * fragment video call
 * Created by Quannv on 3/15/2017.
 */

public class VideoCallFragment extends Fragment
    implements View.OnClickListener, FragmentCompat.OnRequestPermissionsResultCallback {

  private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
  private ImageView mBackIv, mEndCallIv, mMuteAudioIv, mVideoCallIv;
  public static final String CAMERA_FRONT = "1";
  public static final String CAMERA_BACK = "0";

  private DraggableService mService;
  boolean mBound = false;

  /**
   * Conversion from screen rotation to JPEG orientation.
   */
  private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
  private static final int REQUEST_CAMERA_PERMISSION = 1;
  private static final String FRAGMENT_DIALOG = "dialog";

  static {
    ORIENTATIONS.append(Surface.ROTATION_0, 90);
    ORIENTATIONS.append(Surface.ROTATION_90, 0);
    ORIENTATIONS.append(Surface.ROTATION_180, 270);
    ORIENTATIONS.append(Surface.ROTATION_270, 180);
  }

  /**
   * An additional thread for running tasks that shouldn't block the UI.
   */
  private HandlerThread mBackgroundThread;

  /**
   * A {@link Handler} for running tasks in the background.
   */
  private Handler mBackgroundHandler;

  /**
   * Camera state: Showing camera preview.
   */
  private static final int STATE_PREVIEW = 0;

  /**
   * Camera state: Waiting for the exposure to be precapture state.
   */
  private static final int STATE_WAITING_PRECAPTURE = 2;

  /**
   * Camera state: Waiting for the exposure state to be something other than precapture.
   */
  private static final int STATE_WAITING_NON_PRECAPTURE = 3;

  /**
   * An {@link ImageReader} that handles still image capture.
   */
  private ImageReader mImageReader;


  /**
   * Max preview width that is guaranteed by Camera2 API
   */
  private static final int MAX_PREVIEW_WIDTH = 1920;

  /**
   * Max preview height that is guaranteed by Camera2 API
   */
  private static final int MAX_PREVIEW_HEIGHT = 1080;

  /**
   * ID of the current {@link CameraDevice}.
   */
  private String mCameraId = CAMERA_BACK;

  /**
   * An {@link AutoFitTextureView} for camera preview.
   */
  private SurfaceViewRenderer mTextureView;

  /**
   * A {@link CameraCaptureSession } for camera preview.
   */
  private CameraCaptureSession mCaptureSession;

  /**
   * A reference to the opened {@link CameraDevice}.
   */
  private CameraDevice mCameraDevice;

  /**
   * The {@link Size} of camera preview.
   */
  private Size mPreviewSize;

  /**
   * {@link CaptureRequest.Builder} for the camera preview
   */
  private CaptureRequest.Builder mPreviewRequestBuilder;

  /**
   * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
   */
  private CaptureRequest mPreviewRequest;

  /**
   * The current state of camera state for taking pictures.
   *
   * @see #mCaptureCallback
   */
  private int mState = STATE_PREVIEW;

  /**
   * A {@link Semaphore} to prevent the app from exiting before closing the camera.
   */
  private Semaphore mCameraOpenCloseLock = new Semaphore(1);

  /**
   * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
   * still image is ready to be saved.
   */
  private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
      = new ImageReader.OnImageAvailableListener() {

    @Override
    public void onImageAvailable(ImageReader reader) {
      // TODO: 3/15/2017 get Image result from preview here to process
    }

  };

  /**
   * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
   */
  private CameraCaptureSession.CaptureCallback mCaptureCallback
      = new CameraCaptureSession.CaptureCallback() {

    private void process(CaptureResult result) {
      switch (mState) {
        case STATE_PREVIEW: {
          // We have nothing to do when the camera preview is working normally.
          break;
        }

        case STATE_WAITING_PRECAPTURE: {
          // CONTROL_AE_STATE can be null on some devices
          Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
          if (aeState == null ||
              aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
              aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
            mState = STATE_WAITING_NON_PRECAPTURE;
          }
          break;
        }

      }
    }



    @Override
    public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureResult partialResult) {
      process(partialResult);
    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                   @NonNull CaptureRequest request,
                                   @NonNull TotalCaptureResult result) {
      process(result);
    }

  };

  /**
   * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
   * {@link TextureView}.
   */
  private final TextureView.SurfaceTextureListener mSurfaceTextureListener
      = new TextureView.SurfaceTextureListener() {

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
      openCamera(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
      configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
      return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
    }

  };

  /**
   * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
   */
  private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

    @Override
    public void onOpened(@NonNull CameraDevice cameraDevice) {
      // This method is called when the camera is opened.  We start camera preview here.
      mCameraOpenCloseLock.release();
      mCameraDevice = cameraDevice;
//      createCameraPreviewSession();
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
      mCameraOpenCloseLock.release();
      cameraDevice.close();
      mCameraDevice = null;
    }

    @Override
    public void onError(@NonNull CameraDevice cameraDevice, int error) {
      mCameraOpenCloseLock.release();
      cameraDevice.close();
      mCameraDevice = null;
      Activity activity = getActivity();
      if (null != activity) {
        activity.finish();
      }
    }

  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

//    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//            Uri.parse("package:" + getActivity().getPackageName()));
//    startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);

//    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
//            == PackageManager.PERMISSION_GRANTED) {
//      Intent intent = new Intent(getActivity(), DraggableService.class);
//      getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//    } else {
//      requestCameraPermission();
//    }
  }

  /** Defines callbacks for service binding, passed to bindService() */
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      DraggableService.LocalBinder binder = (DraggableService.LocalBinder) service;
      mService = binder.getService();
      mBound = true;
      mTextureView = mService.getTextureView();
      reopenCamera();
//      mService.setImageView(mImageView);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }
  };

  /**
   * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
   * is at least as large as the respective texture view size, and that is at most as large as the
   * respective max size, and whose aspect ratio matches with the specified value. If such size
   * doesn't exist, choose the largest one that is at most as large as the respective max size,
   * and whose aspect ratio matches with the specified value.
   *
   * @param choices           The list of sizes that the camera supports for the intended output
   *                          class
   * @param textureViewWidth  The width of the texture view relative to sensor coordinate
   * @param textureViewHeight The height of the texture view relative to sensor coordinate
   * @param maxWidth          The maximum width that can be chosen
   * @param maxHeight         The maximum height that can be chosen
   * @param aspectRatio       The aspect ratio
   * @return The optimal {@code Size}, or an arbitrary one if none were big enough
   */
  private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                        int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

    // Collect the supported resolutions that are at least as big as the preview Surface
    List<Size> bigEnough = new ArrayList<>();
    // Collect the supported resolutions that are smaller than the preview Surface
    List<Size> notBigEnough = new ArrayList<>();
    int w = aspectRatio.getWidth();
    int h = aspectRatio.getHeight();
    for (Size option : choices) {
      if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
          option.getHeight() == option.getWidth() * h / w) {
        if (option.getWidth() >= textureViewWidth &&
            option.getHeight() >= textureViewHeight) {
          bigEnough.add(option);
        } else {
          notBigEnough.add(option);
        }
      }
    }

    // Pick the smallest of those big enough. If there is no one big enough, pick the
    // largest of those not big enough.
    if (bigEnough.size() > 0) {
      return Collections.min(bigEnough, new CompareSizesByArea());
    } else if (notBigEnough.size() > 0) {
      return Collections.max(notBigEnough, new CompareSizesByArea());
    } else {
      Log.e(TAG, "Couldn't find any suitable preview size");
      return choices[0];
    }
  }

  public static VideoCallFragment newInstance() {
    return new VideoCallFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_video_call, container, false);
  }

  @Override
  public void onViewCreated(final View view, Bundle savedInstanceState) {
//        view.findViewById(R.id.picture).setOnClickListener(this);
//        view.findViewById(R.id.info).setOnClickListener(this);
//    mTextureView = (AutoFitTextureView) view.findViewById(R.id.video_call_owner_texture);
    mMuteAudioIv = (ImageView) view.findViewById(R.id.mute_audio_iv);
    mEndCallIv = (ImageView) view.findViewById(R.id.end_call_iv);
    mVideoCallIv = (ImageView) view.findViewById(R.id.video_call_iv);
//    mSwapCameraIv = (ImageView) view.findViewById(R.id.swap_camera_iv);
//    mScaleVideoIv = (ImageView) view.findViewById(R.id.scale_video_iv);
    mBackIv = (ImageView) view.findViewById(R.id.back_iv);

    mMuteAudioIv.setOnClickListener(this);
    mEndCallIv.setOnClickListener(this);
    mVideoCallIv.setOnClickListener(this);
//    mScaleVideoIv.setOnClickListener(this);
    mBackIv.setOnClickListener(this);

  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public void onResume() {
    super.onResume();
    // When the screen is turned off and turned back on, the SurfaceTexture is already
    // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
    // a camera and start preview from here (otherwise, we wait until the surface is ready in
    // the SurfaceTextureListener).
//    reopenCamera();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity())) {


      //If the draw over permission is not available open the settings screen
      //to grant the permission.
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
              Uri.parse("package:" + getActivity().getPackageName()));
      startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
    } else {
      if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
              == PackageManager.PERMISSION_GRANTED) {
        Intent intent = new Intent(getActivity(), DraggableService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
      } else {
        requestCameraPermission();
      }
    }

//    reopenCamera();
//
//    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
//            == PackageManager.PERMISSION_GRANTED) {
//      Intent intent = new Intent(getActivity(), DraggableService.class);
//      getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//    } else {
//      requestCameraPermission();
//    }
  }
//
  @Override
  public void onStop() {
    // Unbind from the service
//    closeCamera();
    if (mBound) {
      closeCamera();
      mService.removeTextureView();
      getActivity().unbindService(mConnection);
      mBound = false;
    }
    super.onStop();
  }

  @Override
  public void onClick(View v) {
    if (v == mEndCallIv) {
      // TODO: 3/16/2017 end video call
    } else if (v == mMuteAudioIv) {
      // TODO: 3/16/2017 mute audio video call
    } else if (v == mVideoCallIv) {
      swapCamera();
      // TODO: 3/17/2017 allow video call
    } else if (v == mBackIv) {
      // TODO: 3/16/2017 finish video call
      Log.e("@@@@@@@@@@@@@@","Click back");
    }
  }

  private void swapCamera() {
    if (mCameraId.equals(CAMERA_FRONT)) {
      mCameraId = CAMERA_BACK;
      closeCamera();
      reopenCamera();

    } else if (mCameraId.equals(CAMERA_BACK)) {
      mCameraId = CAMERA_FRONT;
      closeCamera();
      reopenCamera();
    }
  }

  public void reopenCamera() {
//    if (mTextureView.isAvailable()) {
//      openCamera(mTextureView.getWidth(), mTextureView.getHeight());
//    } else {
//      mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//    }
  }

  /**
   * Sets up member variables related to camera.
   *
   * @param width  The width of available size for camera preview
   * @param height The height of available size for camera preview
   */
  private void setUpCameraOutputs(int width, int height) {
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      CameraCharacteristics characteristics
          = manager.getCameraCharacteristics(mCameraId);

      StreamConfigurationMap map = characteristics.get(
          CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

      // For still image captures, we use the largest available size.

      if (map == null) {
        return;
      }

      Size largest = Collections.max(
          Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
          new CompareSizesByArea());
      mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
          ImageFormat.JPEG, /*maxImages*/2);
      mImageReader.setOnImageAvailableListener(
          mOnImageAvailableListener, mBackgroundHandler);

      // Find out if we need to swap dimension to get the preview size relative to sensor
      // coordinate.
      int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
      //noinspection ConstantConditions
        /*
    Orientation of the camera sensor
   */
      int mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
      boolean swappedDimensions = false;
      switch (displayRotation) {
        case Surface.ROTATION_0:
        case Surface.ROTATION_180:
          if (mSensorOrientation == 90 || mSensorOrientation == 270) {
            swappedDimensions = true;
          }
          break;
        case Surface.ROTATION_90:
        case Surface.ROTATION_270:
          if (mSensorOrientation == 0 || mSensorOrientation == 180) {
            swappedDimensions = true;
          }
          break;
        default:
          Log.e(TAG, "Display rotation is invalid: " + displayRotation);
      }

      Point displaySize = new Point();
      activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
      int rotatedPreviewWidth = width;
      int rotatedPreviewHeight = height;
      int maxPreviewWidth = displaySize.x;
      int maxPreviewHeight = displaySize.y;

      if (swappedDimensions) {
        rotatedPreviewWidth = height;
        rotatedPreviewHeight = width;
        maxPreviewWidth = displaySize.y;
        maxPreviewHeight = displaySize.x;
      }

      if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
        maxPreviewWidth = MAX_PREVIEW_WIDTH;
      }

      if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
        maxPreviewHeight = MAX_PREVIEW_HEIGHT;
      }

      // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
      // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
      // garbage capture data.
      mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
          rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
          maxPreviewHeight, largest);

      // We fit the aspect ratio of TextureView to the size of preview we picked.
      int orientation = getResources().getConfiguration().orientation;
//      if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//        mTextureView.setAspectRatio(
//            mPreviewSize.getWidth(), mPreviewSize.getHeight());
//      } else {
//        mTextureView.setAspectRatio(
//            mPreviewSize.getHeight(), mPreviewSize.getWidth());
//      }

        /*
    Whether the current camera device supports Flash or not.
   */
    } catch (CameraAccessException e) {
      e.printStackTrace();
    } catch (NullPointerException e) {
      // Currently an NPE is thrown when the Camera2API is used but not supported on the
      // device this code runs.
      ErrorDialog.newInstance(getString(R.string.camera_error))
          .show(getChildFragmentManager(), FRAGMENT_DIALOG);
    }
  }

  /**
   * Shows OK/Cancel confirmation dialog about camera permission.
   */
  public static class ConfirmationDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Fragment parent = getParentFragment();
      return new AlertDialog.Builder(getActivity())
          .setMessage(R.string.request_permission)
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//              FragmentCompat.requestPermissions(parent,
//                  new String[]{Manifest.permission.CAMERA},
//                  REQUEST_CAMERA_PERMISSION);
              parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                  REQUEST_CAMERA_PERMISSION);
            }
          })
          .setNegativeButton(android.R.string.cancel,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  Activity activity = parent.getActivity();
                  if (activity != null) {
                    activity.finish();
                  }
                }
              })
          .create();
    }
  }

  /**
   * Opens the camera specified by {@link VideoCallFragment#mCameraId}.
   */
  private void openCamera(int width, int height) {
    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED) {
      requestCameraPermission();
      return;
    }
    setUpCameraOutputs(width, height);
    configureTransform(width, height);
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw new RuntimeException("Time out waiting to lock camera opening.");
      }
      manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
    }
  }


   /**
   * Creates a new {@link CameraCaptureSession} for camera preview.
   */
  /**
  private void createCameraPreviewSession() {
    try {
      SurfaceTexture texture = mTextureView.getSurfaceTexture();
      assert texture != null;

      // We configure the size of default buffer to be the size of camera preview we want.
      texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

      // This is the output Surface we need to start preview.
      Surface surface = new Surface(texture);

      // We set up a CaptureRequest.Builder with the output Surface.
      mPreviewRequestBuilder
          = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      mPreviewRequestBuilder.addTarget(surface);

      // Here, we create a CameraCaptureSession for camera preview.
      mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
          new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
              // The camera is already closed
              if (null == mCameraDevice) {
                return;
              }

              // When the session is ready, we start displaying the preview.
              mCaptureSession = cameraCaptureSession;
              try {
                // Auto focus should be continuous for camera preview.
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // Finally, we start displaying the camera preview.
                mPreviewRequest = mPreviewRequestBuilder.build();
                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                    mCaptureCallback, mBackgroundHandler);
              } catch (CameraAccessException e) {
                e.printStackTrace();
              }
            }

            @Override
            public void onConfigureFailed(
                @NonNull CameraCaptureSession cameraCaptureSession) {
              Log.e(TAG, "Failed");
            }
          }, null
      );
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }
  */

  /**
   * Configures the necessary {@link Matrix} transformation to `mTextureView`.
   * This method should be called after the camera preview size is determined in
   * setUpCameraOutputs and also the size of `mTextureView` is fixed.
   *
   * @param viewWidth  The width of `mTextureView`
   * @param viewHeight The height of `mTextureView`
   */
  private void configureTransform(int viewWidth, int viewHeight) {
    Activity activity = getActivity();
    if (null == mTextureView || null == mPreviewSize || null == activity) {
      return;
    }
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    Matrix matrix = new Matrix();
    RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
    RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
    float centerX = viewRect.centerX();
    float centerY = viewRect.centerY();
    if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
      bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
      matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
      float scale = Math.max(
          (float) viewHeight / mPreviewSize.getHeight(),
          (float) viewWidth / mPreviewSize.getWidth());
      matrix.postScale(scale, scale, centerX, centerY);
      matrix.postRotate(90 * (rotation - 2), centerX, centerY);
    } else if (Surface.ROTATION_180 == rotation) {
      matrix.postRotate(180, centerX, centerY);
    }
//    mTextureView.setTransform(matrix);
  }

  /**
   * Closes the current {@link CameraDevice}.
   */
  private void closeCamera() {
    try {
      mCameraOpenCloseLock.acquire();
      if (null != mCaptureSession) {
        mCaptureSession.close();
        mCaptureSession = null;
      }
      if (null != mCameraDevice) {
        mCameraDevice.close();
        mCameraDevice = null;
      }

      if (null != mImageReader) {
        mImageReader.close();
        mImageReader = null;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
    } finally {
      mCameraOpenCloseLock.release();
    }
  }

  /**
   * Shows an error message dialog.
   */
  public static class ErrorDialog extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static ErrorDialog newInstance(String message) {
      ErrorDialog dialog = new ErrorDialog();
      Bundle args = new Bundle();
      args.putString(ARG_MESSAGE, message);
      dialog.setArguments(args);
      return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Activity activity = getActivity();
      return new AlertDialog.Builder(activity)
          .setMessage(getArguments().getString(ARG_MESSAGE))
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              activity.finish();
            }
          })
          .create();
    }

  }

  /**
   * Compares two {@code Size}s based on their areas.
   */
  static class CompareSizesByArea implements Comparator<Size> {

    @Override
    public int compare(Size lhs, Size rhs) {
      // We cast here to ensure the multiplications won't overflow
      return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
          (long) rhs.getWidth() * rhs.getHeight());
    }

  }

  private void requestCameraPermission() {
    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
      new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
    } else {
      requestPermissions(new String[]{Manifest.permission.CAMERA},
          REQUEST_CAMERA_PERMISSION);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    if (requestCode == REQUEST_CAMERA_PERMISSION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//        ErrorDialog.newInstance(getString(R.string.request_permission))
//            .show(getChildFragmentManager(), FRAGMENT_DIALOG);

        Intent intent = new Intent(getActivity(), DraggableService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

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
//        initializeView();
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
          Intent intent = new Intent(getActivity(), DraggableService.class);
          getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else {
          requestCameraPermission();
        }
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


  /// TODO video call
  private List<String> missingPermissions;
  private Intent intent = null;
// List of mandatory application permissions.
  private static final String[] MANDATORY_PERMISSIONS = {
      "android.permission.MODIFY_AUDIO_SETTINGS",
      "android.permission.RECORD_AUDIO",
      "android.permission.CAMERA",
      "android.permission.INTERNET"
  };

  private void initWS() {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    String keyprefFrom = getString(R.string.pref_from_key);
    String keyprefVideoCallEnabled = getString(R.string.pref_videocall_key);
    String keyprefResolution = getString(R.string.pref_resolution_key);
    String keyprefFps = getString(R.string.pref_fps_key);
    String keyprefCaptureQualitySlider = getString(R.string.pref_capturequalityslider_key);
    String keyprefVideoBitrateType = getString(R.string.pref_startvideobitrate_key);
    String keyprefVideoBitrateValue = getString(R.string.pref_startvideobitratevalue_key);
    String keyprefVideoCodec = getString(R.string.pref_videocodec_key);
    String keyprefHwCodecAcceleration = getString(R.string.pref_hwcodec_key);
    String keyprefCaptureToTexture = getString(R.string.pref_capturetotexture_key);
    String keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
    String keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
    String keyprefAudioCodec = getString(R.string.pref_audiocodec_key);
    String keyprefNoAudioProcessingPipeline = getString(R.string.pref_noaudioprocessing_key);
    String keyprefAecDump = getString(R.string.pref_aecdump_key);
    String keyprefOpenSLES = getString(R.string.pref_opensles_key);
    String keyprefDisplayHud = getString(R.string.pref_displayhud_key);
    String keyprefTracing = getString(R.string.pref_tracing_key);
    String keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
    String keyprefRoom = getString(R.string.pref_room_key);
    String keyprefRoomList = getString(R.string.pref_room_list_key);
    String from = sharedPref.getString(keyprefFrom, getString(R.string.pref_from_default));
    String roomUrl = sharedPref.getString(
        keyprefRoomServerUrl, getString(R.string.pref_room_server_url_default));
    roomUrl="wss://192.168.0.117:8898";
    // Video call enabled flag.
    boolean videoCallEnabled = sharedPref.getBoolean(keyprefVideoCallEnabled,
        Boolean.valueOf(getString(R.string.pref_videocall_default)));

    // Get default codecs.
    String videoCodec = sharedPref.getString(keyprefVideoCodec, getString(R.string.pref_videocodec_default));
    String audioCodec = sharedPref.getString(keyprefAudioCodec, getString(R.string.pref_audiocodec_default));

    // Check HW codec flag.
    boolean hwCodec = sharedPref.getBoolean(keyprefHwCodecAcceleration, Boolean.valueOf(getString(R.string.pref_hwcodec_default)));

    // Check Capture to texture.
    boolean captureToTexture = sharedPref.getBoolean(keyprefCaptureToTexture, Boolean.valueOf(getString(R.string.pref_capturetotexture_default)));

    // Check Disable Audio Processing flag.
    boolean noAudioProcessing = sharedPref.getBoolean(keyprefNoAudioProcessingPipeline, Boolean.valueOf(getString(R.string.pref_noaudioprocessing_default)));

    // Check Disable Audio Processing flag.
    boolean aecDump = sharedPref.getBoolean(keyprefAecDump, Boolean.valueOf(getString(R.string.pref_aecdump_default)));

    // Check OpenSL ES enabled flag.
    boolean useOpenSLES = sharedPref.getBoolean(
        keyprefOpenSLES,
        Boolean.valueOf(getString(R.string.pref_opensles_default)));

    // Check for mandatory permissions.
    int counter = 0;
    missingPermissions = new ArrayList<>();

    for (String permission : MANDATORY_PERMISSIONS) {
      if (getActivity().checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
        counter++;
        missingPermissions.add(permission);
      }
    }
    requestPermission();

    // Get video resolution from settings.
    int videoWidth = 0;
    int videoHeight = 0;
    String resolution = sharedPref.getString(keyprefResolution,
        getString(R.string.pref_resolution_default));
    String[] dimensions = resolution.split("[ x]+");
    if (dimensions.length == 2) {
      try {
        videoWidth = Integer.parseInt(dimensions[0]);
        videoHeight = Integer.parseInt(dimensions[1]);
      } catch (NumberFormatException e) {
        videoWidth = 0;
        videoHeight = 0;
        Log.e(TAG, "Wrong video resolution setting: " + resolution);
      }
    }

    // Get camera fps from settings.
    int cameraFps = 0;
    String fps = sharedPref.getString(keyprefFps,
        getString(R.string.pref_fps_default));
    String[] fpsValues = fps.split("[ x]+");
    if (fpsValues.length == 2) {
      try {
        cameraFps = Integer.parseInt(fpsValues[0]);
      } catch (NumberFormatException e) {
        Log.e(TAG, "Wrong camera fps setting: " + fps);
      }
    }

    // Check capture quality slider flag.
    boolean captureQualitySlider = sharedPref.getBoolean(keyprefCaptureQualitySlider,
        Boolean.valueOf(getString(R.string.pref_capturequalityslider_default)));

    // Get video and audio start bitrate.
    int videoStartBitrate = 0;
    String bitrateTypeDefault = getString(
        R.string.pref_startvideobitrate_default);
    String bitrateType = sharedPref.getString(
        keyprefVideoBitrateType, bitrateTypeDefault);
    if (!bitrateType.equals(bitrateTypeDefault)) {
      String bitrateValue = sharedPref.getString(keyprefVideoBitrateValue, getString(R.string.pref_startvideobitratevalue_default));
      videoStartBitrate = Integer.parseInt(bitrateValue);
    }
    int audioStartBitrate = 0;
    bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
    bitrateType = sharedPref.getString(
        keyprefAudioBitrateType, bitrateTypeDefault);
    if (!bitrateType.equals(bitrateTypeDefault)) {
      String bitrateValue = sharedPref.getString(keyprefAudioBitrateValue,
          getString(R.string.pref_startaudiobitratevalue_default));
      audioStartBitrate = Integer.parseInt(bitrateValue);
    }

    // Check statistics display option.
    boolean displayHud = sharedPref.getBoolean(keyprefDisplayHud, Boolean.valueOf(getString(R.string.pref_displayhud_default)));

    boolean tracing = sharedPref.getBoolean(keyprefTracing, Boolean.valueOf(getString(R.string.pref_tracing_default)));

//    Log.d(TAG, "Connecting from " + from + " at URL " + roomUrl);
//
//    if (validateUrl(roomUrl)) {
//      Uri uri = Uri.parse(roomUrl);
//      intent = new Intent(this, ConnectActivity.class);
//      intent.setData(uri);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, cameraFps);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
//      intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, videoStartBitrate);
//      intent.putExtra(CallActivity.EXTRA_VIDEOCODEC, videoCodec);
//      intent.putExtra(CallActivity.EXTRA_HWCODEC_ENABLED, hwCodec);
//      intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
//      intent.putExtra(CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
//      intent.putExtra(CallActivity.EXTRA_AECDUMP_ENABLED, aecDump);
//      intent.putExtra(CallActivity.EXTRA_OPENSLES_ENABLED, useOpenSLES);
//      intent.putExtra(CallActivity.EXTRA_AUDIO_BITRATE, audioStartBitrate);
//      intent.putExtra(CallActivity.EXTRA_AUDIOCODEC, audioCodec);
//      intent.putExtra(CallActivity.EXTRA_DISPLAY_HUD, displayHud);
//      intent.putExtra(CallActivity.EXTRA_TRACING, tracing);
//      intent.putExtra(CallActivity.EXTRA_CMDLINE, commandLineRun);
//      intent.putExtra(CallActivity.EXTRA_RUNTIME, runTimeMs);
//    }

    if (peerConnectionParameters == null) {
      peerConnectionParameters = new PeerConnectionClient.PeerConnectionParameters(
          videoCallEnabled,
          tracing,
          videoWidth, videoHeight, cameraFps, videoStartBitrate, videoCodec, hwCodec,
          captureToTexture, audioStartBitrate, audioCodec, noAudioProcessing,
          aecDump, useOpenSLES);
    }

    if (roomConnectionParameters == null) {
      roomConnectionParameters = new AppRTCClient.RoomConnectionParameters(Configs.ROOM_URL, from, false);
    }
  }

  public boolean validateUrl(String url) {
    //if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
    if (isWSUrl(url) || isWSSUrl(url)) {
      return true;
    }

    new AlertDialog.Builder(getActivity())
        .setTitle(getText(R.string.invalid_url_title))
        .setMessage(getString(R.string.invalid_url_text, url))
        .setCancelable(false)
        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
          }
        }).create().show();
    return false;
  }

  //http://stackoverflow.com/questions/35484767/activitycompat-requestpermissions-not-showing-dialog-box
  //https://developer.android.com/training/permissions/requesting.html
  private void requestPermission() {
    if (missingPermissions.size() > 0)
      requestPermissions(new String[]{missingPermissions.get(0)}, missingPermissions.size());
  }
}
