/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;

public class InboundComPortPoolBuilder extends NamedBuilder<InboundComPortPool, InboundComPortPoolBuilder> {
    private final ProtocolPluggableService protocolPluggableService;
    private final EngineConfigurationService engineConfigurationService;

    private boolean isActive = false;
    private String protocolPluggableClassName;
    private ComPortType comPortType;

    @Inject
    public InboundComPortPoolBuilder(ProtocolPluggableService protocolPluggableService, EngineConfigurationService engineConfigurationService) {
        super(InboundComPortPoolBuilder.class);
        this.protocolPluggableService = protocolPluggableService;
        this.engineConfigurationService = engineConfigurationService;
    }

    public InboundComPortPoolBuilder withActiveStatus(boolean isActive){
        this.isActive = isActive;
        return this;
    }

    public InboundComPortPoolBuilder withInboundComPortPool(String protocolPluggableClass) {
        this.protocolPluggableClassName = protocolPluggableClass;
        return this;
    }

    public InboundComPortPoolBuilder withComPortType(ComPortType comPortType) {
        this.comPortType = comPortType;
        return this;
    }

    @Override
    public Optional<InboundComPortPool> find() {
        return engineConfigurationService.findInboundComPortPoolByName(getName());
    }

    @Override
    public InboundComPortPool create() {
        InboundDeviceProtocolPluggableClass protocolPluggableClass = protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(protocolPluggableClassName).get(0);
        InboundComPortPool inboundComPortPool = engineConfigurationService.newInboundComPortPool(getName(), comPortType, protocolPluggableClass, Collections.emptyMap());
        inboundComPortPool.setActive(isActive);
        inboundComPortPool.update();
        return inboundComPortPool;
    }
}
