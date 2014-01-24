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
        if (inboundComPorts !=null) {
            for (InboundComPortInfo inboundComPortInfo : this.inboundComPorts) {
                InboundComPort inboundComPort;
                if(inboundComPortInfo.id>0){
                    inboundComPort = (InboundComPort)engineModelService.findComPort(inboundComPortInfo.id);
                } else {
                    ComServer comServer = engineModelService.findComServer(inboundComPortInfo.comServer_id);
                    if(comServer!=null){
                        inboundComPort = inboundComPortInfo.createNew(comServer, engineModelService);
                    } else {
                        throw new WebApplicationException("Could not find comserver with id " + inboundComPortInfo.comServer_id, Response.Status.BAD_REQUEST);
                    }
                }
                inboundComPortInfo.writeTo(inboundComPort,engineModelService);
            }
        }
        return source;
    }

    @Override
    protected InboundComPortPool createNew(EngineModelService engineModelService) {
        return engineModelService.newInboundComPortPool();
    }
}
