package javax.sip.viewer.filters;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.TraceSession;

/**
 * 
 * @copyright ©2012, Bell Canada
 * @author philippe.agaliotis (CTS)
 */
public class BeginEndTimeFilter extends AbstractFilter {
  private long mBeginTime;
  private long mEndTime;

  /**
   * 
   * Creates a <code>DestinationPhoneNumberFilter</code>.
   * 
   * @param pTraceSessions, List of TraceSessions to be filtered
   * @param pDestPhoneNumber, the filter criterion
   */
  public BeginEndTimeFilter(List<TraceSession> pTraceSessions, String pBeginTime, String pEndTime) {
    super(pTraceSessions);
    this.mBeginTime = convertDateStringToLong(pBeginTime);
    this.mEndTime = convertDateStringToLong(pEndTime);
  }

  private long convertDateStringToLong(String pDateToConvert) {
    long lResult = 0;
    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    Date date;

    try {
      pDateToConvert = pDateToConvert.replace("#", " ");
      date = formatter.parse(pDateToConvert);
      lResult = date.getTime();
    } catch (ParseException e) {
      System.out.println(e);
    }
    return lResult;
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
        try {
          if (lSipMessage.getTime() >= this.mBeginTime && lSipMessage.getTime() <= this.mEndTime
              && !lResult.contains(lTracesSession))
          {
            lResult.add(lTracesSession);
          }
        } catch (StringIndexOutOfBoundsException e) {
          System.out.println(e);
        }
      }
    }

    return lResult;
  }
}
