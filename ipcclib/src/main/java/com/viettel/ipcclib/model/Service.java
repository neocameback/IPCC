package com.viettel.ipcclib.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Macbook on 3/16/17.
 */

public class Service {
  @SerializedName("service_name")
  private String serviceName;

  @SerializedName("service_id")
  private String serviceId;

  public String getServiceName() {
    return serviceName;
  }

  public Service setServiceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public String getServiceId() {
    return serviceId;
  }

  public Service setServiceId(String serviceId) {
    this.serviceId = serviceId;
    return this;
  }
}
