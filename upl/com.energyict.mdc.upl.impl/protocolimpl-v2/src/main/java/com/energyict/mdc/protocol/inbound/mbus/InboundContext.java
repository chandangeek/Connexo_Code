package com.energyict.mdc.protocol.inbound.mbus;


import com.energyict.mdc.upl.InboundDiscoveryContext;

public class InboundContext {

    private MerlinLogger logger;
    private InboundDiscoveryContext inboundDiscoveryContext;

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
}
