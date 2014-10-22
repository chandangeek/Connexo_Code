package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class OutboundComPortPoolInfo extends ComPortPoolInfo<OutboundComPortPool> {

    public OutboundComPortPoolInfo() {
        this.outboundComPorts = new ArrayList<>();
    }

    public OutboundComPortPoolInfo(OutboundComPortPool comPortPool, EngineModelService engineModelService) {
        super(comPortPool);
        this.outboundComPorts = comPortPool.getComPorts()
                .stream()
                .map(outboundComPort -> ComPortInfoFactory.asOutboundInfo(outboundComPort, engineModelService))
                .collect(Collectors.toList());
        this.taskExecutionTimeout = TimeDurationInfo.of(comPortPool.getTaskExecutionTimeout());
    }

    @Override
    protected OutboundComPortPool writeTo(OutboundComPortPool source, ProtocolPluggableService protocolPluggableService) {
        super.writeTo(source, protocolPluggableService);
        source.setTaskExecutionTimeout(
                Optional.ofNullable(this.taskExecutionTimeout)
                    .map(TimeDurationInfo::asTimeDuration)
                    .orElse(null));
        return source;
    }

    @Override
    protected OutboundComPortPool createNew(EngineModelService engineModelService) {
        return engineModelService.newOutboundComPortPool();
    }

}
