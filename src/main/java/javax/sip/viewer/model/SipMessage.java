package javax.sip.viewer.model;

import java.io.Serializable;

public class SipMessage implements Serializable {
  private String mMessageAsText = "";
  private String mSource = null;
  private String mDestination = null;
  private long mDelay;
  private long mTime;

  public void setSource(String pSource) {
    mSource = pSource;
  }

  public void setDestination(String pDestination) {
    mDestination = pDestination;
  }

  public void setDelay(long delay) {
    mDelay = delay;
  }

  public void setMessageAsText(String pText) {
    mMessageAsText = pText;
  }

  public String getSource() {
    return mSource;
  }

  public String getDestination() {
    return mDestination;
  }

  public long getDelay() {
    return mDelay;
  }

  public String getMessageAsText() {
    return mMessageAsText;
  }

  public String getFirstLine() {
    return mMessageAsText.split("\n")[0];
  }

  public void setTime(long pTime) {
    mTime = pTime;
  }

  public long getTime() {
    return mTime;
  }

}
