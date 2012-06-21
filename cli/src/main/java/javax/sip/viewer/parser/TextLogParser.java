package javax.sip.viewer.parser;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.TraceSession;
import javax.sip.viewer.model.TraceSessionIndexer;

public class TextLogParser implements SipLogParser {
  private static Pattern sDetailsPattern = Pattern.compile("\\[(.*)\\] (IN|OUT) (.*) --> (.*)");
  private static Pattern sTagTokenPattern = Pattern.compile("s(\\d*)-.*");
  private static Pattern sTagPattern = Pattern.compile("([^;]*).*");

  private TraceSessionIndexer mTraceSessionIndexer = new TraceSessionIndexer();

  private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

  /**
   * @see javax.sip.viewer.parser.SipLogParser#parseLogs(java.io.InputStream)
   */
  public List<TraceSession> parseLogs(InputStream pInputStream) {

    Scanner lMessageScanner = new Scanner(pInputStream);

    lMessageScanner.useDelimiter(Pattern.compile("-----------------------"));
    while (lMessageScanner.hasNext()) {

      String lData = lMessageScanner.next().trim();

      if (!lData.isEmpty()) {
        String[] lSplitData = TextLogMessageParser.splitIn2Parts(lData);
        SipMessage lSipMessage = parseMessageDetails(lSplitData[0]);
        lSipMessage.setMessageAsText(lSplitData[1]);
        parseMessage(lSipMessage);
      }
    }

    return mTraceSessionIndexer.getTraceSessions();
  }

  private SipMessage parseMessageDetails(String pDetails) {
    SipMessage lResult = new SipMessage();

    if (pDetails.startsWith("[")) {
      StringBuilder lDateStr = new StringBuilder();
      StringBuilder lOrigin = new StringBuilder();
      StringBuilder lDestination = new StringBuilder();

      TextLogMessageParser.parseDetails(pDetails, lDateStr, lOrigin, lDestination);

      try {
        Date lDate = mDateFormat.parse(lDateStr.toString());

        lResult.setTime(lDate.getTime());
        lResult.setSource(lOrigin.toString());
        lResult.setDestination(lDestination.toString());
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
    StringBuilder lCallId = new StringBuilder();
    StringBuilder lFrom = new StringBuilder();
    StringBuilder lTo = new StringBuilder();
    StringBuilder lEvent = new StringBuilder();
    TextLogMessageParser.parse(pSipMessage.getMessageAsText(), lCallId, lFrom, lTo, lEvent);

    // check for B2B dialog correlation tokens
    String lFromTagToken = searchTagToken(lFrom.toString());
    String lToTagToken = searchTagToken(lTo.toString());

    String lFromTag = searchTag(lFrom.toString());

    // check for subscribe event id correlation (based on prior callid mapping)
    String lEventId = searchEventId(lEvent.toString());
    // String lEventId = "cid_0-22516";

    // look for existing session (or create one)
    mTraceSessionIndexer.indexSipMessage(pSipMessage,
                                         lToTagToken,
                                         lFromTagToken,
                                         lFromTag,
                                         lCallId.toString(),
                                         lEventId);
  }

  /**
   * Check within the value of a header the presence of a tag header param matching the configured
   * tag pattern
   * 
   * @param pHeaderValue
   * @return
   */
  private String searchTagToken(String pHeaderValue) {
    String lResult = null;

    String lTag = pHeaderValue.substring(pHeaderValue.indexOf("tag=") + 4);

    if (lTag != null) {
      Matcher lTagMatcher = sTagTokenPattern.matcher(lTag);
      if (lTagMatcher.matches()) {
        lResult = lTagMatcher.group(1);
      }
    }
    return lResult;
  }

  private String searchTag(String pHeaderValue) {
    String lResult = null;

    String lTag = pHeaderValue.substring(pHeaderValue.indexOf("tag=") + 4);

    if (lTag != null) {
      Matcher lTagMatcher = sTagPattern.matcher(lTag);
      if (lTagMatcher.matches()) {
        lResult = lTagMatcher.group(1);
      }
    }
    return lResult;
  }

  private String searchEventId(String pHeaderValue) {
    String lResult = null;
    if (pHeaderValue.contains("id=")) {
      lResult = pHeaderValue.substring(pHeaderValue.indexOf("id=") + 3);
      lResult.replaceAll("'", "");
    }
    return lResult;
  }

  /**
   * Optionally, the pattern to find a token in the tag parameter of the from and to header to
   * aggregate dialogs in a logical session can be configured.
   * <p>
   * Default is <code>s(\\d*)-.*</code>
   * 
   * @param pTagPattern
   */
  public static void setTagPattern(Pattern pTagPattern) {
    sTagTokenPattern = pTagPattern;
  }

}
