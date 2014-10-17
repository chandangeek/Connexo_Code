package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.Optional;

import java.util.ArrayList;
import java.util.List;

public class InboundComPortPoolInfo extends ComPortPoolInfo<InboundComPortPool> {

    public InboundComPortPoolInfo() {
    }

    public InboundComPortPoolInfo(InboundComPortPool comPortPool) {
        super(comPortPool);
        Optional<InboundDeviceProtocolPluggableClass> discoveryProtocolPluggableClass = Optional.ofNullable(comPortPool.getDiscoveryProtocolPluggableClass());
        if(discoveryProtocolPluggableClass.isPresent()) {
            this.discoveryProtocolPluggableClassId = discoveryProtocolPluggableClass.get().getId();
        } else {
            this.discoveryProtocolPluggableClassId = 0L;
        }
        Optional<List<InboundComPort>> comPorts = Optional.ofNullable(comPortPool.getComPorts());
        if (comPorts.isPresent()) {
            this.inboundComPorts = new ArrayList<>(comPorts.get().size());
            for (InboundComPort inboundComPort : comPorts.get()) {
                this.inboundComPorts.add(ComPortInfoFactory.asInboundInfo(inboundComPort));
            }
        }
    }

    @Override
    protected InboundComPortPool writeTo(InboundComPortPool source, ProtocolPluggableService protocolPluggableService) {
        super.writeTo(source, protocolPluggableService);
        Optional<Long> discoveryProtocolPluggableClassId = Optional.ofNullable(this.discoveryProtocolPluggableClassId);
        source.setDiscoveryProtocolPluggableClass(protocolPluggableService.findInboundDeviceProtocolPluggableClass(discoveryProtocolPluggableClassId.orElse(0L)));
        return source;
    }

    @Override
    protected InboundComPortPool createNew(EngineModelService engineModelService) {
        return engineModelService.newInboundComPortPool();
    }

}
