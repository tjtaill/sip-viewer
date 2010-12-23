package javax.sip.viewer.model;

import java.io.Serializable;

/**
 * Représente vaguement un log d'un message SIP.
 */
public class SipMessage implements Serializable {

  private String messageFrom = null;
  private String messageTo = null;
  private long messageDelay;
  private long time;
  private String content = "";
  private String messageFirstLine = null;
  private String messageStatusInfo = null;
  private String messageTransactionId = null;
  private String mCallId;
  private boolean mSender;

  public SipMessage() {
  }

  public void setFrom(String from) {
    messageFrom = from;
  }

  public void setTo(String to) {
    messageTo = to;
  }

  public void setDelay(long delay) {
    messageDelay = delay;

  }

  public void setMessageString(String str) {
    content = str;
  }

  public void setFirstLine(String FirstLine) {
    messageFirstLine = FirstLine;
  }

  public void setStatusInfo(String statusInfo) {
    messageStatusInfo = statusInfo;
  }

  public void setTransactionId(String transactionId) {
    messageTransactionId = transactionId;
  }

  public String getFrom() {
    return messageFrom;
  }

  public String getTo() {
    return messageTo;
  }

  public long getDelay() {
    return messageDelay;
  }

  public String getContent() {
    return content;
  }

  public String getFirstLine() {
    return messageFirstLine;
  }

  public String getStatusInfo() {
    return messageStatusInfo;
  }

  public String getTransactionId() {
    return messageTransactionId;
  }

  public void setCallId(String pCallId) {
    mCallId = pCallId;

  }

  public void setSender(boolean pSender) {
    mSender = pSender;
  }

  public boolean isSender() {
    return mSender;
  }

  public String getCallId() {
    return mCallId;
  }

  public void setContent(String pContent) {
    content = pContent;
  }
  
  public void setTime(long pTime) {
    time = pTime;
  }
  
  public long getTime() {
    return time;
  }
}
