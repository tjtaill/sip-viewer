package javax.sip.viewer.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.viewer.filters.AbstractFilter;
import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.TraceSession;

/**
 * 
 * @copyright ©2011, Bell Canada
 * @author jonathan.rochette (CTS)
 */
public class ErrorFilter extends AbstractFilter {
  private static Pattern sResponsePattern = Pattern.compile(".*^(SIP/2\\.0 .*?)$.*",
                                                            Pattern.DOTALL | Pattern.MULTILINE);
  private String mResponseTag;

  /**
   * 
   * Creates a <code>CallIdFilter</code>.
   * 
   * @param pTraceSessions, List of TraceSessions to be filtered
   * @param pErrorFiltering, a boolean that apply the filter if true
   */
  public ErrorFilter(List<TraceSession> pTraceSessions) {
    super(pTraceSessions);
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
      for (SipMessage lSipMessage : lTracesSession.getSipMessageList()) {
        String lSipMessageAsText = lSipMessage.getMessageAsText();
        Matcher lMatcher = sResponsePattern.matcher(lSipMessageAsText);
        if (lMatcher.matches()) {
          mResponseTag = lMatcher.group(1);
          Integer lResponseCode = Integer.parseInt(mResponseTag.split(" ")[1]);
          if (lResponseCode >= 400) {
            lResult.add(lTracesSession);
          }
        }
      }
    }
    return lResult;
  }
}
