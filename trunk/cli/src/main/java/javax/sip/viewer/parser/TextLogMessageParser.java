package javax.sip.viewer.parser;

/**
 * @copyright ©2012, Bell Canada
 * @author mathieu.marcotte-gag (CTS)
 */
public class TextLogMessageParser {
  
  // Parse with a little state machine (more complex than regexes but faster)
  static final int STATE_DEFAULT = 0;
  static final int STATE_C = 1;
  static final int STATE_A = 2;
  static final int STATE_L1 = 3;
  static final int STATE_L2 = 4;
  static final int STATE_DASH = 5;
  static final int STATE_I = 6;
  static final int STATE_D = 7;
  static final int STATE_COLON_CALL_ID = 8;
  static final int STATE_CALL_ID = 9;
  static final int STATE_F = 10;
  static final int STATE_R = 11;
  static final int STATE_O_FROM = 12;
  static final int STATE_M = 13;
  static final int STATE_COLON_FROM = 14;
  static final int STATE_FROM = 15;
  static final int STATE_T = 16;
  static final int STATE_O_TO = 17;
  static final int STATE_COLON_TO = 18;
  static final int STATE_TO = 19;
  
  /**
   * 
   * @param pMessage input to parse
   * @param lCallId output goes there
   * @param lFrom output goes there
   * @param lTo output goes there
   */
  public static void parse(String pMessage,
                           StringBuilder lCallId,
                           StringBuilder lFrom,
                           StringBuilder lTo) {
    // Parse with a little state machine (more complex than regexes but faster)
    int state = STATE_DEFAULT;

    for (int n=0; n<pMessage.length(); n++) {
      char c = pMessage.charAt(n);
      
      switch (state) {
      case STATE_DEFAULT:
        if (c == 'C') state = STATE_C;
        else if (c == 'F') state = STATE_F;
        else if (c == 'T') state = STATE_T;
        break;
        
      // -- Call-ID
      case STATE_C:
        if (c == 'a') state = STATE_A;
        else state = STATE_DEFAULT;
        break;
      case STATE_A:
        if (c == 'l') state = STATE_L1;
        else state = STATE_DEFAULT;
        break;
      case STATE_L1:
        if (c == 'l') state = STATE_L2;
        else state = STATE_DEFAULT;
        break;
      case STATE_L2:
        if (c == '-') state = STATE_DASH;
        else state = STATE_DEFAULT;
        break;
      case STATE_DASH:
        if (c == 'I') state = STATE_I;
        else state = STATE_DEFAULT;
        break;
      case STATE_I:
        if (c == 'D') state = STATE_D;
        else state = STATE_DEFAULT;
        break;
      case STATE_D:
        if (c == ':') state = STATE_COLON_CALL_ID;
        else state = STATE_DEFAULT;
        break;
      case STATE_COLON_CALL_ID:
        if (c == ' ') continue;
        else state = STATE_CALL_ID;
        
      // fall-through intended
      case STATE_CALL_ID:
        if (c == '\n' || c == '\r') state = STATE_DEFAULT;
        else lCallId.append(c);
        break;
        
      // -- From
      case STATE_F:
        if (c == 'r') state = STATE_R;
        else state = STATE_DEFAULT;
        break;
      case STATE_R:
        if (c == 'o') state = STATE_O_FROM;
        else state = STATE_DEFAULT;
        break;
      case STATE_O_FROM:
        if (c == 'm') state = STATE_M;
        else state = STATE_DEFAULT;
        break;
      case STATE_M:
        if (c == ':') state = STATE_COLON_FROM;
        else state = STATE_DEFAULT;
        break;
      case STATE_COLON_FROM:
        if (c == ' ') continue;
        else state = STATE_FROM;
        
      // Fall-through intended
      case STATE_FROM:
        if (c == '\n' || c == '\r') state = STATE_DEFAULT;
        else lFrom.append(c);
        break;
        
      // -- TO
      case STATE_T:
        if (c == 'o') state = STATE_O_TO;
        else state = STATE_DEFAULT;
        break;
      case STATE_O_TO:
        if (c == ':') state = STATE_COLON_TO;
        else state = STATE_DEFAULT;
        break;
      case STATE_COLON_TO:
        if (c == ' ') continue;
        else state = STATE_TO;
        
      // Fall-through intended
      case STATE_TO:
        if (c == '\n' || c == '\r') state = STATE_DEFAULT;
        else lTo.append(c);
        break;
      }
    }
  }
  
  static final int STATE_DATE_TIME = 1;
  static final int STATE_INOUT = 2;
  static final int STATE_IN_N = 3;
  static final int STATE_OUT_U = 4;
  static final int STATE_OUT_T = 5;
  static final int STATE_ORIGIN = 6;
  static final int STATE_ARROW = 7;
  static final int STATE_DESTINATION = 8;
  
  /**
   * 
   * @param pDetails the string to parse
   * @param pDate out
   * @param pOrigin out
   * @param pDestination out
   */
  public static void parseDetails(String pDetails,
                                  StringBuilder pDate,
                                  StringBuilder pOrigin,
                                  StringBuilder pDestination) {
    int state = STATE_DEFAULT;
    
    for (int n=0; n<pDetails.length(); n++) {
      char c = pDetails.charAt(n);
      
      switch (state) {
      // -- date
      case STATE_DEFAULT:
        if (c == '[') state = STATE_DATE_TIME;
        break;
      case STATE_DATE_TIME:
        if (c == ']') state = STATE_INOUT;
        else pDate.append(c);
        break;
        
      // -- in or out
      case STATE_INOUT:
        if (c == ' ') continue;
        else if (c == 'I') state = STATE_IN_N;
        else if (c == 'O') state = STATE_OUT_U;
        else throw new RuntimeException("Expected IN or OUT, found unexpected char " + c);
        break;
      case STATE_IN_N:
        if (c == 'N') state = STATE_ORIGIN;
        else throw new RuntimeException("Expected IN, found unexpected char " + c);
        break;
      case STATE_OUT_U:
        if (c == 'U') state = STATE_OUT_T;
        else throw new RuntimeException("Expected OUT, found unexpected char " + c);
        break;
      case STATE_OUT_T:
        if (c == 'T') state = STATE_ORIGIN;
        else throw new RuntimeException("Expected OUT, found unexpected char " + c);
        break;
        
      // -- origin
      case STATE_ORIGIN:
        if (c == ' ') continue;
        else if (c == '-') state = STATE_ARROW;
        else pOrigin.append(c);
        break;
      
      // -- arrow
      case STATE_ARROW:
        if (c == '-') continue;
        else if (c == '>') state = STATE_DESTINATION;
        break;
      
      // -- destination
      case STATE_DESTINATION:
        if (c == ' ') continue;
        else if (c == '\n' || c == '\r') break;
        else pDestination.append(c);
        break;
      }
    }
  }
  
  /**
   * Split a string in 2 parts, first the details then the rest of the message.
   * This method is more efficient than using Scanner with a regex.
   * @param pStr
   * @return the 2 parts
   */
  public static String[] splitIn2Parts(String pStr) {
    String[] lOut = new String[2];
    int lFirstLineBreak = pStr.indexOf('\n');
    lOut[0] = pStr.substring(0, lFirstLineBreak).trim();
    // skip second line because it is empty.
    lOut[1] = pStr.substring(pStr.indexOf('\n', lFirstLineBreak) + 1).trim();
    
    return lOut;
  }
}
