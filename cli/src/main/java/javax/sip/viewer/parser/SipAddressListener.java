package javax.sip.viewer.parser;

import org.antlr.v4.runtime.misc.NotNull;

/**
 * Created by ttaillefer on 5/11/2015.
 */
public class SipAddressListener extends SipUrlBaseListener {
    private String sipAddress;

    @Override
    public void enterIpv4(@NotNull SipUrlParser.Ipv4Context ctx) {
        sipAddress = ctx.IPV4().getText();
    }

    @Override
    public void enterDomainName(@NotNull SipUrlParser.DomainNameContext ctx) {
        sipAddress = ctx.DOMAIN_NAME().getText();
    }

    public String getSipAddress() {
        return sipAddress;
    }
}
