/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.InboundComPortPoolBuilder;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.ports.ComPortType;

public enum InboundComPortPoolTpl implements Template<InboundComPortPool, InboundComPortPoolBuilder> {
    INBOUND_SERVLET_POOL_NAME("Inbound Servlet Pool", false, "com.energyict.mdc.protocol.inbound.dlms.DlmsSerialNumberDiscover", ComPortType.SERVLET),
    INBOUND_SERVLET_BEACON_PSK("Beacon PSK", true, "com.energyict.mdc.protocol.inbound.g3.Beacon3100PushEventNotification", ComPortType.TCP);

    private String name;
    private boolean isActive;
    private String protocolPluggableClassName;
    private ComPortType comPortType;

    InboundComPortPoolTpl(String name, boolean isActive, String protocolPluggableClassName, ComPortType comPortType) {
        this.name = name;
        this.isActive = isActive;
        this.protocolPluggableClassName = protocolPluggableClassName;
        this.comPortType = comPortType;
    }

    @Override
    public Class<InboundComPortPoolBuilder> getBuilderClass() {
        return InboundComPortPoolBuilder.class;
    }

    @Override
    public InboundComPortPoolBuilder get(InboundComPortPoolBuilder builder) {
        return builder.withName(this.name).withActiveStatus(isActive).withComPortType(comPortType)
                .withInboundComPortPool(protocolPluggableClassName);
    }

    public String getName() {
        return name;
    }
}
