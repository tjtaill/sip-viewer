package javax.sip.viewer.parser;


import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang3.StringUtils;

import javax.sip.viewer.model.SipMessage;
import javax.sip.viewer.model.SipMessageDirection;
import javax.sip.viewer.model.TraceSessionIndexer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SipMessageListener extends XsLogBaseListener {
    private final TraceSessionIndexer traceSessionIndexer;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");
    private Date date;
    private SipMessageDirection direction;
    private String sipMessageText;
    private String toTag;
    private String fromTag;
    private String callId;



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
    public void enterDirectionLine(@NotNull XsLogParser.DirectionLineContext ctx) {
        direction = ctx.DIRECTION_LINE().getText().contains("IN") ? SipMessageDirection.IN :
                SipMessageDirection.OUT;
    }

    @Override
    public void enterCallId(@NotNull XsLogParser.CallIdContext ctx) {
        callId = ctx.CALL_ID().getText().split(":", 2)[1];
    }

    @Override
    public void exitSipMessage(@NotNull XsLogParser.SipMessageContext ctx) {


        SipMessage sipMessage = new SipMessage();
        sipMessage.setMessageAsText(ctx.getText());
        // sipMessage.setSource()

        traceSessionIndexer.indexSipMessage(sipMessage, toTag, fromTag, fromTag, callId, null);
    }
}
