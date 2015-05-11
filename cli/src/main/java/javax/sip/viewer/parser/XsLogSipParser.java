package javax.sip.viewer.parser;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.sip.viewer.model.TraceSession;
import javax.sip.viewer.model.TraceSessionIndexer;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class XsLogSipParser implements SipLogParser {


    @Override
    public List<TraceSession> parseLogs(InputStream pInputStream) {

        TraceSessionIndexer traceSessionIndexer = new TraceSessionIndexer();

        try {
            ANTLRInputStream input = new ANTLRInputStream( pInputStream );
            
            XsLogLexer lexer = new XsLogLexer(input);

            CommonTokenStream tokens  = new CommonTokenStream( lexer );
            
            XsLogParser parser = new XsLogParser( tokens );

            ParseTree tree = parser.xsLog();

            ParseTreeWalker walker = new ParseTreeWalker();

            walker.walk( new SipMessageListener(traceSessionIndexer), tree);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return traceSessionIndexer.getTraceSessions();

    }
}
