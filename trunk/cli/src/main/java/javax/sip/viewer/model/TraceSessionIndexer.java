package javax.sip.viewer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @copyright ©2011, Bell Canada
 * @author jonathan.agaliotis (CTS)
 */
public class TraceSessionIndexer {

  private List<TraceSession> mTraceSessions = new ArrayList<TraceSession>(100000);
  private Map<String, TraceSession> mTraceSessionIndex = new TreeMap<String, TraceSession>();

  /**
   * @return
   */
  public List<TraceSession> getTraceSessions() {
    return mTraceSessions;
  }

  /**
   * @param pSipMessage
   * @param pToTagToken
   * @param pFromTagToken
   * @param pCallId
   * @return
   */
  public void indexSipMessage(SipMessage pSipMessage,
                              String pToTagToken,
                              String pFromTagToken,
                              String pFromTag,
                              String pCallId,
                              String pEventId)
  {
    // look for existing session (or create one)
    List<TraceSession> lTraceSessions = findTraceSessions(pToTagToken,
                                                          pFromTagToken,
                                                          pFromTag,
                                                          pCallId,
                                                          pEventId);

    TraceSession lTraceSession;

    if (lTraceSessions.size() > 1) {
      lTraceSession = mergeSessions(lTraceSessions);
    } else {
      lTraceSession = lTraceSessions.get(0);
    }

    // add the sip message in the session
    lTraceSession.attach(pSipMessage);

    // leverage session's indices
    lTraceSession.addCallId(pCallId);
    mTraceSessionIndex.put(pCallId, lTraceSession);
    if (pToTagToken != null) {
      lTraceSession.addB2BTagTokens(pToTagToken);
      mTraceSessionIndex.put(pToTagToken, lTraceSession);
    }
    if (pFromTagToken != null) {
      lTraceSession.addB2BTagTokens(pFromTagToken);
      mTraceSessionIndex.put(pFromTagToken, lTraceSession);
    }
    if (pFromTag != null) {
      lTraceSession.addFromTag(pFromTag);
      mTraceSessionIndex.put(pFromTag, lTraceSession);
    }
  }

  /**
   * According to session indices (callid, to-tag and from-tag), search for an already existing
   * session. If none is found, a new session is created.
   * 
   * @param pToTagToken
   * @param pFromTagToken
   * @param pCallId
   * @return a TraceSessionList that contains at least 1 session.
   */
  private List<TraceSession> findTraceSessions(String pToTagToken,
                                               String pFromTagToken,
                                               String pFromTag,
                                               String pCallId,
                                               String pEventId)
  {
    List<TraceSession> lResult = new ArrayList<TraceSession>();

    if (pToTagToken != null && mTraceSessionIndex.containsKey(pToTagToken)) {
      if (!lResult.contains(mTraceSessionIndex.get(pToTagToken))) {
        lResult.add(mTraceSessionIndex.get(pToTagToken));
      }
    }
    if (pFromTagToken != null && mTraceSessionIndex.containsKey(pFromTagToken)) {
      if (!lResult.contains(mTraceSessionIndex.get(pFromTagToken))) {
        lResult.add(mTraceSessionIndex.get(pFromTagToken));
      }
    }
    if (pFromTag != null && mTraceSessionIndex.containsKey(pFromTag)) {
      if (!lResult.contains(mTraceSessionIndex.get(pFromTag))) {
        lResult.add(mTraceSessionIndex.get(pFromTag));
      }
    }
    if (pCallId != null && mTraceSessionIndex.containsKey(pCallId)) {
      if (!lResult.contains(mTraceSessionIndex.get(pCallId))) {
        lResult.add(mTraceSessionIndex.get(pCallId));
      }
    }
    if (pEventId != null && mTraceSessionIndex.containsKey(pEventId)) {
      if (!lResult.contains(mTraceSessionIndex.get(pEventId))) {
        lResult.add(mTraceSessionIndex.get(pEventId));
      }
    }

    if (lResult.size() == 0) {
      lResult.add(new TraceSession());
      mTraceSessions.add(lResult.get(0));
    }
    return lResult;
  }

  /**
   * @param pFromTagSession
   * @param pToTagSession
   */
  private TraceSession mergeSessions(List<TraceSession> pTracesSessions) {

    if (pTracesSessions.size() < 1) {
      throw new IllegalArgumentException("Can't merge empty session list");
    }

    TraceSession lResult;

    // sort session from oldest to newest
    Collections.sort(pTracesSessions);

    lResult = pTracesSessions.get(0);
    // add all messages from the newer sessions to the oldest session
    for (TraceSession lTracesSession : pTracesSessions.subList(1, pTracesSessions.size())) {

      lResult.mergeSession(lTracesSession);

      // remove newest session from the session list.
      mTraceSessions.remove(lTracesSession);
    }

    // update indexes
    for (String lCallId : lResult.getCallIds()) {
      mTraceSessionIndex.put(lCallId, lResult);
    }
    for (String lB2BTagToken : lResult.getB2BTagTokens()) {
      mTraceSessionIndex.put(lB2BTagToken, lResult);
    }
    for (String lFromTag : lResult.getFromTags()) {
      mTraceSessionIndex.put(lFromTag, lResult);
    }

    return lResult;

  }

}
