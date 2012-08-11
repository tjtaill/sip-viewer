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
public class DestinationPhoneNumberFilter extends AbstractFilter {
  private static Pattern sToPattern = Pattern.compile(".*^(To:[ ]?(.*?))$.*", Pattern.DOTALL
                                                                              | Pattern.MULTILINE);
  private String mDestPhoneNumber;
  private String mToTag;

  /**
   * 
   * Creates a <code>DestinationPhoneNumberFilter</code>.
   * 
   * @param pTraceSessions, List of TraceSessions to be filtered
   * @param pDestPhoneNumber, the filter criterion
   */
  public DestinationPhoneNumberFilter(List<TraceSession> pTraceSessions, String pDestPhoneNumber) {
    super(pTraceSessions);
    this.mDestPhoneNumber = pDestPhoneNumber;
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
        Matcher lMatcher = sToPattern.matcher(lSipMessageAsText);
        if (lMatcher.matches()) {
          mToTag = lMatcher.group(1);
          AddressHeaderParser lParser = new AddressHeaderParser(mToTag);
          String lDestPhoneNumber = lParser.getUriUser().split(":")[1].trim();
          if (lDestPhoneNumber.contains(mDestPhoneNumber) && !lResult.contains(lTracesSession)) {
            lResult.add(lTracesSession);
          }
        }
      }
    }
    return lResult;
  }
}
