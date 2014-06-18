package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
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
    public ConnectionTask<?,?> createTask(DeviceDataService deviceDataService, EngineModelService engineModelService, Device device, MdcPropertyUtils mdcPropertyUtils) {
        PartialConnectionTask partialConnectionTask = findMyPartialConnectionTask(device);
        if (partialConnectionTask==null) {
            throw new WebApplicationException("No such partial connection task", Response.Status.BAD_REQUEST);
        }
        if (!PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            throw new WebApplicationException("Expected partial connection task to be 'Outbound'", Response.Status.BAD_REQUEST);
        }
        OutboundComPortPool outboundComPortPool=null;
        if (!Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace()) {
            outboundComPortPool=(OutboundComPortPool) engineModelService.findComPortPool(this.comPortPool);
        }

        PartialScheduledConnectionTask partialScheduledConnectionTask = (PartialScheduledConnectionTask) partialConnectionTask;
        ScheduledConnectionTask scheduledConnectionTask=null;
        switch (this.connectionStrategy) {
            case AS_SOON_AS_POSSIBLE:
                scheduledConnectionTask = deviceDataService.newAsapConnectionTask(device, partialScheduledConnectionTask, outboundComPortPool);
                break;
            case MINIMIZE_CONNECTIONS:
                scheduledConnectionTask = deviceDataService.newMinimizeConnectionTask(device, partialScheduledConnectionTask, outboundComPortPool,
                        this.temporalExpression!=null?temporalExpression.asTemporalExpression():null);
                break;
        }
        scheduledConnectionTask.setSimultaneousConnectionsAllowed(this.allowSimultaneousConnections);
        scheduledConnectionTask.setCommunicationWindow(new ComWindow(this.comWindowStart, this.comWindowEnd));
        scheduledConnectionTask.setConnectionStrategy(this.connectionStrategy);
        if (this.properties !=null) {
            for (PropertySpec<?> propertySpec : partialConnectionTask.getPluggableClass().getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
                if (propertyValue!=null) {
                    scheduledConnectionTask.setProperty(propertySpec.getName(), propertyValue);
                }
            }
        }

        scheduledConnectionTask.save();
        if (this.paused) {
            scheduledConnectionTask.pause();
        } else {
            scheduledConnectionTask.resume();
        }

        return scheduledConnectionTask;
    }

}
