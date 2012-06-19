package javax.sip.viewer.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.TraceSession;
import javax.sip.viewer.model.TraceSessionIndexer;
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

  private TraceSessionIndexer mTraceSessionIndexer = new TraceSessionIndexer();

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

  public List<TraceSession> parseLogs(InputStream pInputStream) {
    try {
      InputStream lPreFile = new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>\n<messages>\n".getBytes("UTF-8"));
      InputStream lPostFile = new ByteArrayInputStream("</messages>\n".getBytes("UTF-8"));

      InputStream lFormattedStream = new SequenceInputStream(new SequenceInputStream(lPreFile,
                                                                                     pInputStream),
                                                             lPostFile);
      InputSource inputSource = new InputSource(lFormattedStream);
      inputSource.setEncoding("UTF-8");
      xmlReader.parse(inputSource);
      return mTraceSessionIndexer.getTraceSessions();
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

      // populate variables
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

      String lEventId = null; // subscribe event id correlation not yet supported
      mTraceSessionIndexer.indexSipMessage(sipMessage, lToTag, lFromTag, lCallId, null);
    }

    public void characters(char[] buf, int offset, int len) throws SAXException {
      if (!currentTag.equals("message")) {
        return;
      }
      if (buf == null)
        return;
      String str = new String(buf, offset, len);
      str = str.trim();

      if (str.equals(""))
        return;
      String lContent = sipMessage.getMessageAsText();
      sipMessage.setMessageAsText(lContent + str);
    }

    public void endDocument() throws SAXException {
      // Nothing to do.
    }
  }

}
