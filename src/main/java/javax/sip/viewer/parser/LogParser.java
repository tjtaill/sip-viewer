package javax.sip.viewer.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.TracesSession;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Parse the log files - sort them and organize by call id.
 */
public class LogParser extends DefaultHandler {
  private static final String CLASS_NAME = LogParser.class.getName();
  private static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
  private static final Logger sLogger = Logger.getLogger(PACKAGE_NAME);
  private List<TracesSession> mTraceSessions = new ArrayList<TracesSession>(100000);
  private Map<String, TracesSession> mTraceSessionIndex = new TreeMap<String, TracesSession>();

  private XMLReader xmlReader;

  public LogParser() {
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

      InputStream lFormattedStream = new SequenceInputStream(new SequenceInputStream(lPreFile, pInputStream), lPostFile);
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

    public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
      currentTag = qName;
      if (!qName.equals("message")) {
        return;
      }
      sipMessage = new SipMessage();
      sipMessage.setFrom(attrs.getValue("from"));
      sipMessage.setTo(attrs.getValue("to"));
      sipMessage.setDelay(Long.parseLong(attrs.getValue("time")));
      sipMessage.setTime(Long.parseLong(attrs.getValue("time")));
      sipMessage.setStatusInfo(attrs.getValue("statusMessage"));
      sipMessage.setTransactionId(attrs.getValue("transactionId"));
      sipMessage.setFirstLine(attrs.getValue("firstLine"));
      sipMessage.setCallId(attrs.getValue("callId"));
      sipMessage.setSender(Boolean.valueOf(attrs.getValue("isSender")));
    }

    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
      if (!qName.equals("message")) {
        return;
      }
      Pattern lToTagPattern = Pattern.compile(".*^To:.*?tag=s(\\d*)-.*$.*", Pattern.DOTALL | Pattern.MULTILINE);
      Pattern lFromTagPattern = Pattern.compile(".*^From:.*?tag=s(\\d*)-.*$.*", Pattern.DOTALL | Pattern.MULTILINE);
      Matcher lToTagMatcher = lToTagPattern.matcher(sipMessage.getContent().toString());
      Matcher lFromTagMatcher = lFromTagPattern.matcher(sipMessage.getContent().toString());
      String lFromTag = null;
      String lToTag = null;
      TracesSession lTraceSession;
      sLogger.finer("Analyse " + sipMessage.getFirstLine());

      // populate variables
      String lCallId = sipMessage.getCallId();
      if (lFromTagMatcher.matches()) {
        lFromTag = lFromTagMatcher.group(1);
      }
      if (lToTagMatcher.matches()) {
        lToTag = lToTagMatcher.group(1);
      }
      
      // look for existing session
      lTraceSession = getTraceSession(lToTag, lFromTag,sipMessage.getCallId());
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
      String lContent = sipMessage.getContent();
      sipMessage.setContent(lContent + str);
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
        }
      }

    }
  }

}
