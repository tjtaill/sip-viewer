package javax.sip.viewer;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.sip.viewer.filters.B2BSipTagTokenFilter;
import javax.sip.viewer.filters.CallIdFilter;
import javax.sip.viewer.filters.CallerNameFilter;
import javax.sip.viewer.filters.CallerPhoneNumberFilter;
import javax.sip.viewer.filters.DateFilter;
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
  private List<String> mFileNames;
  @Parameter(names = { "-s", "--index" }, description = "find by indices")
  private String mSessionId;
  @Parameter(names = { "-ci", "-callId" }, description = "find by call ID")
  private String mCallId;
  @Parameter(names = { "-b2b", "-b2bSipTagToken" }, description = "find by B2B sip tag token")
  private String mB2BSipTagToken;
  @Parameter(names = { "-cn", "-callerName" }, description = "find by caller name")
  private String mCallerName;
  @Parameter(names = { "-cpn", "-callerPhoneNumber" }, description = "find by caller phone number")
  private String mCallerPhoneNumber;
  @Parameter(names = { "-dpn", "-destPhoneNumber" }, description = "find by destination phone number")
  private String mDestPhoneNumber;
  @Parameter(names = { "-db", "-dateBegin" }, description = "find by date interval. This date is the beginning of the interval. Ending of the interval is \"-de\" or \"-dateEnd\"")
  private String mDateBegin;
  @Parameter(names = { "-de", "-dateEnd" }, description = "find by date interval. This date is the end of the interval. Beginning of the interval is \"-db\" or \"-dateBegin\"")
  private String mDateEnd;
  @Parameter(names = { "-eo", "--errorOnly" }, description = "display only the sessions with error in the response code (default false")
  private boolean mErrorOnly = false;
  @Parameter(names = { "-v", "--verbose" }, description = "Verbose diagram mode (default false)")
  private boolean mVerbose = false;
  @Parameter(names = { "-hsl", "--hideSipLog" }, description = "Outputs the call stack after the sequence diagram (default true)")
  private boolean mHideSipLog = false;
  @Parameter(names = { "-h", "--help" }, description = "Displays this help context")
  private boolean mShowHelp;
  @Parameter(names = { "-rin", "--resolve-ip-name" }, description = "Make a reverse dns query to resolve a pretty name associated with the ip. If reverse dns misses the query, it might delay parsing (default: false)")
  private boolean mResolveIpNames = false;
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

  private SipLogParser setupParser() throws Exception {
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
    if (mDateBegin != null || mDateEnd != null) {
      SimpleDateFormat lDateFormatter = new SimpleDateFormat("dd/MM/yy");
      Date lDateBegin = null;
      Date lDateEnd = null;
      try {
        if (mDateBegin != null) {
          lDateBegin = lDateFormatter.parse(mDateBegin);
        }
        if (mDateEnd != null) {
          lDateEnd = lDateFormatter.parse(mDateEnd);
        }
        lResult = new DateFilter(lResult, lDateBegin, lDateEnd).process();
      } catch (ParseException e) {
        sLogger.log(Level.SEVERE, "Error in the date format", e);
        System.err.println("The entered date format in incorrect.");
      }
    }
    if (mErrorOnly) {
      lResult = new ErrorFilter(lResult).process();
    }
    // TODO here we should be able to apply many filters
    return lResult;
  }

  public void setFileNames(List<String> pFiles) {
    mFileNames = pFiles;
  }

  public void setParserClassName(String pParserClassName) {
    mParserClassName = pParserClassName;
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
