package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

public class InboundComPortPoolInfo extends ComPortPoolInfo<InboundComPortPool> {

    public InboundComPortPoolInfo() {
    }

    public InboundComPortPoolInfo(InboundComPortPool comPortPool) {
        super(comPortPool);
        Optional<InboundDeviceProtocolPluggableClass> discoveryProtocolPluggableClass = Optional.fromNullable(comPortPool.getDiscoveryProtocolPluggableClass());
        if(discoveryProtocolPluggableClass.isPresent()) {
            this.discoveryProtocolPluggableClassId = discoveryProtocolPluggableClass.get().getId();
        } else {
            this.discoveryProtocolPluggableClassId = 0L;
        }
        Optional<List<InboundComPort>> comPorts = Optional.fromNullable(comPortPool.getComPorts());
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
        Optional<Long> discoveryProtocolPluggableClassId = Optional.fromNullable(this.discoveryProtocolPluggableClassId);
        source.setDiscoveryProtocolPluggableClass(protocolPluggableService.findInboundDeviceProtocolPluggableClass(discoveryProtocolPluggableClassId.or(0L)));
        return source;
    }

    @Override
    protected InboundComPortPool createNew(EngineModelService engineModelService) {
        return engineModelService.newInboundComPortPool();
    }

    @Override
    protected void handlePools(InboundComPortPool inboundComPortPool, EngineModelService engineModelService, boolean all) {
        // Nothing to do: ComPorts are handled through ComPort, not ComPortPool
        // TODO throw an exception when comPorts differ: should be managed by ComPort, not InboundComPortPool
//            throw new WebApplicationException("Could not find comPort with id " + inboundComPortInfo.id, Response.Status.BAD_REQUEST);
    }
}
