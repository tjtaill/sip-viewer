package javax.sip.viewer.filters;

import java.util.ArrayList;
import java.util.List;

import javax.sip.viewer.model.TraceSession;

/**
 * 
 * @copyright ©2011, Bell Canada
 * @author jonathan.rochette (CTS)
 */
public class B2BSipTagTokenFilter extends AbstractFilter {
  private String mB2BSipTagToken;

  /**
   * 
   * Creates a <code>B2BSipTagTokenFilter</code>.
   * 
   * @param pTraceSessions, List of TraceSessions to be filtered
   * @param pB2BSipTagToken, the filter criterion
   */
  public B2BSipTagTokenFilter(List<TraceSession> pTraceSessions, String pB2BSipTagToken) {
    super(pTraceSessions);
    this.mB2BSipTagToken = pB2BSipTagToken;
  }

  @Override
  /**
   * 
   * @see javax.sip.viewer.filters.AbstractFilter#process()
   * 
   * This method proceed to the filtering of the list of TraceSessions
   */
  public List<TraceSession> process() {
    List<TraceSession> lResult = new ArrayList<TraceSession>();
    for (TraceSession lTracesSession : mTraceSessions) {
      if (lTracesSession.getB2BTagTokens().contains(mB2BSipTagToken)) {
        lResult.add(lTracesSession);
      }
    }
    return lResult;
  }
}
