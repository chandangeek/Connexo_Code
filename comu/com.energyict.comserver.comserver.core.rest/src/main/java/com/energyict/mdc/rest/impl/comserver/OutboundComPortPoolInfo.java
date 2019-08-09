/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class OutboundComPortPoolInfo extends ComPortPoolInfo<OutboundComPortPool> {

    public OutboundComPortPoolInfo() {
        this.outboundComPorts = new ArrayList<>();
    }

    public OutboundComPortPoolInfo(OutboundComPortPool comPortPool, EngineConfigurationService engineConfigurationService, ComPortInfoFactory comPortInfoFactory) {
        super(comPortPool);
        this.outboundComPorts = comPortPool.getComPorts()
                .stream()
                .map(outboundComPort -> comPortInfoFactory.asOutboundInfo(outboundComPort, engineConfigurationService))
                .collect(Collectors.toList());

        this.maxPriorityConnections = calculateMaxPriorityConnections(comPortPool, engineConfigurationService, comPortPool.getPctHighPrioTasks());
        this.taskExecutionTimeout = TimeDurationInfo.of(comPortPool.getTaskExecutionTimeout());
    }

    @Override
    protected OutboundComPortPool createNew(EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils) {
        TimeDuration taskExecutionTimeout;
        if (this.taskExecutionTimeout==null || this.taskExecutionTimeout.count==0) {
            taskExecutionTimeout = new TimeDuration(6, TimeDuration.TimeUnit.HOURS);
        } else {
            taskExecutionTimeout = this.taskExecutionTimeout.asTimeDuration();
        }

        OutboundComPortPool outboundComPortPool = engineConfigurationService.newOutboundComPortPool(this.name, this.comPortType != null ? this.comPortType.id : null, taskExecutionTimeout, this.pctHighPrioTasks);
        this.writeTo(outboundComPortPool);
        return outboundComPortPool;
    }


    protected long calculateMaxPriorityConnections(ComPortPool comPortPool, EngineConfigurationService engineConfigurationService, long pctHighPrioTasks) {
        float tempMaxPriorityConnections = 0;
        for (ComPort comPort : comPortPool.getComPorts()) {
            long numberOfPortPoolsUsedByThePort = getNumberOfPortPoolsUsedByThePort(comPort, engineConfigurationService);
            if(numberOfPortPoolsUsedByThePort != 0)
                tempMaxPriorityConnections += (float)comPort.getNumberOfSimultaneousConnections()/numberOfPortPoolsUsedByThePort;
        }
        return (long)Math.ceil(tempMaxPriorityConnections * ((float)pctHighPrioTasks/100));
    }

    protected long getNumberOfPortPoolsUsedByThePort(ComPort comPort, EngineConfigurationService engineConfigurationService) {
        long comPortInUseByPortPools = 0;
        for (ComPortPool comPortPool:engineConfigurationService.findAllComPortPools())
            comPortInUseByPortPools += comPortPool.getComPorts().stream().filter(temp->temp.getId() == comPort.getId()).count();

        return comPortInUseByPortPools;
    }

}
