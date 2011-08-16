package javax.sip.viewer.filters;

import java.util.List;

import javax.sip.viewer.model.TraceSession;

public abstract class AbstractFilter {
  protected List<TraceSession> mTraceSessions;

  public AbstractFilter(List<TraceSession> pTraceSessions) {
    mTraceSessions = pTraceSessions;
  }

  // test
  public abstract List<TraceSession> process();

}
