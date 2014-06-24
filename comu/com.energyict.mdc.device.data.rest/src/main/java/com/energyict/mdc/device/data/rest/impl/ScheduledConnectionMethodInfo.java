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

    public ScheduledConnectionMethodInfo(ScheduledConnectionTask scheduledConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        super(scheduledConnectionTask, uriInfo, mdcPropertyUtils);
        this.connectionStrategy=scheduledConnectionTask.getConnectionStrategy();
        this.allowSimultaneousConnections=scheduledConnectionTask.isSimultaneousConnectionsAllowed();
        this.rescheduleRetryDelay = scheduledConnectionTask.getRescheduleDelay()!=null?new TimeDurationInfo(scheduledConnectionTask.getRescheduleDelay()):null;
        if (scheduledConnectionTask.getCommunicationWindow()!=null) {
            this.comWindowStart=scheduledConnectionTask.getCommunicationWindow().getStart().getMillis()/1000;
            this.comWindowEnd=scheduledConnectionTask.getCommunicationWindow().getEnd().getMillis()/1000;
        }
        this.nextExecutionSpecs =scheduledConnectionTask.getNextExecutionSpecs()!=null?
                TemporalExpressionInfo.from(scheduledConnectionTask.getNextExecutionSpecs().getTemporalExpression()):null;
    }

    @Override
    protected void writeTo(ScheduledConnectionTask scheduledConnectionTask, PartialConnectionTask partialConnectionTask, DeviceDataService deviceDataService, EngineModelService engineModelService, MdcPropertyUtils mdcPropertyUtils) {
        super.writeTo(scheduledConnectionTask, partialConnectionTask, deviceDataService, engineModelService, mdcPropertyUtils);
        writeCommonFields(scheduledConnectionTask, engineModelService);
        scheduledConnectionTask.setConnectionStrategy(this.connectionStrategy);
        scheduledConnectionTask.setNextExecutionSpecsFrom(this.nextExecutionSpecs != null ? nextExecutionSpecs.asTemporalExpression() : null); // This method is called separately here because
    }

    private void writeCommonFields(ScheduledConnectionTask scheduledConnectionTask, EngineModelService engineModelService) {
        scheduledConnectionTask.setSimultaneousConnectionsAllowed(this.allowSimultaneousConnections);
        if (this.comWindowEnd!=null && this.comWindowStart!=null) {
            scheduledConnectionTask.setCommunicationWindow(new ComWindow(this.comWindowStart, this.comWindowEnd));
        }
        if (!Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace()) {
            scheduledConnectionTask.setComPortPool((OutboundComPortPool) engineModelService.findComPortPool(this.comPortPool));
        } else {
            scheduledConnectionTask.setComPortPool(null);
        }
    }

    @Override
    public ConnectionTask<?,?> createTask(DeviceDataService deviceDataService, EngineModelService engineModelService, Device device, MdcPropertyUtils mdcPropertyUtils, PartialConnectionTask partialConnectionTask) {

        if (!PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            throw new WebApplicationException("Expected partial connection task to be 'Outbound'", Response.Status.BAD_REQUEST);
        }        OutboundComPortPool outboundComPortPool=null;
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
                        this.nextExecutionSpecs != null ? nextExecutionSpecs.asTemporalExpression() : null);
                break;
        }
        writeCommonFields(scheduledConnectionTask, engineModelService);
        scheduledConnectionTask.save();

        return scheduledConnectionTask;
    }

}
