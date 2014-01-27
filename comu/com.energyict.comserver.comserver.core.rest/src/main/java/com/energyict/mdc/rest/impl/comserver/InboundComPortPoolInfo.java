package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

public class InboundComPortPoolInfo extends ComPortPoolInfo<InboundComPortPool> {

    public InboundComPortPoolInfo() {
    }

    public InboundComPortPoolInfo(InboundComPortPool comPortPool) {
        super(comPortPool);
        this.discoveryProtocolPluggableClassId = comPortPool.getDiscoveryProtocolPluggableClassId();
        if (comPortPool.getComPorts()!=null) {
            this.inboundComPorts = new ArrayList<>(comPortPool.getComPorts().size());
            for (InboundComPort inboundComPort : comPortPool.getComPorts()) {
                inboundComPorts.add(ComPortInfoFactory.asInboundInfo(inboundComPort));
            }
        }
    }

    @Override
    protected InboundComPortPool writeTo(InboundComPortPool source,EngineModelService engineModelService) {
        super.writeTo(source,engineModelService);
        source.setDiscoveryProtocolPluggableClassId(this.discoveryProtocolPluggableClassId);
        // TODO throw an exception when comPorts differ: should be managed by ComPort, not InboundComPortPool
//        if (this.inboundComPorts !=null && source.getComPorts()!=null) {
//
//            throw new WebApplicationException("Could not find comPort with id " + inboundComPortInfo.id, Response.Status.BAD_REQUEST);
//        }
        return source;
    }

    @Override
    protected InboundComPortPool createNew(EngineModelService engineModelService) {
        return engineModelService.newInboundComPortPool();
    }
}
