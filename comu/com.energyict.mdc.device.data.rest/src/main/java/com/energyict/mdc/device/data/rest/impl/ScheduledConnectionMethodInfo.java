package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import javax.ws.rs.core.UriInfo;

public class ScheduledConnectionMethodInfo extends ConnectionMethodInfo<ScheduledConnectionTask> {

    public ScheduledConnectionMethodInfo() {
    }

    public ScheduledConnectionMethodInfo(ScheduledConnectionTask partialConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        super(partialConnectionTask, uriInfo, mdcPropertyUtils);
        this.connectionStrategy=partialConnectionTask.getConnectionStrategy();
        this.allowSimultaneousConnections=partialConnectionTask.isSimultaneousConnectionsAllowed();
        this.rescheduleRetryDelay = partialConnectionTask.getRescheduleDelay()!=null?new TimeDurationInfo(partialConnectionTask.getRescheduleDelay()):null;
        if (partialConnectionTask.getCommunicationWindow()!=null) {
            this.comWindowStart=partialConnectionTask.getCommunicationWindow().getStart().getMillis()/1000;
            this.comWindowEnd=partialConnectionTask.getCommunicationWindow().getEnd().getMillis()/1000;
        }
        this.temporalExpression=partialConnectionTask.getNextExecutionSpecs()!=null?
                TemporalExpressionInfo.from(partialConnectionTask.getNextExecutionSpecs().getTemporalExpression()):null;
    }

    @Override
    protected void writeTo(ScheduledConnectionTask connectionTask, EngineModelService engineModelService) {
        super.writeTo(connectionTask, engineModelService);
//        connectionTask.isDefault()setDefault(this.isDefault);
//        connectionTask.setAllowSimultaneousConnections(this.allowSimultaneousConnections);
//        connectionTask.setComWindow(new ComWindow(this.comWindowStart, this.comWindowEnd));
//        connectionTask.setConnectionStrategy(this.connectionStrategy);
//        connectionTask.setComportPool(Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace() ? null : (OutboundComPortPool) engineModelService.findComPortPool(this.comPortPool));
//        connectionTask.setRescheduleRetryDelay(this.rescheduleRetryDelay != null ? this.rescheduleRetryDelay.asTimeDuration() : null);
//        if (temporalExpression!=null) {
//            connectionTask.setTemporalExpression(temporalExpression.asTemporalExpression());
//        } else {
//            connectionTask.setNextExecutionSpecs(null);
//        }
    }

    @Override
    public ConnectionTask<?,?> createTask(DeviceConfiguration deviceConfiguration, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils) {
//        this.mdcPropertyUtils = mdcPropertyUtils;
//        ConnectionTypePluggableClass connectionTypePluggableClass = findConnectionTypeOrThrowException(this.connectionType, protocolPluggableService);
//        TimeDuration rescheduleDelay = this.rescheduleRetryDelay == null ? null : this.rescheduleRetryDelay.asTimeDuration();
//        PartialScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = deviceConfiguration.newPartialScheduledConnectionTask(this.name, connectionTypePluggableClass, rescheduleDelay, this.connectionStrategy)
//            .comPortPool((OutboundComPortPool) engineModelService.findComPortPool(this.comPortPool))
//            .comWindow(new ComWindow(this.comWindowStart, this.comWindowEnd))
//            .asDefault(this.isDefault)
//            .allowSimultaneousConnections(this.allowSimultaneousConnections);
//        if (this.temporalExpression!=null) {
//            if (this.temporalExpression.offset==null) {
//                scheduledConnectionTaskBuilder
//                        .nextExecutionSpec()
//                        .temporalExpression(this.temporalExpression.every.asTimeDuration())
//                        .set();
//            } else {
//                scheduledConnectionTaskBuilder
//                        .nextExecutionSpec()
//                        .temporalExpression(this.temporalExpression.every.asTimeDuration(), this.temporalExpression.offset.asTimeDuration())
//                        .set();
//            }
//        }
//
//        addPropertiesToPartialConnectionTask(scheduledConnectionTaskBuilder, connectionTypePluggableClass);
//        return scheduledConnectionTaskBuilder.build();
        return null;
    }

}
