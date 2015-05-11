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
    private final TraceSessionIndexer traceSessionIndexer;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");
    private static Pattern tagPattern = Pattern.compile(";tag=([\\d-]+)");
    private static Pattern sipUrlPattern = Pattern.compile("\\<sip:(.*?)\\>");
    private static Pattern ipPattern = Pattern.compile("(\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}:?\\d{0,5})");
    private Date date;
    private SipMessageDirection direction;
    private String sipMessageText;
    private String toTag;
    private String fromTag;
    private String callId;
    private String destination;
    private String source;
    private MessageType messageType;

    private enum MessageType {
        REQUEST,
        RESPONSE
    }

    public SipMessageListener(TraceSessionIndexer traceSessionIndexer) {
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
    public void enterSipMessage(@NotNull XsLogParser.SipMessageContext ctx) {
        String sipMessageText = ctx.getText();
        System.out.println(sipMessageText);
    }

    @Override
    public void enterTo(@NotNull XsLogParser.ToContext ctx) {
        String to = ctx.TO().getText();
        Matcher matcher = tagPattern.matcher(to);
        toTag = matcher.find() ? matcher.group(1) : null;

        matcher = sipUrlPattern.matcher(to);
        String toAddress = matcher.find() ? matcher.group(1) : null;
        toAddress = SipAddressParser.parseIpOrDns(toAddress);
        toAddress = toAddress.contains(":") ? toAddress : toAddress + ":5060";
        switch (messageType) {
            case REQUEST:
                destination = toAddress;
                break;
            case RESPONSE:
                source = toAddress;
                break;
        }
    }

    @Override
    public void enterFrom(@NotNull XsLogParser.FromContext ctx) {
        String from = ctx.FROM().getText();
        Matcher matcher = tagPattern.matcher(from);
        fromTag = matcher.find() ? matcher.group(1) : null;

        matcher = sipUrlPattern.matcher(from);
        String fromAddress = matcher.find() ? matcher.group(1) : null;
        fromAddress = SipAddressParser.parseIpOrDns(fromAddress);
        fromAddress = fromAddress.contains(":") ? fromAddress : fromAddress + ":5060";
        switch (messageType) {
            case REQUEST:
                source = fromAddress;
                break;
            default:
                break;
        }
    }

    @Override
    public void enterVia(@NotNull XsLogParser.ViaContext ctx) {
        switch (messageType) {
            case RESPONSE:
                String via = ctx.VIA().getText();
                Matcher matcher = ipPattern.matcher( via );
                destination = matcher.find() ? matcher.group(1) : null;
                destination = destination.contains(":") ? destination : destination + ":5060";
                break;
            default:
                break;
        }
    }

    @Override
    public void enterDirectionLine(@NotNull XsLogParser.DirectionLineContext ctx) {
        direction = ctx.DIRECTION_LINE().getText().contains("IN") ? SipMessageDirection.IN :
                SipMessageDirection.OUT;
    }

    @Override
    public void enterCallId(@NotNull XsLogParser.CallIdContext ctx) {
        callId = ctx.CALL_ID().getText().split(":", 2)[1];
    }

    @Override
    public void enterContact(@NotNull XsLogParser.ContactContext ctx) {
        String contact = ctx.CONTACT().getText();

        Matcher matcher = sipUrlPattern.matcher(contact);
        String fromAddress = matcher.find() ? matcher.group(1) : null;
        fromAddress = SipAddressParser.parseIpOrDns( fromAddress );
        source = fromAddress;
    }

    @Override
    public void exitSipMessage(@NotNull XsLogParser.SipMessageContext ctx) {
        SipMessage sipMessage = new SipMessage();
        sipMessageText = ctx.getText();
        sipMessage.setMessageAsText(sipMessageText);
        sipMessage.setSource(source);
        sipMessage.setDirection(direction);
        sipMessage.setDestination(destination);
        sipMessage.setTime( date.getTime() );

        traceSessionIndexer.indexSipMessage(sipMessage, toTag, fromTag, null, callId, null);
    }
}
