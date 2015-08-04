package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.ArrayList;
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
        TimeDuration taskExecutionTimeout;
        if (this.taskExecutionTimeout==null || this.taskExecutionTimeout.count==0) {
            taskExecutionTimeout = new TimeDuration(6, TimeDuration.TimeUnit.HOURS);
        } else {
            taskExecutionTimeout = this.taskExecutionTimeout.asTimeDuration();
        }

        OutboundComPortPool outboundComPortPool = engineConfigurationService.newOutboundComPortPool(this.name, this.comPortType, taskExecutionTimeout);
        this.writeTo(outboundComPortPool);
        return outboundComPortPool;
    }

}