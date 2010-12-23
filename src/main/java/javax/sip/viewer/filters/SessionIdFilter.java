package javax.sip.viewer.filters;

import java.util.ArrayList;
import java.util.List;

import javax.sip.viewer.model.TracesSession;


public class SessionIdFilter extends AbstractFilter {
  private final String mSessionId;

  public SessionIdFilter(List<TracesSession> pTraceSessions, String pSessionId) {
    super(pTraceSessions);
    mSessionId = pSessionId;
  }
  
  public List<TracesSession> process() {
    List<TracesSession> lResult = new ArrayList<TracesSession>();
    for (TracesSession lTracesSession : mTraceSessions) {
      if (lTracesSession.getB2BTagTokens().contains(mSessionId)
          || lTracesSession.getCallIds().contains(mSessionId))
      {
        lResult.add(lTracesSession);
      }
    }
    return lResult;
  }
}
