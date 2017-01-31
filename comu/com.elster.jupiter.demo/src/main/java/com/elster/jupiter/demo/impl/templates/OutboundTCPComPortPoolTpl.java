/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.OutboundTCPComPortPoolBuilder;
import com.energyict.mdc.engine.config.OutboundComPortPool;

import java.util.Arrays;
import java.util.List;

public enum OutboundTCPComPortPoolTpl implements Template<OutboundComPortPool, OutboundTCPComPortPoolBuilder> {
    VODAFONE("Vodafone", Arrays.asList(OutboundTCPComPortTpl.OUTBOUND_TCP_1.getPortName(), OutboundTCPComPortTpl.OUTBOUND_TCP_2.getPortName())),
    ORANGE("Orange", Arrays.asList(OutboundTCPComPortTpl.OUTBOUND_TCP_1.getPortName(), OutboundTCPComPortTpl.OUTBOUND_TCP_2.getPortName())),
    ;

    private String name;
    private List<String> comPortNamePatterns;

    OutboundTCPComPortPoolTpl(String name, List<String> comPortNamePatterns) {
        this.name = name;
        this.comPortNamePatterns = comPortNamePatterns;
    }

    @Override
    public Class<OutboundTCPComPortPoolBuilder> getBuilderClass() {
        return OutboundTCPComPortPoolBuilder.class;
    }

    @Override
    public OutboundTCPComPortPoolBuilder get(OutboundTCPComPortPoolBuilder builder) {
        return builder.withName(this.name).withComPortNames(this.comPortNamePatterns);
    }

    public String getName() {
        return name;
    }
}
