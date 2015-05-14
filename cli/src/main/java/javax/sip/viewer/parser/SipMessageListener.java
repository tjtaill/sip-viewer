package javax.sip.viewer.parser;


import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang3.StringUtils;

import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.SipMessageDirection;
import javax.sip.viewer.model.TraceSessionIndexer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SipMessageListener extends XsLogBaseListener {
    private final String xsAddress;
    private final TraceSessionIndexer traceSessionIndexer;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");
    private static Pattern tagPattern = Pattern.compile(";tag=([\\d-]+)");
    private static Pattern sipUrlPattern = Pattern.compile("\\<sip:(.*?)\\>");
    private static Pattern ipPattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:?\\d{0,5})");
    private Date date;
    private SipMessageDirection direction;
    private String sipMessageText;
    private String toTag;
    private String fromTag;
    private String callId;
    private String destination;
    private String source;
    private MessageType messageType;
    // this is to make sure that messages that have similar time
    // are sequenced in parsed order
    private long timeJitter = 0;

    private enum MessageType {
        REQUEST,
        RESPONSE
    }

    public SipMessageListener(TraceSessionIndexer traceSessionIndexer, String xsAddress) {
        this.xsAddress = xsAddress;
        this.traceSessionIndexer = traceSessionIndexer;
    }




    @Override
    public void enterDateTimeLine(@NotNull XsLogParser.DateTimeLineContext ctx) {
        String dateLine = ctx.DATE_TIME_LINE().getText();
        int secondSpaceIndex = StringUtils.ordinalIndexOf(dateLine, " ", 2);
        String dts = dateLine.substring(0, secondSpaceIndex);
        try {
            date = simpleDateFormat.parse(dts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void enterSipResponse(@NotNull XsLogParser.SipResponseContext ctx) {
        messageType = MessageType.RESPONSE;
    }

    @Override
    public void enterSipRequest(@NotNull XsLogParser.SipRequestContext ctx) {
        messageType = MessageType.REQUEST;
    }


    @Override
    public void enterFrom(@NotNull XsLogParser.FromContext ctx) {
        String from = ctx.FROM().getText();
        Matcher matcher = tagPattern.matcher(from);
        fromTag = matcher.find() ? matcher.group(1) : null;

    }

    @Override
    public void enterDirectionLine(@NotNull XsLogParser.DirectionLineContext ctx) {
        String directionLine = ctx.DIRECTION_LINE().getText();
        direction = directionLine.contains("IN") ? SipMessageDirection.IN :
                SipMessageDirection.OUT;
        Matcher matcher = ipPattern.matcher(directionLine);
        String loc = matcher.find() ? matcher.group(1) : null;
        loc = loc.contains(":") ? loc : loc + ":5060";
        switch(direction) {
            case IN:
                source = loc;
                destination = xsAddress;
                break;
            case OUT:
                source = xsAddress;
                destination = loc;
                break;
        }
    }

    @Override
    public void enterCallId(@NotNull XsLogParser.CallIdContext ctx) {
        callId = ctx.CALL_ID().getText().split(":", 2)[1];
    }



    @Override
    public void exitSipMessage(@NotNull XsLogParser.SipMessageContext ctx) {
        SipMessage sipMessage = new SipMessage();
        sipMessageText = ctx.getText();
        sipMessage.setMessageAsText(sipMessageText);
        sipMessage.setSource(source);
        sipMessage.setDirection(direction);
        sipMessage.setDestination(destination);
        sipMessage.setTime( date.getTime() + timeJitter++ );

        traceSessionIndexer.indexSipMessage(sipMessage, toTag, fromTag, null, callId, null);
    }
}
