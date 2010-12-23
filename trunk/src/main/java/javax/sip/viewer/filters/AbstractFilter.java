package javax.sip.viewer.filters;

import java.util.List;

import javax.sip.viewer.model.TracesSession;


public abstract class AbstractFilter {
  protected List<TracesSession> mTraceSessions;
  
  public AbstractFilter(List<TracesSession> pTraceSessions) {
    mTraceSessions = pTraceSessions;
  }
  
  public abstract List<TracesSession> process();
  
}
