package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Store;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;

public class InboundComPortPoolFactory extends NamedFactory<InboundComPortPoolFactory, InboundComPortPool> {
    private final Store store;
    private final ProtocolPluggableService protocolPluggableService;
    private final EngineConfigurationService engineConfigurationService;

    private boolean isActive = false;

    @Inject
    public InboundComPortPoolFactory(Store store, ProtocolPluggableService protocolPluggableService, EngineConfigurationService engineConfigurationService) {
        super(InboundComPortPoolFactory.class);
        this.store = store;
        this.protocolPluggableService = protocolPluggableService;
        this.engineConfigurationService = engineConfigurationService;
    }

    public InboundComPortPoolFactory withActiveStatus(boolean isActive){
        this.isActive = isActive;
        return this;
    }

    @Override
    public InboundComPortPool get() {
        InboundDeviceProtocolPluggableClass protocolPluggableClass = protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName("com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover").get(0);
        InboundComPortPool inboundComPortPool = engineConfigurationService.newInboundComPortPool(getName(), ComPortType.SERVLET, protocolPluggableClass);
        inboundComPortPool.setActive(isActive);
        inboundComPortPool.save();
        store.add(InboundComPortPool.class, inboundComPortPool);
        return inboundComPortPool;
    }
}
