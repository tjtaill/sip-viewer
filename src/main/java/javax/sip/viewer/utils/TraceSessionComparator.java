package javax.sip.viewer.utils;

import java.util.Comparator;

import javax.sip.viewer.model.TracesSession;


public class TraceSessionComparator implements Comparator<TracesSession> {

  public int compare(TracesSession pO1, TracesSession pO2) {
    return (int)(pO1.getTime() - pO2.getTime());
  }
}
