package javax.sip.viewer.parser;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.TracesSession;
import javax.sip.viewer.utils.AddressHeaderParser;

public class TextLogParser implements SipLogParser {
  private static Pattern sDetailsPattern = Pattern.compile("\\[(.*)\\] (IN|OUT) (.*) --> (.*)");
  private static Pattern sCallIdPattern = Pattern.compile(".*^Call-ID:[ ]?(.*?)$.*",
                                                          Pattern.DOTALL | Pattern.MULTILINE);
  private static Pattern sToPattern = Pattern.compile(".*^To:[ ]?(.*?)$.*", Pattern.DOTALL
                                                                            | Pattern.MULTILINE);
  private static Pattern sFromPattern = Pattern.compile(".*^From:[ ]?(.*?)$.*", Pattern.DOTALL
                                                                                | Pattern.MULTILINE);
  private static Pattern sTagPattern = Pattern.compile("s(\\d*)-.*");

  private List<TracesSession> mTraceSessions = new ArrayList<TracesSession>(100000);
  private Map<String, TracesSession> mTraceSessionIndex = new TreeMap<String, TracesSession>();

  /**
   * @see javax.sip.viewer.parser.SipLogParser#parseLogs(java.io.InputStream)
   */
  public List<TracesSession> parseLogs(InputStream pInputStream) {
    Scanner lMessageScanner = new Scanner(pInputStream);

    lMessageScanner.useDelimiter(Pattern.compile("-----------------------"));
    while (lMessageScanner.hasNext()) {
      String lData = lMessageScanner.next().trim();
      if (!lData.isEmpty()) {
        Scanner lDataScanner = new Scanner(lData);
        lDataScanner.useDelimiter("\\Z");
        SipMessage lSipMessage = parseMessageDetails(lDataScanner.nextLine());
        lDataScanner.nextLine(); // skip empty delimiter line
        lSipMessage.setMessageAsText(lDataScanner.next());
        parseMessage(lSipMessage);
      }
    }

    return mTraceSessions;
  }

  private SipMessage parseMessageDetails(String pDetails) {
    SipMessage lResult = new SipMessage();
    Matcher lDetailsMatcher = sDetailsPattern.matcher(pDetails);
    if (lDetailsMatcher.matches()) {
      try {
        Date lDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").parse(lDetailsMatcher.group(1));
        lResult.setTime(lDate.getTime());
        lResult.setSource(lDetailsMatcher.group(3));
        lResult.setDestination(lDetailsMatcher.group(4));
      } catch (Exception e) {
        throw new RuntimeException(pDetails + " is not having the right format", e);
      }
    } else {
      throw new RuntimeException(pDetails + " is not having the right format");
    }
    return lResult;
  }

  /**
   * Analyse a single sip message to match it with sessions
   * 
   * @param pMessageAsText
   */
  private void parseMessage(SipMessage pSipMessage) {
    Matcher lCallIdMatcher = sCallIdPattern.matcher(pSipMessage.getMessageAsText());
    Matcher lFromMatcher = sFromPattern.matcher(pSipMessage.getMessageAsText());
    Matcher lToMatcher = sToPattern.matcher(pSipMessage.getMessageAsText());
    String lCallId = null;
    String lFrom = null;
    String lTo = null;
    if (lCallIdMatcher.matches()) {
      lCallId = lCallIdMatcher.group(1);
    }
    if (lFromMatcher.matches()) {
      lFrom = lFromMatcher.group(1);
    }
    if (lToMatcher.matches()) {
      lTo = lToMatcher.group(1);
    }

    // check for B2B dialog correlation tokens
    String lFromTagToken = searchTagToken(lFrom);
    String lToTagToken = searchTagToken(lTo);

    // look for existing session (or create one)
    List<TracesSession> lTraceSessions = findTraceSessions(lToTagToken, lFromTagToken, lCallId);

    TracesSession lTraceSession;
    
    if (lTraceSessions.size() > 1)  {
      lTraceSession = mergeSessions(lTraceSessions);  
    } else {
      lTraceSession  = lTraceSessions.get(0);
    }      
    
    // add the sip message in the session
    lTraceSession.attach(pSipMessage);

    // leverage session's indices
    lTraceSession.addCallId(lCallId);
    mTraceSessionIndex.put(lCallId, lTraceSession);
    if (lFromTagToken != null) {
      lTraceSession.addB2BTagTokens(lFromTagToken);
      mTraceSessionIndex.put(lFromTagToken, lTraceSession);
    }
    if (lToTagToken != null) {
      lTraceSession.addB2BTagTokens(lToTagToken);
      mTraceSessionIndex.put(lToTagToken, lTraceSession);
    }
  }

  /**
   * Check within the value of a header the presence of a tag header param matching the configured tag pattern
   * 
   * @param pHeaderValue
   * @return
   */
  private String searchTagToken(String pHeaderValue) {
    String lResult = null;
    AddressHeaderParser lAddressHeaderParser = new AddressHeaderParser(pHeaderValue);
    String lTag = lAddressHeaderParser.getHeaderParams().get("tag");
    if (lTag != null) {
      Matcher lTagMatcher = sTagPattern.matcher(lTag);
      if (lTagMatcher.matches()) {
        lResult = lTagMatcher.group(1);
      }
    }
    return lResult;
  }

  /**
   * According to session indices (callid, to-tag and from-tag),
   * search for an already existing session. If none is found, a new session is created.
   * 
   * @param pToTagToken
   * @param pFromTagToken
   * @param pCallId
   * @return a TraceSessionList that contains at least 1 session.
   */
  private List<TracesSession> findTraceSessions(String pToTagToken, String pFromTagToken, String pCallId) {
    List<TracesSession> lResult = new ArrayList<TracesSession>();
    
    if (pToTagToken != null && mTraceSessionIndex.containsKey(pToTagToken)) {
      if (!lResult.contains(mTraceSessionIndex.get(pToTagToken))) {
        lResult.add(mTraceSessionIndex.get(pToTagToken));
      }
    }
    if (pFromTagToken != null && mTraceSessionIndex.containsKey(pFromTagToken)) {
      if (!lResult.contains(mTraceSessionIndex.get(pFromTagToken))) {
        lResult.add(mTraceSessionIndex.get(pFromTagToken));
      }
    }
    if (pCallId != null && mTraceSessionIndex.containsKey(pCallId)) {
      if (!lResult.contains(mTraceSessionIndex.get(pCallId))) {
        lResult.add(mTraceSessionIndex.get(pCallId));
      }
    }
    
    if (lResult.size() == 0) {
      lResult.add(new TracesSession());
      mTraceSessions.add(lResult.get(0));
    }
    return lResult;
  }

  /**
   * @param pFromTagSession
   * @param pToTagSession
   */
  private TracesSession mergeSessions(List<TracesSession> pTracesSessions) {
    
    if (pTracesSessions.size() < 1) {
      throw new IllegalArgumentException("Can't merge empty session list");
    }
    
    TracesSession lResult;
    
    // sort session from oldest to newest
    Collections.sort(pTracesSessions);
    
    lResult = pTracesSessions.get(0);
    // add all messages from the newer sessions to the oldest session 
    for (TracesSession lTracesSession : pTracesSessions.subList(1, pTracesSessions.size())) {
      
      lResult.mergeSession(lTracesSession);

      // remove newest session from the session list.
      mTraceSessions.remove(lTracesSession);
    }
    
    // update indexes
    for (String lCallId : lResult.getCallIds()) {
      mTraceSessionIndex.put(lCallId, lResult);
    }
    for (String lB2BTagToken : lResult.getB2BTagTokens()) {
      mTraceSessionIndex.put(lB2BTagToken, lResult);
    }
    
    return lResult;

  }

  /**
   * Optionally, the pattern to find a token in the tag parameter of the from and to header to aggregate dialogs in a logical session can be configured. <p>Default is <code>s(\\d*)-.*</code>
   * 
   * @param pTagPattern
   */
  public static void setTagPattern(Pattern pTagPattern) {
    sTagPattern = pTagPattern;
  }

}
