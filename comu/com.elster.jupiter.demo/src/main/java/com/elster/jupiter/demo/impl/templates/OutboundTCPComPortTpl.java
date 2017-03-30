/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.OutboundTCPComPortBuilder;
import com.energyict.mdc.engine.config.OutboundComPort;

public enum OutboundTCPComPortTpl implements Template<OutboundComPort, OutboundTCPComPortBuilder> {
    OUTBOUND_TCP_1("Outbound TCP 1"),
    OUTBOUND_TCP_2("Outbound TCP 2"),
    ;

    private String name;

    OutboundTCPComPortTpl(String name) {
        this.name = name;
    }

    @Override
    public Class<OutboundTCPComPortBuilder> getBuilderClass() {
        return OutboundTCPComPortBuilder.class;
    }

    @Override
    public OutboundTCPComPortBuilder get(OutboundTCPComPortBuilder builder) {
        return builder.withName(this.name);
    }

    public String getPortName(){
        return this.name;
    }
}
