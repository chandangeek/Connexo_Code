/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.InboundComPortPoolBuilder;
import com.energyict.mdc.engine.config.InboundComPortPool;

public class InboundComPortPoolTpl implements Template<InboundComPortPool, InboundComPortPoolBuilder>{

    public final static String INBOUND_SERVLET_POOL_NAME = "Inbound Servlet Pool";

    private String name;

    public InboundComPortPoolTpl(String name) {
        this.name = name;
    }

    @Override
    public Class<InboundComPortPoolBuilder> getBuilderClass() {
        return InboundComPortPoolBuilder.class;
    }

    @Override
    public InboundComPortPoolBuilder get(InboundComPortPoolBuilder builder) {
        return builder.withName(this.name);
    }
}
