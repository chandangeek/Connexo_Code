package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import javax.ws.rs.core.UriInfo;

public class ScheduledConnectionMethodInfo extends ConnectionMethodInfo<PartialScheduledConnectionTask> {

    public ScheduledConnectionMethodInfo() {
    }

    public ScheduledConnectionMethodInfo(PartialScheduledConnectionTask partialConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        super(partialConnectionTask, uriInfo, mdcPropertyUtils);
        this.connectionStrategy=partialConnectionTask.getConnectionStrategy();
        this.allowSimultaneousConnections=partialConnectionTask.isSimultaneousConnectionsAllowed();
        this.rescheduleRetryDelay = partialConnectionTask.getRescheduleDelay()!=null?new TimeDurationInfo(partialConnectionTask.getRescheduleDelay()):null;
        if (partialConnectionTask.getCommunicationWindow()!=null) {
            this.comWindowStart=partialConnectionTask.getCommunicationWindow().getStart().getMillis()/1000;
            this.comWindowEnd=partialConnectionTask.getCommunicationWindow().getEnd().getMillis()/1000;
        } else {
            this.comWindowStart=0;
            this.comWindowEnd=0;
        }
        this.nextExecutionSpecs =partialConnectionTask.getNextExecutionSpecs()!=null?
                TemporalExpressionInfo.from(partialConnectionTask.getNextExecutionSpecs().getTemporalExpression()):null;
    }

    @Override
    protected void writeTo(PartialScheduledConnectionTask partialConnectionTask, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        super.writeTo(partialConnectionTask, engineModelService, protocolPluggableService);
        partialConnectionTask.setDefault(this.isDefault);
        partialConnectionTask.setAllowSimultaneousConnections(this.allowSimultaneousConnections);
        if (this.comWindowEnd!=null && this.comWindowStart!=null) {
            partialConnectionTask.setComWindow(new ComWindow(this.comWindowStart, this.comWindowEnd));
        } else {
            partialConnectionTask.setComWindow(null);
        }
        partialConnectionTask.setConnectionStrategy(this.connectionStrategy);
        partialConnectionTask.setComportPool(Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace() ? null : (OutboundComPortPool) engineModelService.findComPortPool(this.comPortPool));
        partialConnectionTask.setRescheduleRetryDelay(this.rescheduleRetryDelay!=null?this.rescheduleRetryDelay.asTimeDuration():null);
        if (nextExecutionSpecs !=null) {
            partialConnectionTask.setTemporalExpression(nextExecutionSpecs.asTemporalExpression());
        } else {
            partialConnectionTask.setNextExecutionSpecs(null);
        }
    }

    @Override
    public PartialConnectionTask createPartialTask(DeviceConfiguration deviceConfiguration, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        ConnectionTypePluggableClass connectionTypePluggableClass = findConnectionTypeOrThrowException(this.connectionTypePluggableClass, protocolPluggableService);
        TimeDuration rescheduleDelay = this.rescheduleRetryDelay == null ? null : this.rescheduleRetryDelay.asTimeDuration();
        PartialScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = deviceConfiguration.newPartialScheduledConnectionTask(this.name, connectionTypePluggableClass, rescheduleDelay, this.connectionStrategy)
            .comPortPool((OutboundComPortPool) engineModelService.findComPortPool(this.comPortPool))
            .asDefault(this.isDefault)
            .allowSimultaneousConnections(this.allowSimultaneousConnections);
        if (this.nextExecutionSpecs !=null) {
            if (this.nextExecutionSpecs.offset==null) {
                scheduledConnectionTaskBuilder
                        .nextExecutionSpec()
                        .temporalExpression(this.nextExecutionSpecs.every.asTimeDuration())
                        .set();
            } else {
                scheduledConnectionTaskBuilder
                        .nextExecutionSpec()
                        .temporalExpression(this.nextExecutionSpecs.every.asTimeDuration(), this.nextExecutionSpecs.offset.asTimeDuration())
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
