package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import java.util.Optional;

import java.util.ArrayList;
import java.util.List;


public abstract class OutboundComPortInfo extends ComPortInfo<OutboundComPort, OutboundComPort.OutboundComPortBuilder> {

    public List<Long> outboundComPortPoolIds = new ArrayList<>();;

    public OutboundComPortInfo() {
        this.direction = "outbound";
        this.comPortType = ComPortType.TCP;
    }

    public OutboundComPortInfo(OutboundComPort comPort, EngineConfigurationService engineConfigurationService) {
        super(comPort);
        this.direction = "outbound";
        List<OutboundComPortPool> outboundComPortPools = engineConfigurationService.findContainingComPortPoolsForComPort(comPort);
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
    protected void writeTo(OutboundComPort source,EngineConfigurationService engineConfigurationService) {
        super.writeTo(source, engineConfigurationService);
        updateComPortPools(source, engineConfigurationService);
    }

    private void updateComPortPools(OutboundComPort comPort, EngineConfigurationService engineConfigurationService) {
        List<OutboundComPortPool> currentOutboundPools = engineConfigurationService.findContainingComPortPoolsForComPort(comPort);
        List<Long> currentIdList = createHasIdList(currentOutboundPools);
        for (Long outboundComPortPoolId : outboundComPortPoolIds) {
            if (!currentIdList.contains(outboundComPortPoolId)) {
                engineConfigurationService
                        .findOutboundComPortPool(outboundComPortPoolId)
                        .ifPresent(cpp -> cpp.addOutboundComPort(comPort));
            }
        }
        for (OutboundComPortPool oldOutboundPool : currentOutboundPools) {
            if(!outboundComPortPoolIds.contains(oldOutboundPool.getId())){
                oldOutboundPool.removeOutboundComPort(comPort);
            }
        }
    }

    @Override
    protected OutboundComPort.OutboundComPortBuilder build(OutboundComPort.OutboundComPortBuilder builder, EngineConfigurationService engineConfigurationService) {
        return super.build(builder.comPortType(comPortType), engineConfigurationService);
    }

    @Override
    protected OutboundComPort createNew(ComServer comServer, EngineConfigurationService engineConfigurationService) {
        OutboundComPort outboundComPort = build(comServer.newOutboundComPort(this.name, this.numberOfSimultaneousConnections), engineConfigurationService).add();
        for (Long outboundComPortPoolId : outboundComPortPoolIds) {
            Optional<OutboundComPortPool> outboundComPortPool = engineConfigurationService.findOutboundComPortPool(outboundComPortPoolId);
            if (outboundComPortPool.isPresent()) {
                outboundComPortPool.get().addOutboundComPort(outboundComPort);
            }
        }
        return outboundComPort;
    }

}
