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

}
