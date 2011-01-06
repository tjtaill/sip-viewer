package javax.sip.viewer.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.TracesSession;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XmlLogParser extends DefaultHandler implements SipLogParser {
  private static Pattern sCallIdPattern = Pattern.compile(".*^Call-ID:[ ]?(.*?)$.*",
                                                          Pattern.DOTALL | Pattern.MULTILINE);
  private static Pattern sToTagPattern = Pattern.compile(".*^To:[ ]?.*;tag=s(\\d*)-.*?$.*",
                                                         Pattern.DOTALL | Pattern.MULTILINE);
  private static Pattern sFromTagPattern = Pattern.compile(".*^From:[ ]?.*;tag=s(\\d*)-.*?$.*",
                                                           Pattern.DOTALL | Pattern.MULTILINE);

  private List<TracesSession> mTraceSessions = new ArrayList<TracesSession>(100000);
  private Map<String, TracesSession> mTraceSessionIndex = new TreeMap<String, TracesSession>();

  private XMLReader xmlReader;

  public XmlLogParser() {
    try {
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      SAXParser saxParser = saxParserFactory.newSAXParser();
      this.xmlReader = saxParser.getXMLReader();
      xmlReader.setContentHandler(new XmlCalllogHandler());
      xmlReader.setFeature("http://xml.org/sax/features/validation", false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<TracesSession> parseLogs(InputStream pInputStream) {
    try {
      InputStream lPreFile = new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>\n<messages>\n".getBytes("UTF-8"));
      InputStream lPostFile = new ByteArrayInputStream("</messages>\n".getBytes("UTF-8"));

      InputStream lFormattedStream = new SequenceInputStream(new SequenceInputStream(lPreFile,
                                                                                     pInputStream),
                                                             lPostFile);
      InputSource inputSource = new InputSource(lFormattedStream);
      inputSource.setEncoding("UTF-8");
      xmlReader.parse(inputSource);
      return mTraceSessions;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private class XmlCalllogHandler extends DefaultHandler {
    private String currentTag;
    private SipMessage sipMessage;

    public void startElement(String namespaceURI, String lName, String qName, Attributes attrs)
      throws SAXException
    {
      currentTag = qName;
      if (!qName.equals("message")) {
        return;
      }
      sipMessage = new SipMessage();
      sipMessage.setSource(attrs.getValue("from"));
      sipMessage.setDestination(attrs.getValue("to"));
      sipMessage.setDelay(Long.parseLong(attrs.getValue("time")));
      sipMessage.setTime(Long.parseLong(attrs.getValue("time")));
    }

    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
      if (!qName.equals("message")) {
        return;
      }

      Matcher lCallIdMatcher = sCallIdPattern.matcher(sipMessage.getMessageAsText());
      Matcher lToTagMatcher = sToTagPattern.matcher(sipMessage.getMessageAsText());
      Matcher lFromTagMatcher = sFromTagPattern.matcher(sipMessage.getMessageAsText());
      String lCallId = null;
      String lFromTag = null;
      String lToTag = null;
      TracesSession lTraceSession;

      // populate variables
      System.out.println(sipMessage.getMessageAsText());
      lCallIdMatcher.matches();
      if (lCallIdMatcher.matches()) {
        lCallId = lCallIdMatcher.group(1);
      }
      if (lFromTagMatcher.matches()) {
        lFromTag = lFromTagMatcher.group(1);
      }
      if (lToTagMatcher.matches()) {
        lToTag = lToTagMatcher.group(1);
      }

      // look for existing session
      lTraceSession = getTraceSession(lToTag, lFromTag, lCallId);
      lTraceSession.add(sipMessage);

      if (lCallId != null) {
        lTraceSession.addCallId(lCallId);
        mTraceSessionIndex.put(lCallId, lTraceSession);
      }
      if (lFromTag != null) {
        lTraceSession.addB2BTagTokens(lFromTag);
        mTraceSessionIndex.put(lFromTag, lTraceSession);
      }
      if (lToTag != null) {
        lTraceSession.addB2BTagTokens(lToTag);
        mTraceSessionIndex.put(lToTag, lTraceSession);
      }
    }

    /**
     * @param pToTag
     * @param pFromTag
     * @param pCallId
     * @return
     */
    private TracesSession getTraceSession(String pToTag, String pFromTag, String pCallId) {
      TracesSession lResult;
      if (pToTag != null && mTraceSessionIndex.containsKey(pToTag)) {
        lResult = mTraceSessionIndex.get(pToTag);
      } else if (pFromTag != null && mTraceSessionIndex.containsKey(pFromTag)) {
        lResult = mTraceSessionIndex.get(pFromTag);
      } else if (pCallId != null && mTraceSessionIndex.containsKey(pCallId)) {
        lResult = mTraceSessionIndex.get(pCallId);
      } else {
        lResult = new TracesSession();
        mTraceSessions.add(lResult);
      }
      return lResult;
    }

    public void characters(char[] buf, int offset, int len) throws SAXException {
      if (!currentTag.equals("message")) {
        return;
      }
      if (buf == null)
        return;
      String str = new String(buf, offset, len); // s.toString().trim();

      if (str.equals(""))
        return;
      String lContent = sipMessage.getMessageAsText();
      sipMessage.setMessageAsText(lContent + str);
    }

    public void endDocument() throws SAXException {
      // Updates all the delays of messages and the time of the trace session
      for (TracesSession traceSession : mTraceSessions) {
        SipMessage lFirstSipMessage = (SipMessage) traceSession.getSipMessageList().get(0);
        long lInitTime = lFirstSipMessage.getDelay();
        traceSession.setTime(lFirstSipMessage.getDelay());
        for (SipMessage lSipMessage : traceSession.getSipMessageList()) {
          long lTime = lSipMessage.getDelay();
          lSipMessage.setDelay(lTime - lInitTime);
          lSipMessage.setMessageAsText(lSipMessage.getMessageAsText().trim());
        }
      }

    }
  }

}
