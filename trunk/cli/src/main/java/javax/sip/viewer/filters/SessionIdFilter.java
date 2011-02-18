package javax.sip.viewer.filters;

import java.util.ArrayList;
import java.util.List;

import javax.sip.viewer.model.TraceSession;


public class SessionIdFilter extends AbstractFilter {
  private final String mSessionId;

  public SessionIdFilter(List<TraceSession> pTraceSessions, String pSessionId) {
    super(pTraceSessions);
    mSessionId = pSessionId;
  }
  
  public List<TraceSession> process() {
    List<TraceSession> lResult = new ArrayList<TraceSession>();
    for (TraceSession lTracesSession : mTraceSessions) {
      if (lTracesSession.getB2BTagTokens().contains(mSessionId)
          || lTracesSession.getCallIds().contains(mSessionId))
      {
        lResult.add(lTracesSession);
      }
    }
    return lResult;
  }
}
