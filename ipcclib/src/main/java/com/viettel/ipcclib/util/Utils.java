package com.viettel.ipcclib.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Macbook on 3/17/17.
 */

public class Utils {
  private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
  public static boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
  }

  public static String formatTime(Date date) {
    return TIME_FORMAT.format(date);
  }

  public static void hideKeyBoard(Context context) {
    // Check if no view has focus:
    View view = ((Activity) context).getCurrentFocus();
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  public static void hideKeyBoard(View v) {
    // Check if no view has focus:
    if (v != null) {
      InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
  }
}
