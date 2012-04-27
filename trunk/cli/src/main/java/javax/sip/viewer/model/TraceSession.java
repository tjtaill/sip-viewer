package javax.sip.viewer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TraceSession implements Comparable<TraceSession> {
  private Set<SipMessage> mSipMessageList = new TreeSet<SipMessage>();
  private List<String> mB2BTagTokens = new ArrayList<String>();
  private List<String> mCallIds = new ArrayList<String>();

  private long mTraceStartTime = 0;

  public TraceSession() {
    super();
  }

  public Set<SipMessage> getSipMessageList() {
    return mSipMessageList;
  }

  public void attach(SipMessage pSipMessage) {
    for (SipMessage message : mSipMessageList) {
      if (message.equals(pSipMessage)) {
        return;
      }
    }
    if (mTraceStartTime == 0) {
      mTraceStartTime = pSipMessage.getTime();
    }

    long delay = pSipMessage.getTime() - mTraceStartTime;
    if (delay <= 0) {
      mTraceStartTime = pSipMessage.getTime();
      calculateMessageDelay();
    } else {
      pSipMessage.setDelay(delay);
    }
    mSipMessageList.add(pSipMessage);
  }

  public void attachAll(Set<SipMessage> pSipMessages) {
    for (SipMessage message : pSipMessages) {
      attach(message);
    }
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
    return mTraceStartTime;
  }

  /**
   * @param pDelay
   */
  public void setTime(long pDelay) {
    mTraceStartTime = pDelay;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(TraceSession pTracesSession) {
    return (int) (this.getTime() - pTracesSession.getTime());
  }

  /**
   * calculate message delay
   */
  public void calculateMessageDelay() {
    for (SipMessage lSipMessage : mSipMessageList) {
      lSipMessage.setDelay(lSipMessage.getTime() - mTraceStartTime);
    }
  }

  /**
   * @param pTracesSession
   */
  public void mergeSession(TraceSession pTracesSession) {
    attachAll(pTracesSession.getSipMessageList());
    // change all newest session index to point to the old session.
    mCallIds.addAll(pTracesSession.getCallIds());
    mB2BTagTokens.addAll(pTracesSession.getB2BTagTokens());
  }

}
