package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Checks;

import javax.ws.rs.core.UriInfo;

public class ScheduledConnectionMethodInfo extends ConnectionMethodInfo<PartialScheduledConnectionTask> {

    public ScheduledConnectionMethodInfo() {
    }

    public ScheduledConnectionMethodInfo(PartialScheduledConnectionTask partialConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        super(partialConnectionTask, uriInfo, mdcPropertyUtils);
        if (partialConnectionTask.getConnectionStrategy() != null) {
            this.connectionStrategy = partialConnectionTask.getConnectionStrategy().name();
        }
        this.allowSimultaneousConnections = partialConnectionTask.isSimultaneousConnectionsAllowed();
        this.rescheduleRetryDelay = TimeDurationInfo.of(partialConnectionTask.getRescheduleDelay());
        if (partialConnectionTask.getCommunicationWindow()!=null) {
            this.comWindowStart=partialConnectionTask.getCommunicationWindow().getStart().getMillis()/1000;
            this.comWindowEnd=partialConnectionTask.getCommunicationWindow().getEnd().getMillis()/1000;
        } else {
            this.comWindowStart=0;
            this.comWindowEnd=0;
        }
        this.temporalExpression = partialConnectionTask.getNextExecutionSpecs()!=null?
                TemporalExpressionInfo.from(partialConnectionTask.getNextExecutionSpecs().getTemporalExpression()):null;
    }

    @Override
    protected void writeTo(PartialScheduledConnectionTask partialConnectionTask, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService) {
        super.writeTo(partialConnectionTask, engineConfigurationService, protocolPluggableService);
        partialConnectionTask.setDefault(this.isDefault);
        partialConnectionTask.setAllowSimultaneousConnections(this.allowSimultaneousConnections);
        if (this.comWindowEnd!=null && this.comWindowStart!=null) {
            partialConnectionTask.setComWindow(new ComWindow(this.comWindowStart, this.comWindowEnd));
        } else {
            partialConnectionTask.setComWindow(null);
        }
        if (!Checks.is(this.connectionStrategy).emptyOrOnlyWhiteSpace()) {
            partialConnectionTask.setConnectionStrategy(ConnectionStrategy.valueOf(this.connectionStrategy));
        }
        if (!Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace()) {
            engineConfigurationService.findOutboundComPortPoolByName(this.comPortPool).ifPresent(partialConnectionTask::setComportPool);
        } else {
            partialConnectionTask.setComportPool(null);
        }
        partialConnectionTask.setRescheduleRetryDelay(this.rescheduleRetryDelay!=null?this.rescheduleRetryDelay.asTimeDuration():null);
        if (temporalExpression !=null) {
            partialConnectionTask.setTemporalExpression(temporalExpression.asTemporalExpression());
        } else {
            partialConnectionTask.setNextExecutionSpecs(null);
        }
    }

    @Override
    public PartialConnectionTask createPartialTask(DeviceConfiguration deviceConfiguration, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        ConnectionTypePluggableClass connectionTypePluggableClass = findConnectionTypeOrThrowException(this.connectionTypePluggableClass, protocolPluggableService);
        TimeDuration rescheduleDelay = this.rescheduleRetryDelay == null ? null : this.rescheduleRetryDelay.asTimeDuration();
        PartialScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder =
                deviceConfiguration
                        .newPartialScheduledConnectionTask(this.name, connectionTypePluggableClass, rescheduleDelay, ConnectionStrategy.valueOf(this.connectionStrategy))
                        .comPortPool(engineConfigurationService.findOutboundComPortPoolByName(this.comPortPool).orElse(null))
                        .asDefault(this.isDefault)
                        .allowSimultaneousConnections(this.allowSimultaneousConnections);
        if (this.temporalExpression !=null) {
            if (this.temporalExpression.offset==null) {
                scheduledConnectionTaskBuilder
                        .nextExecutionSpec()
                        .temporalExpression(this.temporalExpression.every.asTimeDuration())
                        .set();
            } else {
                scheduledConnectionTaskBuilder
                        .nextExecutionSpec()
                        .temporalExpression(this.temporalExpression.every.asTimeDuration(), this.temporalExpression.offset.asTimeDuration())
                        .set();
            }
        }
        if(this.comWindowStart!=null && this.comWindowEnd!=null){
            scheduledConnectionTaskBuilder.comWindow(new ComWindow(this.comWindowStart, this.comWindowEnd));
        }

        addPropertiesToPartialConnectionTask(scheduledConnectionTaskBuilder, connectionTypePluggableClass);
        return scheduledConnectionTaskBuilder.build();
    }

}
