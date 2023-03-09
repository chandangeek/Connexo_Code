/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus;


import com.energyict.mdc.upl.InboundDiscoveryContext;

import java.time.ZoneId;

public class InboundContext {

    private MerlinLogger logger;
    private InboundDiscoveryContext inboundDiscoveryContext;
    private ZoneId timeZone;
    private String encryptionKey;

    public InboundContext(MerlinLogger merlinLogger, InboundDiscoveryContext context) {
        merlinLogger.setLogger(context.getLogger()); // set the logger coming from ComServer
        setLogger(merlinLogger);
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
