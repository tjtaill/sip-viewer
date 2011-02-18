package javax.sip.viewer.eclipse.plugins.popup.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.List;

import javax.sip.viewer.model.TraceSession;
import javax.sip.viewer.parser.SipLogParser;
import javax.sip.viewer.parser.TextLogParser;
import javax.sip.viewer.parser.XmlLogParser;

public class SipLogAdapterParser implements SipLogParser {
  private TextLogParser mTextLogParser = new TextLogParser();
  private XmlLogParser mXmlLogParser = new XmlLogParser();

  public List<TraceSession> parseLogs(InputStream pSource) {
    char a = 0;
    try {
      a = (char)pSource.read();
      InputStream lReconstructedStream = new SequenceInputStream(new ByteArrayInputStream(new byte[]{(byte)a}),
                                                                 pSource);

      if (a == '<') {
        return mXmlLogParser.parseLogs(lReconstructedStream);
      } else {
        return mTextLogParser.parseLogs(lReconstructedStream);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
