package javax.sip.viewer.filters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.sip.viewer.filters.AbstractFilter;
import javax.sip.viewer.model.TraceSession;

/**
 * 
 * @copyright ©2011, Bell Canada
 * @author jonathan.rochette (CTS)
 */
public class DateFilter extends AbstractFilter {
  private static Pattern sDatePattern = Pattern.compile(".*^[*****].*([1-31].*[0-9]{2,4} [0-9]{2}:[0-9]{2}:[0-9]{2}).*?$.*",
                                                        Pattern.DOTALL | Pattern.MULTILINE);
  private Date mDateBegin;
  private Date mDateEnd;

  /**
   * 
   * Creates a <code>DateFilter</code>.
   * 
   * @param pTraceSessions, List of TraceSessions to be filtered
   * @param pDateBegin, the Date that begins the interval to filter
   * @param pDateEnd, the Date that ends the interval to filter
   */
  public DateFilter(List<TraceSession> pTraceSessions, Date pDateBegin, Date pDateEnd) {
    super(pTraceSessions);
    this.mDateBegin = pDateBegin;
    this.mDateEnd = pDateEnd;
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
      Date lTraceSessionDate = new Date(lTracesSession.getTime());
      if (mDateBegin == null) {
        if (mDateEnd.after(lTraceSessionDate)) {
          lResult.add(lTracesSession);
        }
        continue;
      }
      if (mDateEnd == null) {
        if (mDateBegin.before(lTraceSessionDate)) {
          lResult.add(lTracesSession);
        }
        continue;
      }
      if (mDateEnd.after(lTraceSessionDate) && mDateBegin.before(lTraceSessionDate)) {
        lResult.add(lTracesSession);
      }
    }
    return lResult;
  }
}
