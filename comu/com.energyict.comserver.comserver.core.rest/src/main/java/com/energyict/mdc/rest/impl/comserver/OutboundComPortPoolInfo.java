package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.time.TimeDuration;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class OutboundComPortPoolInfo extends ComPortPoolInfo<OutboundComPortPool> {

    public OutboundComPortPoolInfo() {
        this.outboundComPorts = new ArrayList<>();
    }

    public OutboundComPortPoolInfo(OutboundComPortPool comPortPool, EngineConfigurationService engineConfigurationService) {
        super(comPortPool);
        this.outboundComPorts = comPortPool.getComPorts()
                .stream()
                .map(outboundComPort -> ComPortInfoFactory.asOutboundInfo(outboundComPort, engineConfigurationService))
                .collect(Collectors.toList());
        this.taskExecutionTimeout = TimeDurationInfo.of(comPortPool.getTaskExecutionTimeout());
    }

    @Override
    protected OutboundComPortPool createNew(EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService) {
        TimeDuration taskExecutionTimeout = Optional.ofNullable(this.taskExecutionTimeout).map(TimeDurationInfo::asTimeDuration).orElse(null);
        OutboundComPortPool outboundComPortPool = engineConfigurationService.newOutboundComPortPool(this.name, this.type, taskExecutionTimeout);
        this.writeTo(outboundComPortPool);
        return outboundComPortPool;
    }

}