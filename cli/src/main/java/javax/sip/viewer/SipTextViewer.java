package javax.sip.viewer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.io.Writer;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
      lResult = new SessionIdFilter(pTraceSessions, mSessionId).process();
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
        lTF.display(System.out);
      } else {
        System.err.println("You must specify a log file.");
        lJCommander.usage();
      }
    }
  }
}
