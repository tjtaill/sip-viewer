package javax.sip.viewer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TracesSession implements Comparable<TracesSession>{
  private List<SipMessage> mSipMessageList = new ArrayList<SipMessage>();
  private List<String> mB2BTagTokens = new ArrayList<String>();
  private List<String> mCallIds = new ArrayList<String>();

  private long mTime;

  public TracesSession() {
    super();
  }

  public List<SipMessage> getSipMessageList() {
    return mSipMessageList;
  }

  public void attach(SipMessage pSipMessage) {
    if (mSipMessageList.isEmpty()) {
      mTime = pSipMessage.getTime();
    }
    pSipMessage.setDelay(pSipMessage.getTime() - mTime);
    mSipMessageList.add(pSipMessage);
  }

  public void attachAll(List<SipMessage> pSipMessages) {
    mSipMessageList.addAll(pSipMessages);
    calculateMessageDelay();
  }

  /**
   * @return the B2BTagTokens
   */
  public List<String> getB2BTagTokens() {
    return mB2BTagTokens;
  }

  /**
   * @param pB2BTagTokens the mB2BTagTokens to set
   */
  public void addB2BTagTokens(String pB2BTagTokens) {
    if (!mB2BTagTokens.contains(pB2BTagTokens)) {
      mB2BTagTokens.add(pB2BTagTokens);
    }
  }

  /**
   * @return the callIds
   */
  public List<String> getCallIds() {
    return mCallIds;
  }

  /**
   * @param pCallIds the callIds to set
   */
  public void addCallId(String pCallId) {
    if (!mCallIds.contains(pCallId)) {
      mCallIds.add(pCallId);
    }
  }

  /**
   * @return the time of the first message
   */
  public long getTime() {
    return mTime;
  }

  /**
   * @param pDelay
   */
  public void setTime(long pDelay) {
    mTime = pDelay;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(TracesSession pTracesSession) {
    return (int)(this.getTime() - pTracesSession.getTime());
  }

  /**
   * calculate message delay
   */
  public void calculateMessageDelay() {
    for (SipMessage lSipMessage : mSipMessageList) {
      lSipMessage.setDelay(lSipMessage.getTime() - mTime);
    }
  }

  /**
   * @param pTracesSession
   */
  public void mergeSession(TracesSession pTracesSession) {
    attachAll(pTracesSession.getSipMessageList());
    // change all newest session index to point to the old session.
    mCallIds.addAll(pTracesSession.getCallIds());
    mB2BTagTokens.addAll(pTracesSession.getB2BTagTokens());
  }

}
