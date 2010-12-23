package javax.sip.viewer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TracesSession {
  private Set<SipMessage> mSipMessageList = new TreeSet<SipMessage>(new LogComparator());
  private List<String> mB2BTagTokens = new ArrayList<String>();
  private List<String> mCallIds = new ArrayList<String>();

  private long mTime;

  public TracesSession() {
    super();
  }

  public List<SipMessage> getSipMessageList() {
    return new ArrayList<SipMessage>(mSipMessageList);
  }

  public void add(SipMessage pSipMessage) {
    mSipMessageList.add(pSipMessage);
  }

  public void addAll(List<SipMessage> pSipMessages) {
    mSipMessageList.addAll(pSipMessages);
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

  public void setTime(long pTime) {
    mTime = pTime;
  }

  public long getTime() {
    return mTime;
  }
}
