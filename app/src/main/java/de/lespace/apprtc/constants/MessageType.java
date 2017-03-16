package de.lespace.apprtc.constants;

/**
 * Created by neo on 3/16/17.
 */

public enum MessageType {
  DEFAULT(0),
  PING(1),
  AGENT_LOGIN(2),
  LOGOUT(3),
  HAVE_MESSAGE(4),
  MESSAGE(5),
  SET_CONVERSATION(7),
  GET_CONVERSATION(8),
  LEAVE_CONVERSATION(9),
  JOIN_CONVERSATION(10),
  INVITE_CONVERSATION(11),
  INVITE_CONVERSATION_RESPONSE(12),
  TRANFER_CONVERSATION(13),
  TRANFER_CONVERSATION_RESPONSE(14),
  GET_CONVERSATION_LIST(15),
  GET_CONVERSATION_DETAIL(16),
  CREATE_SHORTCUT(17),
  DELETE_SHORTCUT(18),
  NOTIFY(19),
  SEND_FILE(20),
  RECEIVE_FILE(21),
  SEND_FILE_CALCEL(22),
  USER_INFO(23),
  MAKE_NOTE(24),
  GET_PAST_CONVERSATION_LIST(25),
  GET_PAST_CONVERSATION_DETAIL(26),
  BAN_VISITOR_ID(27),
  BAN_IP(28),
  BAN_VERIFIED_VISITOR_ID(29),
  ATTENTION(30),
  ERROR_LOGGED_OTHER_CHANNEL(31),
  HAVE_INVITE_CONVERSATION(32),
  HAVE_SET_CONVERSATION(33),
  USER_JOIN_CONVERSATION(34),
  HAVE_TRANFER_CONVERSATION(35),
  HAVE_INVITE_RESPONSE(36),
  CUSTOMER_LOGIN(37),
  HAVE_TRANFER_RESPONSE(38),
  WEB_PUSH_CONNECT(39),
  LOGIN_OTHER_SERVER(40),
  UPDATE_VISITOR_INFO(41),
  //HuyDN
  AGENT_NOT_RESPONSE_INCOMING_CHAT(42),
  AGENT_RESPONSE_INCOMING_CHAT(43),
  AGENT_GET_CONV_HISTORY(44),
  AGENT_GET_MESSAGE_HISTORY(45),
  AGENT_HOLD(46),
  AGENT_UNHOLD(47),
  AGENT_CHAT_FORM(48),
  AGENT_END_CONVERSATION(49),
  AGENT_FRIEND_LIST(50),
  AGENT_CREATE_CONVERSATION(51),
  AGENT_MSG_CONVERSATION(52),
  GET_LIST_AGENT(53),
  TRANSFER_CHAT_TO_AGENT(54),//thuc hien chuyen chat den agent khac
  TRANSFER_CHAT_TO_AGENT_RESULT(55),
  RESPONSE_TRANSFER_CHAT_TO_AGENT(56),//tra loi co dong y hay khong
  INVITE_AGENT_TO_CONVERSATION(57),
  INVITE_AGENT_TO_CONVERSATION_RESULT(58),
  RESPONSE_INVITE_AGENT_TO_CONVERSATION(59),
  FORCE_INVITE_AGENT(60),
  FORCE_INVITE_AGENT_RESULT(61),
  FRIEND_LIST_UPDATE(62),
  CONVERSATION_UPDATE(63),
  AGENT_LEFT_CONVERSATION(64),
  ALERT_MSG(65),
  AGENT_HAS_CUSTOMER_SUPERVISOR(66),
  AGENT_RESPONSE_INCOMING_CHAT_SUPERVISOR(67),
  AGENT_END_CONVERSATION_SUPERVISOR(68),
  CUSTOMER_END_CONVERSATION(69), //ban tin customer ket thuc conversation gui cho agent
  CUSTOMER_END_CONVERSATION_SUPERVISOR(70), //ban tin customer ket thuc conversation gui cho supervisor
  AGENT_END_CONVERSATION_CUSTOMER(71), //ban tin agent ket thuc conversation gui cho customer
  AGENT_INFO_CUSTOMER(72), //ban tin thong tin agent gui cho customer
  AGENT_NOT_RESPONSE_INCOMING_CHAT_SUPERVISOR(73),
  AGENT_HOLD_SUPERVISOR(74),// truong hop tvv hold
  AGENT_UNHOLD_SUPERVISOR(75),// truong hop tvv unhold
  RESPONSE_AGENT_MISS_CHAT_TO_CUSTOMER(76), //ban tin misschat gui lai cho customer
  AGENT_HAS_CUSTOMER(77),
  RESPONSE_CUSTOMER_GET_HISTORY_CHAT(78),
  CUSTOMER_GET_HISTORY_CHAT(79),
  GET_SERVICE_LIST(80), //web client request danh sach service
  RESPONSE_SERVICE_LIST(81), //tra ve danh sach service cho web clie
  CHAT_REQUEST_ENQUEUE_AMCD_SUPERVISOR(82),
  CHAT_SUPER_SPY(83),
  CHAT_SUPER_JOIN(84),
  UPDATE_TEMPLATE(85),
  CUSTOMER_RESUMING_REQUEST(86),
  CUSTOMER_RESUMING_RESPONSE(87),
  NOTIFY_TYPING(88),
  LIKE(89),
  DISLIKE(90),
  CHAT_SUPER_SPY_END(91),
  CHAT_SUPER_SUPPORT_END(92),
  TRANSFER_CHAT_RESPONSE(93),
  SUPPORT_CHAT_RESPONSE(94),
  AGENT_LOGOUT(95),
  AGENT_HAS_CUSTOMER_TRANSFER_SUPERVISOR(96),
  CHAT_SUPER_JOIN_END(97),
  CUSTOMER_COMMENT(98),
  RESPONSE_TRANSFER_CHAT_TO_AGENT_RESULT(99),//phan hoi lai nguoi duoc transfer ket qua
  OFFLINE_MESSAGE_RESPONSE(100),
  BAN_CUSTOMER(101),
  AGENT_RESUME_RESPONSE(102),
  END_INTERNAL_CHAT(103),
  GET_INTERNAL_HISTORY(104),
  TRANSFER_CHAT_TIMEOUT(105),
  SEND_IS_CHAT_TYPING(106),
  WARNING_CUSTOMER_TIMEOUT(107),
  CUSTOMER_TIMEOUT_CONVERSATION(108),
  CUSTOMER_TIMEOUT_CONVERSATION_SUPERVISOR(109),
  CUSTOMER_UPDATE_USER_INFO_ERROR(110),
  HOLD_UNHOLD_CHAT(111),
  NOTIFY_HOLD_CHAT(112),
  VIDEO_CALL(113),
  AGENT_HAS_VIDEO_CALL(114),
  AGENT_RESPONSE_INCOMING_VIDEO_CALL(115),
  AGENT_NOT_RESPONSE_INCOMING_VIDEO_CALL(116),
  RESPONSE_AGENT_MISS_VIDEO_CALL_TO_CUSTOMER(117),
  AGENT_RESPONSE_BUSY_VIDEO_CALL(118);

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
}
