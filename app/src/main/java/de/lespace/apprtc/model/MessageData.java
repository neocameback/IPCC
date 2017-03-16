package de.lespace.apprtc.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import de.lespace.apprtc.constants.MessageType;

/**
 * Created by Macbook on 3/16/17.
 */

public class MessageData {
  @SerializedName("visitorName")
  private String visitorName;

  @SerializedName("visitorId")
  private int visitorId;

  @SerializedName("service_id")
  private String serviceId;

  @SerializedName("conversationId")
  private int conversationId;

  @SerializedName("domain")
  private String domain;

  @SerializedName("ip_address")
  private String ipAddress;

  @SerializedName("country_name")
  private String countryName;

  @SerializedName("host")
  private String host;

  @SerializedName("os")
  private String os;

  @SerializedName("browser")
  private String browser;

  @SerializedName("deviceType")
  private String deviceType;

  @SerializedName("message")
  private String message;

  @SerializedName("name")
  private JsonElement name;

  @SerializedName("userId")
  private int userId;

  @SerializedName("userName")
  private String userName;

  @SerializedName("username")
  private String username;

  @SerializedName("r")
  private int result;

  @SerializedName("email")
  private String email;

  @SerializedName("needShow")
  private boolean needShow;

  @SerializedName("isTyping")
  private boolean isTyping;

  @SerializedName("id")
  private String id;

  @SerializedName("agentId")
  private String agentId;

  @SerializedName("service_name")
  private String service_name;

  @SerializedName("fullName")
  private String fullName;

  @SerializedName("avatar")
  private String avatar;

  @SerializedName("conversationType")
  private String conversationType;

  @SerializedName("fileName")
  private String fileName;

  @SerializedName("fromFullname")
  private String fromFullname;

  @SerializedName("fromUserId")
  private String fromUserId;

  @SerializedName("fileUrl")
  private String fileUrl;

  @SerializedName("candidate")
  private CandidateJ candidate;

  @SerializedName("sdpAnswer")
  private String sdpAnswer;

  @SerializedName("sender")
  private String sender;

  @SerializedName("sdpOffer")
  private String sdpOffer;

  @SerializedName("type")
  private MessageType type;

  public String getVisitorName() {
    return visitorName;
  }

  public MessageData setVisitorName(String visitorName) {
    this.visitorName = visitorName;
    return this;
  }

  public int getVisitorId() {
    return visitorId;
  }

  public MessageData setVisitorId(int visitorId) {
    this.visitorId = visitorId;
    return this;
  }

  public String getServiceId() {
    return serviceId;
  }

  public MessageData setServiceId(String serviceId) {
    this.serviceId = serviceId;
    return this;
  }

  public int getConversationId() {
    return conversationId;
  }

  public MessageData setConversationId(int conversationId) {
    this.conversationId = conversationId;
    return this;
  }

  public String getDomain() {
    return domain;
  }

  public MessageData setDomain(String domain) {
    this.domain = domain;
    return this;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public MessageData setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
    return this;
  }

  public String getCountryName() {
    return countryName;
  }

  public MessageData setCountryName(String countryName) {
    this.countryName = countryName;
    return this;
  }

  public String getHost() {
    return host;
  }

  public MessageData setHost(String host) {
    this.host = host;
    return this;
  }

  public String getOs() {
    return os;
  }

  public MessageData setOs(String os) {
    this.os = os;
    return this;
  }

  public String getBrowser() {
    return browser;
  }

  public MessageData setBrowser(String browser) {
    this.browser = browser;
    return this;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public MessageData setDeviceType(String deviceType) {
    this.deviceType = deviceType;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public MessageData setMessage(String message) {
    this.message = message;
    return this;
  }

  public JsonElement getName() {
    return name;
  }

  public MessageData setName(JsonElement name) {
    this.name = name;
    return this;
  }

  public int getUserId() {
    return userId;
  }

  public MessageData setUserId(int userId) {
    this.userId = userId;
    return this;
  }

  public String getUserName() {
    return userName;
  }

  public MessageData setUserName(String userName) {
    this.userName = userName;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public MessageData setUsername(String username) {
    this.username = username;
    return this;
  }

  public int getResult() {
    return result;
  }

  public MessageData setResult(int result) {
    this.result = result;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public MessageData setEmail(String email) {
    this.email = email;
    return this;
  }

  public boolean isNeedShow() {
    return needShow;
  }

  public MessageData setNeedShow(boolean needShow) {
    this.needShow = needShow;
    return this;
  }

  public boolean isTyping() {
    return isTyping;
  }

  public MessageData setTyping(boolean typing) {
    isTyping = typing;
    return this;
  }

  public String getId() {
    return id;
  }

  public MessageData setId(String id) {
    this.id = id;
    return this;
  }

  public String getAgentId() {
    return agentId;
  }

  public MessageData setAgentId(String agentId) {
    this.agentId = agentId;
    return this;
  }

  public String getService_name() {
    return service_name;
  }

  public MessageData setServiceName(String service_name) {
    this.service_name = service_name;
    return this;
  }

  public String getFullName() {
    return fullName;
  }

  public MessageData setFullName(String fullName) {
    this.fullName = fullName;
    return this;
  }

  public String getAvatar() {
    return avatar;
  }

  public MessageData setAvatar(String avatar) {
    this.avatar = avatar;
    return this;
  }

  public String getConversationType() {
    return conversationType;
  }

  public MessageData setConversationType(String conversationType) {
    this.conversationType = conversationType;
    return this;
  }

  public String getFileName() {
    return fileName;
  }

  public MessageData setFileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  public String getFromFullname() {
    return fromFullname;
  }

  public MessageData setFromFullname(String fromFullname) {
    this.fromFullname = fromFullname;
    return this;
  }

  public String getFromUserId() {
    return fromUserId;
  }

  public MessageData setFromUserId(String fromUserId) {
    this.fromUserId = fromUserId;
    return this;
  }

  public String getFileUrl() {
    return fileUrl;
  }

  public MessageData setFileUrl(String fileUrl) {
    this.fileUrl = fileUrl;
    return this;
  }

  public MessageType getType() {
    return type;
  }

  public MessageData setType(MessageType type) {
    this.type = type;
    return this;
  }

  public CandidateJ getCandidate() {
    return candidate;
  }

  public MessageData setCandidate(CandidateJ candidate) {
    this.candidate = candidate;
    return this;
  }

  public String getSdpAnswer() {
    return sdpAnswer;
  }

  public MessageData setSdpAnswer(String sdpAnswer) {
    this.sdpAnswer = sdpAnswer;
    return this;
  }

  public String getSender() {
    return sender;
  }

  public MessageData setSender(String sender) {
    this.sender = sender;
    return this;
  }

  public String getSdpOffer() {
    return sdpOffer;
  }

  public MessageData setSdpOffer(String sdpOffer) {
    this.sdpOffer = sdpOffer;
    return this;
  }
}
