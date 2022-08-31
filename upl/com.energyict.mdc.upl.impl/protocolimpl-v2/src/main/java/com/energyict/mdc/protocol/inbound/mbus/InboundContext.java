package com.energyict.mdc.protocol.inbound.mbus;


public class InboundContext {

    private MerlinLogger logger;

    public InboundContext(MerlinLogger logger) {
        setLogger(logger);
    }

    private void setLogger(MerlinLogger logger) {
        this.logger = logger;
    }

    public MerlinLogger getLogger() {
        return this.logger;
    }
}
