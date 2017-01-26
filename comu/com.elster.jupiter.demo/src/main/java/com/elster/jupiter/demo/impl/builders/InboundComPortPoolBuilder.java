package com.elster.jupiter.demo.impl.builders;

import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;

public class InboundComPortPoolBuilder extends NamedBuilder<InboundComPortPool, InboundComPortPoolBuilder> {
    private final ProtocolPluggableService protocolPluggableService;
    private final EngineConfigurationService engineConfigurationService;

    private boolean isActive = false;

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

    @Override
    public Optional<InboundComPortPool> find() {
        return engineConfigurationService.findInboundComPortPoolByName(getName());
    }

    @Override
    public InboundComPortPool create() {
        InboundDeviceProtocolPluggableClass protocolPluggableClass = protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName("com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover").get(0);
        InboundComPortPool inboundComPortPool = engineConfigurationService.newInboundComPortPool(getName(), ComPortType.SERVLET, protocolPluggableClass, Collections.emptyMap());
        inboundComPortPool.setActive(isActive);
        inboundComPortPool.update();
        return inboundComPortPool;
    }
}
