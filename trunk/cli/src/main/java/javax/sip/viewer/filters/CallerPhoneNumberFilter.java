package javax.sip.viewer.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.TraceSession;
import javax.sip.viewer.utils.AddressHeaderParser;

/**
 * 
 * @copyright ©2011, Bell Canada
 * @author jonathan.rochette (CTS)
 */
public class CallerPhoneNumberFilter extends AbstractFilter {
  private static Pattern sFromPattern = Pattern.compile(".*^(From:[ ]?(.*?))$.*",
                                                        Pattern.DOTALL | Pattern.MULTILINE);
  private String mCallerPhoneNumber;
  private String mFromTag;

  /**
   * 
   * Creates a <code>CallerPhoneNumberFilter</code>.
   * 
   * @param pTraceSessions, the List of TraceSessions to be filtered
   * @param pCallerPhoneNumber, the filter criterion
   */
  public CallerPhoneNumberFilter(List<TraceSession> pTraceSessions, String pCallerPhoneNumber) {
    super(pTraceSessions);
    this.mCallerPhoneNumber = pCallerPhoneNumber;
  }

  @Override
  /**
   * 
   * @see javax.sip.viewer.filters.AbstractFilter#process()
   * 
   * This method proceed to the filtering of the TraceSessions
   */
  public List<TraceSession> process() {
    List<TraceSession> lResult = new ArrayList<TraceSession>();
    for (TraceSession lTracesSession : mTraceSessions) {
      for (SipMessage lSipMessage : lTracesSession.getSipMessageList()) {
        String lSipMessageAsText = lSipMessage.getMessageAsText();
        Matcher lMatcher = sFromPattern.matcher(lSipMessageAsText);
        if (lMatcher.matches()) {
          mFromTag = lMatcher.group(1);
          AddressHeaderParser lParser = new AddressHeaderParser(mFromTag);
          if (lParser.getUriUser().split(":")[1].contains(mCallerPhoneNumber)
              && !lResult.contains(lTracesSession))
          {
            lResult.add(lTracesSession);
          }
        }
      }
    }
    return lResult;
  }
}
