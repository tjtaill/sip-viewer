package javax.sip.viewer.filters;

import java.util.ArrayList;
import java.util.List;

import javax.sip.viewer.model.TraceSession;

/**
 * 
 * @copyright ©2011, Bell Canada
 * @author jonathan.rochette (CTS)
 */
public class CallIdFilter extends AbstractFilter {
  private String mCallId;

  /**
   * 
   * Creates a <code>CallIdFilter</code>.
   * 
   * @param pTraceSessions, List of TraceSessions to be filtered
   * @param pCallId, the filter criterion
   */
  public CallIdFilter(List<TraceSession> pTraceSessions, String pCallId) {
    super(pTraceSessions);
    this.mCallId = pCallId;
  }

  @Override
  /**
   * 
   * @see javax.sip.viewer.filters.AbstractFilter#process()
   * 
   * This method proceeds to the filtering of the TraceSessions
   */
  public List<TraceSession> process() {
    List<TraceSession> lResult = new ArrayList<TraceSession>();
    for (TraceSession lTracesSession : mTraceSessions) {
      if (lTracesSession.getCallIds().contains(mCallId)) {
        lResult.add(lTracesSession);
      }
    }
    return lResult;
  }
}
