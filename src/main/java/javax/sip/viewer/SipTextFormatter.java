package javax.sip.viewer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.viewer.model.Actor;
import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.TraceSession;
import javax.sip.viewer.utils.AddressHeaderParser;

public class SipTextFormatter {
  private static final String CLASS_NAME = SipTextFormatter.class.getName();
  private static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
  private static final Logger sLogger = Logger.getLogger(PACKAGE_NAME);

  private static final int TIME_STR_LENGTH = 14;
  private static final int DELAY_STR_LENGTH = 11;
  private static final String COLUMN_CHAR = "|";
  private static final int DEFAULT_LENGTH = 20;
  private static final int ARROW_PADDING_LEN = 11;
  private static final String SESSION_LINE = "************************************************************";
  private static final String LINE = "--------------------------------------------------------------------";
  private static final String ARROW_LINE = "---";  // this will be prefixed with the call id flag. ex.:(a)---
  private static final String ARROW_LEFT = "<----";
  private static final String ARROW_RIGHT = "---->";
  private static final String PAD_CHAR = "-";
  private static final String TIME_COLUMN = "Time";
  private static final String DELAY_COLUMN = "Delay (ms)";
  private static final String SIP_MSG = "SIP/2.0";
  private static String[] sCallIdFlags = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
                                          "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
                                          "w", "x", "y", "z" };
  private static Pattern sCallIdPattern = Pattern.compile(".*^Call-ID:[ ]?(.*?)$.*",
                                                          Pattern.DOTALL | Pattern.MULTILINE);
  private static final Pattern sContactPattern = Pattern.compile(".*^Contact:\\s*(.*?)\\s*$.*",
                                                                 Pattern.DOTALL | Pattern.MULTILINE);

  private static final SimpleDateFormat sDateFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss SSS");
  private static final SimpleDateFormat sSmallDateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
  private static Map<String, String> sHostNameCache = new HashMap<String, String>();
  private static Map<Integer, String> sEmptyArrowCache = new HashMap<Integer, String>();
  private Map<String, String> mCallIdFlag;
  private Map<String, Actor> mActors;
  private List<String> mActorsName;
  private int mMessagesLength[][];

  private static final int MAX_NUMBER_OF_ACTORS = 100;

  private boolean mVerbose = false;
  private final boolean mResolveIpNames;

  public SipTextFormatter(boolean pVerbose, boolean pResolveIpNames) {
    mVerbose = pVerbose;
    mResolveIpNames = pResolveIpNames;
  }

  /**
   * Creates an ASCII diagram from the actual call session.
   * 
   * @param pTracesSession Traces of the call session
   * @return String that contains the sequence diagram
   */
  public String format(TraceSession pTracesSession) {
    StringBuilder lOutput = new StringBuilder();
    mActors = new HashMap<String, Actor>();
    mActorsName = new ArrayList<String>();
    mCallIdFlag = new HashMap<String, String>();
    mMessagesLength = new int[MAX_NUMBER_OF_ACTORS][MAX_NUMBER_OF_ACTORS];

    // First pass in SipMessages
    prepareVariables(pTracesSession.getSipMessageList());

    String lHeaders = buildHeaders(pTracesSession.getSipMessageList().size());
    // Second pass
    String lFlow = parseSipMessages(pTracesSession.getSipMessageList());
    // lOutput.append(String.format("***** #%s ", pTracesSession.getSessionId()));
    lOutput.append("***** ");
    lOutput.append(sDateFormatter.format(new Date(pTracesSession.getTime())));
    lOutput.append(String.format(SESSION_LINE + "\n"));
    lOutput.append("B2B Tokens: ");
    lOutput.append(Arrays.toString(pTracesSession.getB2BTagTokens().toArray()) + "\n");
    lOutput.append("CallIds: ");
    lOutput.append(Arrays.toString(pTracesSession.getCallIds().toArray()) + "\n");
    lOutput.append(lHeaders);
    lOutput.append(lFlow);

    
    return lOutput.toString();
  }

  /**
   * @param pSipMessage
   * @return
   */
  private String getContactHost(SipMessage pSipMessage) {
    String lResult = null;
    Matcher lContactMatcher = sContactPattern.matcher(pSipMessage.getMessageAsText());
    if (lContactMatcher.matches()) {
      AddressHeaderParser lParser = new AddressHeaderParser(lContactMatcher.group(1));
      lResult = lParser.getUriHost();
    }
    return lResult;
  }

  /**
   * Prints a line that contains the index of each SIP message, the moment it occured (Time (ms)) and an arrow that goes from one actor to another using the correct formatting. Each arrow contains the
   * message that was sent between the actors.
   * 
   * @param pSipMessages List of SIP messages
   * @return String that contains one line per SIP message.
   */
  private String parseSipMessages(Set<SipMessage> pSipMessages) {
    int lToID = 0;
    int lFromID = 0;
    int i = 0;
    StringBuilder lResult = new StringBuilder();
    int lArrowLength = 0;
    int lID = 0;
    int lNoMsg = 1;

    String lNbMsgStr = pSipMessages.size() + "";
    int lNbMsg = lNbMsgStr.length();

    // Builds a line for each SIP message
    int k = 0;
    for (SipMessage lSipMessage : pSipMessages) {
      k++;
      // Message index and time
      lResult.append(String.format("%-" + lNbMsg + "d", lNoMsg++));
      lResult.append(String.format("%-" + TIME_STR_LENGTH + "s",
                                   "  "
                                     + sSmallDateFormatter.format(new Date(lSipMessage.getTime()))));

      lResult.append(String.format("%-" + (DELAY_STR_LENGTH + mActorsName.get(0).length() / 2)
                                   + "s", "  " + lSipMessage.getDelay()));

      lResult.append(COLUMN_CHAR);

      lFromID = mActors.get(lSipMessage.getSource()).getIndex();
      lToID = mActors.get(lSipMessage.getDestination()).getIndex();
      i = 0;
      lArrowLength = 0;

      // We start from the lowest index
      if (lFromID > lToID) {
        lID = lToID;
      } else {
        lID = lFromID;
      }

      // Concatening empty arrow until we reached the ID
      for (i = 0; i < lID; i++) {
        lArrowLength = mMessagesLength[i][i + 1];
        if (lArrowLength == 0) {
          lArrowLength = DEFAULT_LENGTH;
        }
        lArrowLength += ARROW_PADDING_LEN;

        lResult.append(getEmptyArrow(lArrowLength));
        lResult.append(COLUMN_CHAR);
      }

      Matcher lCallIdMatcher = sCallIdPattern.matcher(lSipMessage.getMessageAsText());
      String lCallIdFlag = null;
      if (lCallIdMatcher.matches()) {
        String lCallId = lCallIdMatcher.group(1);
        if (!mCallIdFlag.containsKey(lCallId)) {
          int lIndex = mCallIdFlag.size() % sCallIdFlags.length;
          mCallIdFlag.put(lCallId, sCallIdFlags[lIndex]);
        }
        lCallIdFlag = mCallIdFlag.get(lCallId);
      }

      // Building the arrow
      lResult.append(buildArrow(parseSipFirstLine(lSipMessage.getFirstLine()), lFromID, lToID, lCallIdFlag));
      lResult.append(COLUMN_CHAR);

      // We continue with the lowest index
      if (lFromID > lToID) {
        lID = lFromID;
      } else {
        lID = lToID;
      }

      // Filling the rest of the line with empty spaces and vertical lines.
      for (i = lID; i < mActors.size() - 1; i++) {
        lArrowLength = mMessagesLength[i][i + 1];
        if (lArrowLength == 0) {
          lArrowLength = DEFAULT_LENGTH;
        }
        lArrowLength += ARROW_PADDING_LEN;
        lResult.append(getEmptyArrow(lArrowLength));
        lResult.append(COLUMN_CHAR);
      }

      lResult.append("\n");
    }
    return lResult.toString();
  }

  /**
   * Prints the session traces.
   * 
   * @param pTracesSession Session traces
   * @return The session traces concatenated in a string
   */
  public String generateCallStack(TraceSession pTracesSession) {
    StringBuilder lResult = new StringBuilder("\n");
    int i = 1;
    for (SipMessage lMessage : pTracesSession.getSipMessageList()) {
      lResult.append(String.format("----- #%d ", i));

      lResult.append(sDateFormatter.format(new Date(lMessage.getTime())));
      lResult.append(LINE);
      lResult.append("\n");
      lResult.append(lMessage.getMessageAsText());
      lResult.append("\n\n");
      i++;
    }
    return lResult.toString();
  }

  private String getNameFromIp(String pIp) {
    String lHost = pIp;
    String lPort = "5060";
    if (pIp.contains(":")) {
      lHost = pIp.split(":")[0];
      lPort = pIp.split(":")[1];
    }
    String lHostName = lHost;
    if (!sHostNameCache.containsKey(lHost)) {
      if (mResolveIpNames) {
        try {
          InetAddress inetAddress = InetAddress.getByName(lHost);
          lHostName = inetAddress.getHostName();
        } catch (UnknownHostException e) {
          lHostName = lHost;
        }
      }
      sHostNameCache.put(lHost, lHostName);
    }

    lHostName = sHostNameCache.get(lHost);
    return lHostName + ":" + lPort;
  }

  /**
   * Executes a first pass to retrieve the maximum message length for each actors pair. Keeps also in memory the actors details.
   * 
   * @param pSipMessages List of SIP messages
   */
  private void prepareVariables(Set<SipMessage> pSipMessages) {
    int lActorCount = 0;
    int lIndexTo = 0;
    int lIndexFrom = 0;
    String lFrom = "";
    String lTo = "";

    // This loop initializes the matrix that contains the maximum message length for each
    // pair of actors.
    for (SipMessage lSipMessage : pSipMessages) {
      // FROM field
      lFrom = parseActorName(lSipMessage.getSource());
      if (!mActors.containsKey(lFrom)) {
        // Add possibly the real uac hidden behind a proxy
        String lContactHost = getContactHost(lSipMessage);
        if (lContactHost != null && !lContactHost.equals(lFrom)) {
          if (!mActors.containsKey(lContactHost)) {
            mActors.put(lContactHost, new Actor(lActorCount, 0, lContactHost));
            mActorsName.add(getNameFromIp(lContactHost));
            lActorCount++;
          }
        }

        mActors.put(lFrom, new Actor(lActorCount, 0, lFrom));
        mActorsName.add(getNameFromIp(lFrom));
        lIndexFrom = lActorCount;
        lActorCount++;
      } else {
        lIndexFrom = mActors.get(lFrom).getIndex();
      }

      // TO field
      lTo = parseActorName(lSipMessage.getDestination());
      if (!mActors.containsKey(lTo)) {
        mActors.put(lTo, new Actor(lActorCount, 0, lTo));
        mActorsName.add(getNameFromIp(lTo));
        lIndexTo = lActorCount;
        lActorCount++;
      } else {
        lIndexTo = mActors.get(lTo).getIndex();
      }

      // Keeping maximum message length for this column
      String lMessage = parseSipFirstLine(lSipMessage.getFirstLine());
      if (lMessage.length() > mMessagesLength[lIndexFrom][lIndexTo]) {
        if (lMessage.length() > DEFAULT_LENGTH) {
          mMessagesLength[lIndexFrom][lIndexTo] = lMessage.length();
          mMessagesLength[lIndexTo][lIndexFrom] = lMessage.length();
        } else {
          mMessagesLength[lIndexFrom][lIndexTo] = DEFAULT_LENGTH;
          mMessagesLength[lIndexTo][lIndexFrom] = DEFAULT_LENGTH;
        }
      }
    }
  }

  /**
   * Creates the headers printed at the top of the diagram using the actor's name. The gap between each actor is calculated using the maximum messages length.
   * 
   * @param pNbSipMessages Number of SIP messages
   * @return Headers of the diagram
   */
  private String buildHeaders(int pNbSipMessages) {
    StringBuilder lResult = new StringBuilder();
    String lNbMsg = "" + pNbSipMessages;
    int lTotalLength = 0;
    String lCurrKey = "";
    String lNextKey = "";
    int lStrLen = 0;
    int lMsgLen = 0;

    // First columns: Line number and time
    lResult.append(String.format("%-" + (lNbMsg.length() + 2) + "s", "# "));
    lResult.append(String.format("%-" + TIME_STR_LENGTH + "s", TIME_COLUMN));
    lResult.append(String.format("%-" + DELAY_STR_LENGTH + "s", DELAY_COLUMN));

    // Concatening the actor's name (usually an IP address)
    for (int i = 0; i < mActorsName.size() - 1; i++) {
      InetAddress inetAddress;
      lCurrKey = mActorsName.get(i);
      lNextKey = mActorsName.get(i + 1);
      lMsgLen = mMessagesLength[i][i + 1];

      // if no link exists between the two actors, a default length is used
      if (lMsgLen == 0)
        lMsgLen = DEFAULT_LENGTH;

      lStrLen = ARROW_PADDING_LEN + lMsgLen + lCurrKey.length() / 2 - lNextKey.length() / 2 + 1;
      lResult.append(String.format("%-" + lStrLen + "s", lCurrKey));
      lCurrKey = lNextKey;
    }

    // Final actor's name without any padding
    lResult.append(lCurrKey);
    // SYSTEM.OUT.PRINTLN(LCURRKEY);
    lTotalLength = lResult.length();
    lResult.append("\n");

    // Adding a separation line
    for (int j = 0; j < lTotalLength; j++) {
      lResult.append(PAD_CHAR);
    }
    lResult.append("\n");

    return lResult.toString();
  }

  /**
   * Removes redundant information from the first SIP message line.
   * 
   * @param pSipFirstLine First line of the SIP message
   * @return Line of the SIP message
   */
  private String parseSipFirstLine(String pSipFirstLine) {
    int lPos = pSipFirstLine.indexOf(';');
    // Trimming SipMessage if the line contains a ';'
    String lLine = "";
    if (lPos == -1) {
      lLine = pSipFirstLine;
    } else {
      lLine = pSipFirstLine.substring(0, lPos);
    }

    // Removing duplicated information
    String lParts[] = lLine.split(" ");
    StringBuilder lResult = new StringBuilder();

    // When using compact mode, the SIP command is stripped to avoid repetitive information like
    // FROM/TO fields that are already contained in the header.
    if (!mVerbose && lParts.length > 0) {
      if (lParts[0].equals(SIP_MSG)) {
        for (int i = 1; i < lParts.length; i++) {
          lResult.append(lParts[i]).append(" ");
        }
      } else {
        lResult.append(lParts[0]); // Command
        lParts[1] = lParts[1].replace("sip:", "");
        int lAtPos = lParts[1].indexOf("@");

        // Keeping only the sip identificator
        if (lAtPos != -1)
          lResult.append(" ").append(lParts[1].substring(0, lAtPos));
      }
    } else {
      lResult.append(lLine);
    }

    return lResult.toString().trim();
  }

  /**
   * Strips the unwanted charaters from the actor's name.
   * 
   * @param pName Name of the actor to strip
   * @return The actor's name striped
   */
  private String parseActorName(String pName) {
    return pName.replace("<", "").replace(">", "");
  }

  /**
   * Returns an empty string of a given length.
   * 
   * @param pLength Length of the empty string
   * @return An empty string of a given length
   */
  private String getEmptyArrow(int pLength) {
    if (sEmptyArrowCache.containsKey(pLength)) {
      return sEmptyArrowCache.get(pLength);
    } else {
      String lEmptyArrow = String.format("%-" + pLength + "s", "");
      sEmptyArrowCache.put(pLength, lEmptyArrow);
      return lEmptyArrow;
    }
  }

  /**
   * Builds an ASCII arrow of a dynamic length using the maximum possible length for the column(s) that the arrow needs to get across.
   * 
   * @param pMessage Message to include in the middle of the arrow
   * @param pFromID The arrow starting point
   * @param pToID The arrow ending point
   * @return An horizontal ASCII arrow
   */
  private String buildArrow(String pMessage, int pFromID, int pToID, String pCallIdFlag) {
    int lArrowLength = 0;
    int lCompensationPadding = 0; // Used when an arrow passes across multiple columns
    int i = 0;

    // Getting the arrow length
    if (pFromID < pToID) {
      lArrowLength = getArrowLength(pFromID, pToID);
    } else {
      lArrowLength = getArrowLength(pToID, pFromID);
    }

    // Checking of additional padding will be required
    if (Math.abs(pFromID - pToID) > 1) {
      lCompensationPadding = Math.abs(pFromID - pToID) - 1;
    }
    lCompensationPadding += lArrowLength - pMessage.length();
    String lResult = pMessage;
    for (i = 0; i < lCompensationPadding; i++) {
      if (i % 2 == 0)
        lResult += PAD_CHAR;
      else
        lResult = PAD_CHAR + lResult;
    }

    // Adding final padding
    String lLeftPadding = "(" + pCallIdFlag + ")" + ARROW_LINE;
    String lRightPadding = ARROW_RIGHT;
    if (pFromID > pToID) {
      lLeftPadding = ARROW_LEFT;
      lRightPadding = ARROW_LINE + "(" + pCallIdFlag + ")";
    }
    lResult = lLeftPadding + lResult + lRightPadding;

    return lResult;
  }

  /**
   * Using the matrix of maximum messages length, returns the required length to go from FromID to ToID.
   * 
   * @param pFromID Index of the request initiator
   * @param pToID Index of the request receiver
   * @return Length between two points
   */
  private int getArrowLength(int pFromID, int pToID) {
    int lLength = 0;

    if (pToID - pFromID == 1) {
      if (mMessagesLength[pFromID][pToID] != 0)
        lLength += mMessagesLength[pFromID][pToID];
      else
        lLength += DEFAULT_LENGTH + ARROW_PADDING_LEN;
    } else {
      int count = 0;
      while (pToID > pFromID) {
        count++;
        if (mMessagesLength[pToID][pToID - 1] != 0)
          lLength += mMessagesLength[pToID][pToID - 1];
        else
          lLength += DEFAULT_LENGTH;

        pToID--;
      }
      lLength += ARROW_PADDING_LEN * (count - 1);
    }

    return lLength;
  }

}
