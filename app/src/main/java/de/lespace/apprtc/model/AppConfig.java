package de.lespace.apprtc.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Macbook on 3/16/17.
 */

public class AppConfig {
  @SerializedName("pc_config")
  private String pcConfig;

  public String getPcConfig() {
    return pcConfig;
  }

  public AppConfig setPcConfig(String pcConfig) {
    this.pcConfig = pcConfig;
    return this;
  }
}
