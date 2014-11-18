package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.stream.Collectors;

public class InboundComPortPoolInfo extends ComPortPoolInfo<InboundComPortPool> {

    public InboundComPortPoolInfo() {
    }

    public InboundComPortPoolInfo(InboundComPortPool comPortPool) {
        super(comPortPool);
        this.discoveryProtocolPluggableClassId = comPortPool.getDiscoveryProtocolPluggableClass().getId();
        this.inboundComPorts = comPortPool.getComPorts().stream().map(ComPortInfoFactory::asInboundInfo).collect(Collectors.toList());
    }

    @Override
    protected InboundComPortPool writeTo(InboundComPortPool source, ProtocolPluggableService protocolPluggableService) {
        super.writeTo(source, protocolPluggableService);
        protocolPluggableService.findInboundDeviceProtocolPluggableClass(this.discoveryProtocolPluggableClassId).ifPresent(source::setDiscoveryProtocolPluggableClass);
        return source;
    }

    @Override
    protected InboundComPortPool createNew(EngineModelService engineModelService) {
        return engineModelService.newInboundComPortPool();
    }

}