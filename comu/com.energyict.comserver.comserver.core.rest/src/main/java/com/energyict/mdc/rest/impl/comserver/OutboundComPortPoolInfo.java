package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.rest.impl.TimeDurationInfo;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutboundComPortPoolInfo extends ComPortPoolInfo<OutboundComPortPool> {

    public OutboundComPortPoolInfo() {
    }

    public OutboundComPortPoolInfo(OutboundComPortPool comPortPool) {
        super(comPortPool);
        if (comPortPool.getComPorts()!=null) {
            outboundComPorts = new ArrayList<>(comPortPool.getComPorts().size());
            for (OutboundComPort outboundComPort : comPortPool.getComPorts()) {
                outboundComPorts.add(ComPortInfoFactory.asOutboundInfo(outboundComPort));
            }
        }
        if (comPortPool.getTaskExecutionTimeout()!=null) {
            taskExecutionTimeout = new TimeDurationInfo(comPortPool.getTaskExecutionTimeout());
        }
    }

    @Override
    protected OutboundComPortPool writeTo(OutboundComPortPool source, EngineModelService engineModelService) {
        super.writeTo(source, engineModelService);
        if (this.taskExecutionTimeout!=null) {
            source.setTaskExecutionTimeout(this.taskExecutionTimeout.asTimeDuration());
        }
        return source;
    }


    @Override
    protected OutboundComPortPool createNew(EngineModelService engineModelService) {
        return engineModelService.newOutboundComPortPool();
    }

    @Override
    protected void handlePools(OutboundComPortPool outboundComPortPool, EngineModelService engineModelService) {
        updateComPorts(outboundComPortPool, this.outboundComPorts, engineModelService);
    }

    private void updateComPorts(OutboundComPortPool outboundComPortPool, List<OutboundComPortInfo> newComPorts, EngineModelService engineModelService) {
        Map<Long, ComPortInfo> newComPortIdMap = asIdz(newComPorts);
        for (OutboundComPort comPort : outboundComPortPool.getComPorts()) {
            if (newComPortIdMap.containsKey(comPort.getId())) {
                // Updating ComPorts not allowed here
                newComPortIdMap.remove(comPort.getId());
            } else {
                outboundComPortPool.removeOutboundComPort(comPort);
            }
        }

        for (ComPortInfo comPortInfo : newComPortIdMap.values()) {
            ComPort comPort = engineModelService.findComPort(comPortInfo.id);
            if (comPort == null) {
                throw new WebApplicationException("No ComPort with id "+comPortInfo.id, Response.Status.NOT_FOUND);
            }
            if (!OutboundComPort.class.isAssignableFrom(comPort.getClass())) {
                throw new WebApplicationException("ComPort with id "+comPortInfo.id+" should have been OutboundComPort, but was "+comPort.getClass().getSimpleName(), Response.Status.BAD_REQUEST);
            }

            outboundComPortPool.addOutboundComPort((OutboundComPort) comPort);
        }
    }

    private Map<Long, ComPortInfo> asIdz(Collection<? extends ComPortInfo> comPortInfos) {
        Map<Long, ComPortInfo> comPortIdMap = new HashMap<>();
        for (ComPortInfo comPort : comPortInfos) {
            comPortIdMap.put(comPort.id, comPort);
        }
        return comPortIdMap;
    }

}
