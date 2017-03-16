package de.lespace.apprtc.constants;

import com.google.gson.Gson;

/**
 * Created by Macbook on 3/16/17.
 */

public class GsonWrapper {
  private static Gson mGson;

  public static Gson getGson() {
    if (mGson == null) {
      mGson = new Gson();
    }

    return mGson;
  }
}
