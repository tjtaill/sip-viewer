package javax.sip.viewer.parser;


import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class SipAddressParser {
    public static String parseIpOrDns(String sipUrl) {
        ANTLRInputStream input = new ANTLRInputStream(sipUrl);

        SipUrlLexer lexer = new SipUrlLexer( input );

        CommonTokenStream tokens = new CommonTokenStream( lexer );

        SipUrlParser parser = new SipUrlParser( tokens );
        ParseTree tree = parser.sipUrl();
        ParseTreeWalker walker = new ParseTreeWalker();
        SipAddressListener sipAddressListener = new SipAddressListener();
        walker.walk(sipAddressListener , tree);
        return sipAddressListener.getSipAddress();
    }

    public static void main(String[] args) {

        System.out.println(SipAddressParser.parseIpOrDns("5146986600@mtlasdev86.net") );
        System.out.println(SipAddressParser.parseIpOrDns("5146986604@10.9.39.11:5050") );
        System.out.println(SipAddressParser.parseIpOrDns("5146976604@mtlasdev55.net;user=phone") );


    }
}
