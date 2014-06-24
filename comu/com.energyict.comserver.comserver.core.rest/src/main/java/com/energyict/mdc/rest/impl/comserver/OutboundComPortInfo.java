package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;

import java.util.ArrayList;
import java.util.List;


public abstract class OutboundComPortInfo extends ComPortInfo<OutboundComPort, OutboundComPort.OutboundComPortBuilder> {

    public List<Long> outboundComPortPoolIds = new ArrayList<>();;

    public OutboundComPortInfo() {
        this.direction = "outbound";
        this.comPortType = ComPortType.TCP;
    }

    public OutboundComPortInfo(OutboundComPort comPort, EngineModelService engineModelService) {
        super(comPort);
        this.direction = "outbound";
        List<OutboundComPortPool> outboundComPortPools = engineModelService.findContainingComPortPoolsForComPort(comPort);
        outboundComPortPoolIds.addAll(createHasIdList(outboundComPortPools));
    }

    private List<Long> createHasIdList(List<OutboundComPortPool> outboundComPortPools) {
        List<Long> ids = new ArrayList<>();
        for (OutboundComPortPool outboundComPortPool : outboundComPortPools) {
            ids.add(outboundComPortPool.getId());
        }
        return ids;
    }

    @Override
    protected void writeTo(OutboundComPort source,EngineModelService engineModelService) {
        super.writeTo(source, engineModelService);
        source.setNumberOfSimultaneousConnections(this.numberOfSimultaneousConnections);

        updateComPortPools(source, engineModelService);
    }

    private void updateComPortPools(OutboundComPort comPort, EngineModelService engineModelService) {
        List<OutboundComPortPool> currentOutboundPools = engineModelService.findContainingComPortPoolsForComPort(comPort);
        List<Long> currentIdList = createHasIdList(currentOutboundPools);
        for (Long outboundComPortPoolId : outboundComPortPoolIds) {
            if(!currentIdList.contains(outboundComPortPoolId)){
                engineModelService.findOutboundComPortPool(outboundComPortPoolId).addOutboundComPort(comPort);
            }
        }
        for (OutboundComPortPool oldOutboundPool : currentOutboundPools) {
            if(!outboundComPortPoolIds.contains(oldOutboundPool.getId())){
                oldOutboundPool.removeOutboundComPort(comPort);
            }
        }
    }

    @Override
    protected OutboundComPort.OutboundComPortBuilder build(OutboundComPort.OutboundComPortBuilder builder, EngineModelService engineModelService) {
        return super.build(builder.comPortType(comPortType), engineModelService);
    }

    @Override
    protected OutboundComPort createNew(ComServer comServer, EngineModelService engineModelService) {
        OutboundComPort outboundComPort = build(comServer.newOutboundComPort(this.name, this.numberOfSimultaneousConnections), engineModelService).add();
        for (Long outboundComPortPoolId : outboundComPortPoolIds) {
            OutboundComPortPool outboundComPortPool = engineModelService.findOutboundComPortPool(outboundComPortPoolId);
            if (outboundComPortPool != null) {
                outboundComPortPool.addOutboundComPort(outboundComPort);
            }
        }
        return outboundComPort;
    }

}
