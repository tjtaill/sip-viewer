package javax.sip.viewer;

import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.sip.viewer.filters.SessionIdFilter;
import javax.sip.viewer.model.TracesSession;
import javax.sip.viewer.parser.SipLogParser;
import javax.sip.viewer.parser.TextLogParser;
import javax.sip.viewer.utils.ListOfFiles;
import javax.sip.viewer.utils.TraceSessionComparator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class SipTextViewer {
  private static final String CLASS_NAME = SipTextViewer.class.getName();
  private static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
  private static final Logger sLogger = Logger.getLogger(PACKAGE_NAME);

  @Parameter
  private List<String> mFiles;
  @Parameter(names = { "-s", "--index" }, description = "find by indices")
  private String mSessionId;
  @Parameter(names = { "-v", "--verbose" }, description = "Verbose diagram mode (default false)")
  private boolean mVerbose = false;
  @Parameter(names = { "-hsl", "--hideSipLog" }, description = "Outputs the call stack after the sequence diagram (default true)")
  private boolean mHideSipLog = false;
  @Parameter(names = { "-h", "--help" }, description = "Displays this help context")
  private boolean mShowHelp;
  @Parameter(names = { "-p", "--parser" }, description = "Class to be used to parse the given log files (default is javax.sip.viewer.parser.TextLogParser)")
  private String mParserClassName;
  @Parameter(names = { "-pb2bt", "--parser-b2b-token-regex" }, description = "if using the default textParser which can correlate many dialogs in a same session based on a token found in from and to tags, this key overrides the regex to match that token (default : s(\\d*)-.*)")
  private String mParserB2BTokenRegex;

  /**
   * @return the showHelp
   */
  public boolean isShowHelp() {
    return mShowHelp;
  }

  /**
   * @param pShowHelp the showHelp to set
   */
  public void setShowHelp(boolean pShowHelp) {
    mShowHelp = pShowHelp;
  }

  public void display() throws Exception {
    SipLogParser lLogParser = setupParser();
    
    List<TracesSession> lAllSessions = null;

    String[] lFiles = mFiles.toArray(new String[mFiles.size()]);
    SequenceInputStream sInputStream = new SequenceInputStream(new ListOfFiles(lFiles));
    lAllSessions = lLogParser.parseLogs(sInputStream);
    Collections.sort(lAllSessions, new TraceSessionComparator());
    List<TracesSession> lFilteredTSList = applyFilters(lAllSessions);

    System.out.println(String.format("%d sessions displayed \n\n", lFilteredTSList.size()));
    for (TracesSession lTS : lFilteredTSList) {
      SipTextFormatter lSipTextFormatter = new SipTextFormatter(mVerbose);
      System.out.println(lSipTextFormatter.format(lTS));
      if (!mHideSipLog) {
        System.out.println(lSipTextFormatter.generateCallStack(lTS));
      }
    }
  }

  public boolean isFileProvided() {
    return mFiles != null;
  }

  private SipLogParser setupParser() throws Exception {
    if (mParserClassName != null) {
      Class<? extends SipLogParser> lClass = (Class<? extends SipLogParser>) Class.forName(mParserClassName);
      return lClass.newInstance();
    } else {
      if (mParserB2BTokenRegex != null) {
        TextLogParser.setTagPattern(Pattern.compile(mParserB2BTokenRegex));
      }
      return  new TextLogParser();
    }
    
  }
  

  private List<TracesSession> applyFilters(List<TracesSession> pTraceSessions) {
    List<TracesSession> lResult = pTraceSessions;
    if (mSessionId != null) {
      lResult = new SessionIdFilter(pTraceSessions, mSessionId).process();
    }
    // TODO here we should be able to apply many filters
    return lResult;
  }

  /**
   * Creates an ASCII sequence diagram from a call session log file. The arguments that can be passed are parsed using JCommander and annotations.
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    SipTextViewer lTF = new SipTextViewer();

    // Parsing command-line arguments
    JCommander lJCommander = new JCommander(lTF, args);
    lJCommander.setProgramName("SipTextViewer");

    if (lTF.isShowHelp()) {
      // Displays help
      lJCommander.usage();
    } else {
      if (lTF.isFileProvided()) {
        lTF.display();
      } else {
        System.err.println("You must specify a log file.");
        lJCommander.usage();
      }
    }
  }
}
