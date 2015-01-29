package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.InboundComPortPoolBuilder;
import com.energyict.mdc.engine.config.InboundComPortPool;

public enum InboundComPortPoolTpl implements Template<InboundComPortPool, InboundComPortPoolBuilder> {
    INBOUND_SERVLET_POOL("Inbound Servlet Pool"),
    ;
    private String name;

    InboundComPortPoolTpl(String name) {
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
