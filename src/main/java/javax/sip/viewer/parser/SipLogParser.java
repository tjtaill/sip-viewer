package javax.sip.viewer.parser;

import java.io.InputStream;
import java.util.List;

import javax.sip.viewer.model.TracesSession;

public interface SipLogParser {
  
  /**
   * Parse the content of the input stream in order to match sip messages in sessions
   * based on implementations rules (ex.: callid, token in to/from tag or extra header)
   * @param pInputStream
   * @return A List of aggregated session representing logical calls.
   */
  public List<TracesSession> parseLogs(InputStream pInputStream);
  
}