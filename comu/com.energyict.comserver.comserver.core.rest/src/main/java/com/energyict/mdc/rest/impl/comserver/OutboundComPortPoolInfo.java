package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutboundComPortPoolInfo extends ComPortPoolInfo<OutboundComPortPool> {

    public OutboundComPortPoolInfo() {
        this.outboundComPorts = new ArrayList<>();
    }

    public OutboundComPortPoolInfo(OutboundComPortPool comPortPool, EngineModelService engineModelService) {
        super(comPortPool);
        Optional<List<OutboundComPort>> comPorts = Optional.fromNullable(comPortPool.getComPorts());
        if (comPorts.isPresent()) {
            this.outboundComPorts = new ArrayList<>(comPorts.get().size());
            for (OutboundComPort outboundComPort : comPorts.get()) {
                this.outboundComPorts.add(ComPortInfoFactory.asOutboundInfo(outboundComPort, engineModelService));
            }
        }
        Optional<TimeDuration> taskExecutionTimeout = Optional.fromNullable(comPortPool.getTaskExecutionTimeout());
        if (taskExecutionTimeout.isPresent()) {
            this.taskExecutionTimeout = new TimeDurationInfo(taskExecutionTimeout.get());
        }
    }

    @Override
    protected OutboundComPortPool writeTo(OutboundComPortPool source, ProtocolPluggableService protocolPluggableService) {
        super.writeTo(source, protocolPluggableService);
        Optional<TimeDurationInfo> taskExecutionTimeout = Optional.fromNullable(this.taskExecutionTimeout);
        if (taskExecutionTimeout.isPresent()) {
            source.setTaskExecutionTimeout(taskExecutionTimeout.get().asTimeDuration());
        }
        return source;
    }


    @Override
    protected OutboundComPortPool createNew(EngineModelService engineModelService) {
        return engineModelService.newOutboundComPortPool();
    }

    @Override
    protected void handlePools(OutboundComPortPool outboundComPortPool, EngineModelService engineModelService, boolean all) {
        updateComPorts(outboundComPortPool, this.outboundComPorts, engineModelService, all);
    }

    private void updateComPorts(OutboundComPortPool outboundComPortPool, List<OutboundComPortInfo> newComPorts, EngineModelService engineModelService, boolean all) {
        Map<Long, OutboundComPortInfo> newComPortIdMap = all ? getAllComPorts(engineModelService) : asIdz(newComPorts);
        for (OutboundComPort comPort : outboundComPortPool.getComPorts()) {
            if (newComPortIdMap.containsKey(comPort.getId())) {
                // Updating ComPorts not allowed here
                newComPortIdMap.remove(comPort.getId());
            } else {
                outboundComPortPool.removeOutboundComPort(comPort);
            }
        }

        for (OutboundComPortInfo comPortInfo : newComPortIdMap.values()) {
            Optional<? extends ComPort> comPort = Optional.fromNullable(engineModelService.findComPort(comPortInfo.id));
            if (!comPort.isPresent()) {
                throw new WebApplicationException("No ComPort with id "+comPortInfo.id,
                        Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id "+comPortInfo.id).build());
            }
            if (!OutboundComPort.class.isAssignableFrom(comPort.get().getClass())) {
                throw new WebApplicationException("ComPort with id "+comPortInfo.id+" should have been OutboundComPort, but was "+comPort.get().getClass().getSimpleName(),
                        Response.status(Response.Status.BAD_REQUEST).entity("ComPort with id "+comPortInfo.id+" should have been OutboundComPort, but was "+comPort.get().getClass().getSimpleName()).build());
            }

            outboundComPortPool.addOutboundComPort((OutboundComPort) comPort.get());
        }
    }

    private Map<Long, OutboundComPortInfo> getAllComPorts(EngineModelService engineModelService) {
        Map<Long, OutboundComPortInfo> map = new HashMap<>();
        for (OutboundComPort comPort : engineModelService.findAllOutboundComPorts()) {
            map.put(comPort.getId(), ComPortInfoFactory.asOutboundInfo(comPort, engineModelService));
        }
        return map;
    }

    private Map<Long, OutboundComPortInfo> asIdz(Collection<? extends OutboundComPortInfo> comPortInfos) {
        Map<Long, OutboundComPortInfo> comPortIdMap = new HashMap<>();
        for (OutboundComPortInfo comPort : comPortInfos) {
            comPortIdMap.put(comPort.id, comPort);
        }
        return comPortIdMap;
    }

}
