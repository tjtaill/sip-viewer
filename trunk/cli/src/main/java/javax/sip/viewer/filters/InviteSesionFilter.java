package javax.sip.viewer.filters;

import java.util.ArrayList;
import java.util.List;

import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.TraceSession;

public class InviteSesionFilter extends AbstractFilter {
  public InviteSesionFilter(List<TraceSession> pTraceSessions) {
    super(pTraceSessions);
  }

  public List<TraceSession> process() {
    List<TraceSession> lResult = new ArrayList<TraceSession>();

    for (TraceSession lTracesSession : mTraceSessions) {
      for (SipMessage message : lTracesSession.getSipMessageList()) {
        if (message.getFirstLine().contains("INVITE")) {
          lResult.add(lTracesSession);
          break;
        }
      }
    }
    return lResult;
  }
}
