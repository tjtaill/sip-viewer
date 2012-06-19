package javax.sip.viewer;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.sip.viewer.filters.B2BSipTagTokenFilter;
import javax.sip.viewer.filters.BeginEndTimeFilter;
import javax.sip.viewer.filters.CallIdFilter;
import javax.sip.viewer.filters.CallerNameFilter;
import javax.sip.viewer.filters.CallerPhoneNumberFilter;
import javax.sip.viewer.filters.DestinationPhoneNumberFilter;
import javax.sip.viewer.filters.ErrorFilter;
import javax.sip.viewer.filters.SessionIdFilter;
import javax.sip.viewer.model.TraceSession;
import javax.sip.viewer.parser.SipLogParser;
import javax.sip.viewer.parser.TextLogParser;
import javax.sip.viewer.utils.ListOfFiles;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class SipTextViewer {
  private static final String CLASS_NAME = SipTextViewer.class.getName();
  private static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
  private static final Logger sLogger = Logger.getLogger(PACKAGE_NAME);

  @Parameter
  protected List<String> mFileNames;
  @Parameter(names = { "-s", "--index" }, description = "find by indices")
  protected String mSessionId;
  @Parameter(names = { "-ci", "-callId" }, description = "find by call ID")
  protected String mCallId;
  @Parameter(names = { "-b2b", "-b2bSipTagToken" }, description = "find by B2B sip tag token")
  protected String mB2BSipTagToken;
  @Parameter(names = { "-cn", "-callerName" }, description = "find by caller name")
  protected String mCallerName;
  @Parameter(names = { "-cpn", "-callerPhoneNumber" }, description = "find by caller phone number")
  protected String mCallerPhoneNumber;
  @Parameter(names = { "-dpn", "-destPhoneNumber" }, description = "find by destination phone number")
  protected String mDestPhoneNumber;
  @Parameter(names = { "-eo", "--errorOnly" }, description = "display only the sessions with error in the response code (default false")
  protected boolean mErrorOnly = false;
  @Parameter(names = { "-v", "--verbose" }, description = "Verbose diagram mode (default false)")
  protected boolean mVerbose = false;
  @Parameter(names = { "-hsl", "--hideSipLog" }, description = "Outputs the call stack after the sequence diagram (default true)")
  protected boolean mHideSipLog = false;
  @Parameter(names = { "-h", "--help" }, description = "Displays this help context")
  protected boolean mShowHelp;
  @Parameter(names = { "-rin", "--resolve-ip-name" }, description = "Make a reverse dns query to resolve a pretty name associated with the ip. If reverse dns misses the query, it might delay parsing (default: false)")
  protected boolean mResolveIpNames = false;
  @Parameter(names = { "-p", "--parser" }, description = "Class to be used to parse the given log files (default is javax.sip.viewer.parser.TextLogParser)")
  protected String mParserClassName;
  @Parameter(names = { "-pb2bt", "--parser-b2b-token-regex" }, description = "if using the default textParser which can correlate many dialogs in a same session based on a token found in from and to tags, this key overrides the regex to match that token (default : s(\\d*)-.*)")
  protected String mParserB2BTokenRegex;
  @Parameter(names = { "-t", "--time" }, description = "Filters the logs by showing only the calls between(inclusive) the beginning time and end time (ex: -t 2012/06/13#09:44:27.264|2012/06/13#09:46:27.264)")
  protected String mTime;

  public void display(OutputStream pOut) throws Exception {
    SipLogParser lLogParser = setupParser();
    Writer lWriter = new BufferedWriter(new OutputStreamWriter(pOut));

    String[] lFiles = mFileNames.toArray(new String[mFileNames.size()]);

    SequenceInputStream sInputStream = new SequenceInputStream(new ListOfFiles(lFiles));

    List<TraceSession> lAllSessions = lLogParser.parseLogs(sInputStream);
    Collections.sort(lAllSessions);
    List<TraceSession> lFilteredTSList = applyFilters(lAllSessions);

    lWriter.write(String.format("%d sessions displayed \n\n", lFilteredTSList.size()));
    for (TraceSession lTS : lFilteredTSList) {
      SipTextFormatter lSipTextFormatter = new SipTextFormatter(mVerbose, mResolveIpNames);
      lWriter.write(lSipTextFormatter.format(lTS));
      if (!mHideSipLog) {
        lWriter.write(lSipTextFormatter.generateCallStack(lTS));
      }
      lWriter.flush();
    }
    lWriter.close();
  }

  public boolean isFileProvided() {
    return mFileNames != null;
  }

  protected SipLogParser setupParser() throws Exception {
    if (mParserClassName != null) {
      Class<? extends SipLogParser> lClass = (Class<? extends SipLogParser>) Class.forName(mParserClassName);
      return lClass.newInstance();
    } else {
      if (mParserB2BTokenRegex != null) {
        TextLogParser.setTagPattern(Pattern.compile(mParserB2BTokenRegex));
      }
      return new TextLogParser();
    }

  }

  private List<TraceSession> applyFilters(List<TraceSession> pTraceSessions) {
    List<TraceSession> lResult = pTraceSessions;
    if (mSessionId != null) {
      lResult = new SessionIdFilter(lResult, mSessionId).process();
    }
    if (mCallId != null) {
      lResult = new CallIdFilter(lResult, mCallId).process();
    }
    if (mB2BSipTagToken != null) {
      lResult = new B2BSipTagTokenFilter(lResult, mB2BSipTagToken).process();
    }
    if (mCallerPhoneNumber != null) {
      lResult = new CallerPhoneNumberFilter(lResult, mCallerPhoneNumber).process();
    }
    if (mCallerName != null) {
      lResult = new CallerNameFilter(lResult, mCallerName).process();
    }
    if (mDestPhoneNumber != null) {
      lResult = new DestinationPhoneNumberFilter(lResult, mDestPhoneNumber).process();
    }
    if (mTime != null) {
      lResult = new BeginEndTimeFilter(lResult,
                                       mTime.substring(0, mTime.indexOf("\\")),
                                       mTime.substring(mTime.indexOf("|") + 1)).process();
    }
    if (mErrorOnly) {
      lResult = new ErrorFilter(lResult).process();
    }

    // TODO here we should be able to apply many filters
    return lResult;
  }

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

  public void setFileNames(List<String> pFiles) {
    mFileNames = pFiles;
  }

  public void setParserClassName(String pParserClassName) {
    mParserClassName = pParserClassName;
  }

  public void setB2BSipTagToken(String pB2BSipTagToken) {
    this.mB2BSipTagToken = pB2BSipTagToken;
  }

  public void setCallerName(String mCallerName) {
    this.mCallerName = mCallerName;
  }

  public void setCallerPhoneNumber(String mCallerPhoneNumber) {
    this.mCallerPhoneNumber = mCallerPhoneNumber;
  }

  public void setCallId(String pCallId) {
    this.mCallId = pCallId;
  }

  public void setDestPhoneNumber(String pDestPhoneNumber) {
    this.mDestPhoneNumber = pDestPhoneNumber;
  }

  public void setErrorOnly(boolean pErrorOnly) {
    this.mErrorOnly = pErrorOnly;
  }

  public void setHideSipLog(boolean pHideSipLog) {
    this.mHideSipLog = pHideSipLog;
  }

  public void setParserB2BTokenRegex(String pParserB2BTokenRegex) {
    this.mParserB2BTokenRegex = pParserB2BTokenRegex;
  }

  public void setResolveIpNames(boolean pResolveIpNames) {
    this.mResolveIpNames = pResolveIpNames;
  }

  public void setSessionId(String mSessionId) {
    this.mSessionId = mSessionId;
  }

  public void setTime(String pTime) {
    this.mTime = pTime;
  }

  public void setVerbose(boolean pVerbose) {
    this.mVerbose = pVerbose;
  }

  /**
   * Creates an ASCII sequence diagram from a call session log file. The arguments that can be
   * passed are parsed using JCommander and annotations.
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
        lTF.display(System.out);
      } else {
        System.err.println("You must specify a log file.");
        lJCommander.usage();
      }
    }
  }
}
