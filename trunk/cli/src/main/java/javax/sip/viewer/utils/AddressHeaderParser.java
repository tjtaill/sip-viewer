package javax.sip.viewer.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressHeaderParser {
  private static Pattern sPattern = Pattern.compile("((?:\"?+(.*?)\"?+)?[ ]?<?[^<]*?:([^@^;]*?)(;[^@^>]*)?@?([^@^;^>]*?)(?:;[ ]?([^@^>]*))?>?)(?:;[ ]?([^>]*))?");
  private static int PATTERN_GROUP_ADDRESS = 1;                               
  private static int PATTERN_GROUP_DISPLAY_NAME = 2;
  private static int PATTERN_GROUP_URI_USER = 3;
  private static int PATTERN_GROUP_URI_USER_PARAMS = 4;
  private static int PATTERN_GROUP_URI_HOST = 5;
  private static int PATTERN_GROUP_URI_PARAMS = 6;
  private static int PATTERN_GROUP_HEADER_PARAMS = 7;
  private final Matcher mMatcher;

  public AddressHeaderParser(String pHeaderValue) {
    mMatcher = sPattern.matcher(pHeaderValue);
    if (!mMatcher.matches()) {
      throw new RuntimeException("Does not match address header pattern");
    }
  }

  public String getAddress() {
    return mMatcher.group(PATTERN_GROUP_ADDRESS);
  }

  public String getDisplayName() {
    return mMatcher.group(PATTERN_GROUP_DISPLAY_NAME);
  }

  public String getUriUser() {
    return mMatcher.group(PATTERN_GROUP_URI_USER);
  }

  public String getUriHost() {
    return mMatcher.group(PATTERN_GROUP_URI_HOST);
  }

  public Map<String, String> getUriParams() {
    Map<String, String> lUriParams = new HashMap<String, String>();
    String lParamsString = mMatcher.group(PATTERN_GROUP_URI_PARAMS);
    if (lParamsString != null) {
      String[] lParams = lParamsString.split(";");
      for (String lParam : lParams) {
        String[] lKeyValue = lParam.split("=");
        String lKey = lKeyValue[0].trim();
        String lValue = null;
        if (lKeyValue.length == 2) {
          lValue = lKeyValue[1].trim();
        }
        lUriParams.put(lKey, lValue);
      }
    }
    return lUriParams;
  }

  public Map<String, String> getHeaderParams() {
    Map<String, String> lHeaderParams = new HashMap<String, String>();
    String lParamsString = mMatcher.group(PATTERN_GROUP_HEADER_PARAMS);
    if (lParamsString != null) {
      String[] lParams = lParamsString.split(";");
      for (String lParam : lParams) {
        String[] lKeyValue = lParam.split("=");
        String lKey = lKeyValue[0].trim();
        String lValue = null;
        if (lKeyValue.length == 2) {
          lValue = lKeyValue[1].trim();
        }
        lHeaderParams.put(lKey, lValue);
      }
    }
    return lHeaderParams;
  }

  /**
   * @return
   */
  public Map<String, String> getUriUserParams() {
    Map<String, String> lUriParams = new HashMap<String, String>();
    String lParamsString = mMatcher.group(PATTERN_GROUP_URI_USER_PARAMS);
    if (lParamsString != null) {
      String[] lParams = lParamsString.split(";");
      for (String lParam : lParams) {
        String[] lKeyValue = lParam.split("=");
        String lKey = lKeyValue[0].trim();
        String lValue = null;
        if (lKeyValue.length == 2) {
          lValue = lKeyValue[1].trim();
        }
        lUriParams.put(lKey, lValue);
      }
    }
    return lUriParams;
  }

}
