package com.viettel.ipcclib.util;

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
}
