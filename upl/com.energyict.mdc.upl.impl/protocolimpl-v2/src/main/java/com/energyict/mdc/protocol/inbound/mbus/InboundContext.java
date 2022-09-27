package com.energyict.mdc.protocol.inbound.mbus;


import com.energyict.mdc.upl.InboundDiscoveryContext;

import java.time.ZoneId;

public class InboundContext {

    private MerlinLogger logger;
    private InboundDiscoveryContext inboundDiscoveryContext;
    private ZoneId timeZone;
    private String encryptionKey;// = "4F A7 0B 24 46 5F 81 4A 66 76 31 77 3A 39 76 44"; // some default used for PoC

    public InboundContext(MerlinLogger logger, InboundDiscoveryContext context) {
        setLogger(logger);
        setInboundDiscoveryContext(context);
    }

    private void setInboundDiscoveryContext(InboundDiscoveryContext context) {
        this.inboundDiscoveryContext = context;
    }

    public InboundDiscoveryContext getInboundDiscoveryContext() {
        return inboundDiscoveryContext;
    }

    private void setLogger(MerlinLogger logger) {
        this.logger = logger;
    }

    public MerlinLogger getLogger() {
        return this.logger;
    }

    public void setTimeZone(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }
}
