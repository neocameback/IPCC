package com.viettel.ipcclib.constants;

import com.google.gson.annotations.SerializedName;

/**
 * Created by neo on 3/16/17.
 */

public enum MessageType {
  @SerializedName("0")
  DEFAULT(0),

  @SerializedName("1")
  PING(1),

  @SerializedName("2")
  AGENT_LOGIN(2),

  @SerializedName("3")
  LOGOUT(3),

  @SerializedName("4")
  HAVE_MESSAGE(4),

  @SerializedName("5")
  MESSAGE(5),

  @SerializedName("7")
  SET_CONVERSATION(7),

  @SerializedName("8")
  GET_CONVERSATION(8),

  @SerializedName("9")
  LEAVE_CONVERSATION(9),

  @SerializedName("10")
  JOIN_CONVERSATION(10),

  @SerializedName("11")
  INVITE_CONVERSATION(11),

  @SerializedName("12")
  INVITE_CONVERSATION_RESPONSE(12),

  @SerializedName("13")
  TRANFER_CONVERSATION(13),

  @SerializedName("14")
  TRANFER_CONVERSATION_RESPONSE(14),

  @SerializedName("15")
  GET_CONVERSATION_LIST(15),

  @SerializedName("16")
  GET_CONVERSATION_DETAIL(16),

  @SerializedName("17")
  CREATE_SHORTCUT(17),

  @SerializedName("18")
  DELETE_SHORTCUT(18),

  @SerializedName("19")
  NOTIFY(19),

  @SerializedName("20")
  SEND_FILE(20),

  @SerializedName("21")
  RECEIVE_FILE(21),

  @SerializedName("22")
  SEND_FILE_CALCEL(22),

  @SerializedName("23")
  USER_INFO(23),

  @SerializedName("24")
  MAKE_NOTE(24),

  @SerializedName("25")
  GET_PAST_CONVERSATION_LIST(25),

  @SerializedName("26")
  GET_PAST_CONVERSATION_DETAIL(26),

  @SerializedName("27")
  BAN_VISITOR_ID(27),

  @SerializedName("28")
  BAN_IP(28),

  @SerializedName("29")
  BAN_VERIFIED_VISITOR_ID(29),

  @SerializedName("30")
  ATTENTION(30),

  @SerializedName("31")
  ERROR_LOGGED_OTHER_CHANNEL(31),

  @SerializedName("32")
  HAVE_INVITE_CONVERSATION(32),

  @SerializedName("33")
  HAVE_SET_CONVERSATION(33),

  @SerializedName("34")
  USER_JOIN_CONVERSATION(34),

  @SerializedName("35")
  HAVE_TRANFER_CONVERSATION(35),

  @SerializedName("36")
  HAVE_INVITE_RESPONSE(36),

  @SerializedName("37")
  CUSTOMER_LOGIN(37),

  @SerializedName("38")
  HAVE_TRANFER_RESPONSE(38),

  @SerializedName("39")
  WEB_PUSH_CONNECT(39),

  @SerializedName("40")
  LOGIN_OTHER_SERVER(40),

  @SerializedName("41")
  UPDATE_VISITOR_INFO(41),
  //HuyDN

  @SerializedName("42")
  AGENT_NOT_RESPONSE_INCOMING_CHAT(42),

  @SerializedName("43")
  AGENT_RESPONSE_INCOMING_CHAT(43),

  @SerializedName("44")
  AGENT_GET_CONV_HISTORY(44),

  @SerializedName("45")
  AGENT_GET_MESSAGE_HISTORY(45),

  @SerializedName("46")
  AGENT_HOLD(46),

  @SerializedName("47")
  AGENT_UNHOLD(47),

  @SerializedName("48")
  AGENT_CHAT_FORM(48),

  @SerializedName("49")
  AGENT_END_CONVERSATION(49),

  @SerializedName("50")
  AGENT_FRIEND_LIST(50),

  @SerializedName("51")
  AGENT_CREATE_CONVERSATION(51),

  @SerializedName("52")
  AGENT_MSG_CONVERSATION(52),

  @SerializedName("53")
  GET_LIST_AGENT(53),

  @SerializedName("54")
  TRANSFER_CHAT_TO_AGENT(54),//thuc hien chuyen chat den agent khac

  @SerializedName("55")
  TRANSFER_CHAT_TO_AGENT_RESULT(55),

  @SerializedName("56")
  RESPONSE_TRANSFER_CHAT_TO_AGENT(56),//tra loi co dong y hay khong

  @SerializedName("57")
  INVITE_AGENT_TO_CONVERSATION(57),

  @SerializedName("58")
  INVITE_AGENT_TO_CONVERSATION_RESULT(58),

  @SerializedName("59")
  RESPONSE_INVITE_AGENT_TO_CONVERSATION(59),

  @SerializedName("60")
  FORCE_INVITE_AGENT(60),

  @SerializedName("61")
  FORCE_INVITE_AGENT_RESULT(61),

  @SerializedName("62")
  FRIEND_LIST_UPDATE(62),

  @SerializedName("63")
  CONVERSATION_UPDATE(63),

  @SerializedName("64")
  AGENT_LEFT_CONVERSATION(64),

  @SerializedName("65")
  ALERT_MSG(65),

  @SerializedName("66")
  AGENT_HAS_CUSTOMER_SUPERVISOR(66),

  @SerializedName("67")
  AGENT_RESPONSE_INCOMING_CHAT_SUPERVISOR(67),

  @SerializedName("68")
  AGENT_END_CONVERSATION_SUPERVISOR(68),

  @SerializedName("69")
  CUSTOMER_END_CONVERSATION(69), //ban tin customer ket thuc conversation gui cho agent

  @SerializedName("70")
  CUSTOMER_END_CONVERSATION_SUPERVISOR(70), //ban tin customer ket thuc conversation gui cho supervisor

  @SerializedName("71")
  AGENT_END_CONVERSATION_CUSTOMER(71), //ban tin agent ket thuc conversation gui cho customer

  @SerializedName("72")
  AGENT_INFO_CUSTOMER(72), //ban tin thong tin agent gui cho customer

  @SerializedName("73")
  AGENT_NOT_RESPONSE_INCOMING_CHAT_SUPERVISOR(73),

  @SerializedName("74")
  AGENT_HOLD_SUPERVISOR(74),// truong hop tvv hold

  @SerializedName("75")
  AGENT_UNHOLD_SUPERVISOR(75),// truong hop tvv unhold

  @SerializedName("76")
  RESPONSE_AGENT_MISS_CHAT_TO_CUSTOMER(76), //ban tin misschat gui lai cho customer

  @SerializedName("77")
  AGENT_HAS_CUSTOMER(77),

  @SerializedName("78")
  RESPONSE_CUSTOMER_GET_HISTORY_CHAT(78),

  @SerializedName("79")
  CUSTOMER_GET_HISTORY_CHAT(79),

  @SerializedName("80")
  GET_SERVICE_LIST(80), //web client request danh sach service

  @SerializedName("81")
  RESPONSE_SERVICE_LIST(81), //tra ve danh sach service cho web clie

  @SerializedName("82")
  CHAT_REQUEST_ENQUEUE_AMCD_SUPERVISOR(82),

  @SerializedName("83")
  CHAT_SUPER_SPY(83),

  @SerializedName("84")
  CHAT_SUPER_JOIN(84),

  @SerializedName("85")
  UPDATE_TEMPLATE(85),

  @SerializedName("86")
  CUSTOMER_RESUMING_REQUEST(86),

  @SerializedName("87")
  CUSTOMER_RESUMING_RESPONSE(87),

  @SerializedName("88")
  NOTIFY_TYPING(88),

  @SerializedName("89")
  LIKE(89),

  @SerializedName("90")
  DISLIKE(90),

  @SerializedName("91")
  CHAT_SUPER_SPY_END(91),

  @SerializedName("92")
  CHAT_SUPER_SUPPORT_END(92),

  @SerializedName("93")
  TRANSFER_CHAT_RESPONSE(93),

  @SerializedName("94")
  SUPPORT_CHAT_RESPONSE(94),

  @SerializedName("95")
  AGENT_LOGOUT(95),

  @SerializedName("96")
  AGENT_HAS_CUSTOMER_TRANSFER_SUPERVISOR(96),

  @SerializedName("97")
  CHAT_SUPER_JOIN_END(97),

  @SerializedName("98")
  CUSTOMER_COMMENT(98),

  @SerializedName("99")
  RESPONSE_TRANSFER_CHAT_TO_AGENT_RESULT(99),//phan hoi lai nguoi duoc transfer ket qua

  @SerializedName("100")
  OFFLINE_MESSAGE_RESPONSE(100),

  @SerializedName("101")
  BAN_CUSTOMER(101),

  @SerializedName("102")
  AGENT_RESUME_RESPONSE(102),

  @SerializedName("103")
  END_INTERNAL_CHAT(103),

  @SerializedName("104")
  GET_INTERNAL_HISTORY(104),

  @SerializedName("105")
  TRANSFER_CHAT_TIMEOUT(105),

  @SerializedName("106")
  SEND_IS_CHAT_TYPING(106),

  @SerializedName("107")
  WARNING_CUSTOMER_TIMEOUT(107),

  @SerializedName("108")
  CUSTOMER_TIMEOUT_CONVERSATION(108),

  @SerializedName("109")
  CUSTOMER_TIMEOUT_CONVERSATION_SUPERVISOR(109),

  @SerializedName("110")
  CUSTOMER_UPDATE_USER_INFO_ERROR(110),

  @SerializedName("111")
  HOLD_UNHOLD_CHAT(111),

  @SerializedName("112")
  NOTIFY_HOLD_CHAT(112),

  @SerializedName("113")
  VIDEO_CALL(113),

  @SerializedName("114")
  AGENT_HAS_VIDEO_CALL(114),

  @SerializedName("115")
  AGENT_RESPONSE_INCOMING_VIDEO_CALL(115),

  @SerializedName("116")
  AGENT_NOT_RESPONSE_INCOMING_VIDEO_CALL(116),

  @SerializedName("117")
  RESPONSE_AGENT_MISS_VIDEO_CALL_TO_CUSTOMER(117),

  @SerializedName("118")
  AGENT_RESPONSE_BUSY_VIDEO_CALL(118);

  //====================== END DEFINAION ====================//

  private int extension;

  MessageType(int extension) {
    this.extension = extension;
  }

  public int getExtension() {
    return extension;
  }

  public MessageType setExtension(int extension) {
    this.extension = extension;
    return this;
  }

  public static MessageType getValue(int value) {
    for (MessageType type : MessageType.values()) {
      if (value == type.extension) {
        return type;
      }
    }

    return null;
  }
}
