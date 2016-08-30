package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.elster.jupiter.util.Checks.is;

public class ScheduledConnectionMethodInfo extends ConnectionMethodInfo<ScheduledConnectionTask> {

    public ScheduledConnectionMethodInfo() {
    }

    public ScheduledConnectionMethodInfo(ScheduledConnectionTask scheduledConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        super(scheduledConnectionTask, uriInfo, mdcPropertyUtils);
        this.connectionStrategy = scheduledConnectionTask.getConnectionStrategy().name();
        this.allowSimultaneousConnections = scheduledConnectionTask.isSimultaneousConnectionsAllowed();
        this.rescheduleRetryDelay = TimeDurationInfo.of(scheduledConnectionTask.getRescheduleDelay());
        if (scheduledConnectionTask.getCommunicationWindow() != null) {
            this.comWindowStart = scheduledConnectionTask.getCommunicationWindow().getStart().getMillis() / 1000;
            this.comWindowEnd = scheduledConnectionTask.getCommunicationWindow().getEnd().getMillis() / 1000;
        }
        this.nextExecutionSpecs = scheduledConnectionTask.getNextExecutionSpecs() != null ?
                TemporalExpressionInfo.from(scheduledConnectionTask.getNextExecutionSpecs().getTemporalExpression()) : null;
    }

    @Override
    protected void writeTo(ScheduledConnectionTask scheduledConnectionTask, PartialConnectionTask partialConnectionTask, EngineConfigurationService engineConfigurationService, MdcPropertyUtils mdcPropertyUtils) {
        super.writeTo(scheduledConnectionTask, partialConnectionTask, engineConfigurationService, mdcPropertyUtils);
        writeCommonFields(scheduledConnectionTask, engineConfigurationService);
        scheduledConnectionTask.setConnectionStrategy(getConnectionStrategy());
        try {
            scheduledConnectionTask.setNextExecutionSpecsFrom(this.nextExecutionSpecs != null ? nextExecutionSpecs.asTemporalExpression() : null);
        } catch (LocalizedFieldValidationException e) {
            throw e.fromSubField("nextExecutionSpecs");
        }
    }

    private void writeCommonFields(ScheduledConnectionTask scheduledConnectionTask, EngineConfigurationService engineConfigurationService) {
        scheduledConnectionTask.setSimultaneousConnectionsAllowed(this.allowSimultaneousConnections);
        if (this.comWindowEnd != null && this.comWindowStart != null) {
            scheduledConnectionTask.setCommunicationWindow(new ComWindow(this.comWindowStart, this.comWindowEnd));
        }
        if (!is(this.comPortPool).emptyOrOnlyWhiteSpace()) {
            scheduledConnectionTask.setComPortPool(engineConfigurationService.findOutboundComPortPoolByName(this.comPortPool).orElse(null));
        } else {
            scheduledConnectionTask.setComPortPool(null);
        }
    }

    @Override
    public ConnectionTask<?, ?> createTask(EngineConfigurationService engineConfigurationService, Device device, MdcPropertyUtils mdcPropertyUtils, PartialConnectionTask partialConnectionTask) {
        if (!PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            throw new WebApplicationException("Expected partial connection task to be 'Outbound'", Response.Status.BAD_REQUEST);
        }

        PartialScheduledConnectionTask partialScheduledConnectionTask = (PartialScheduledConnectionTask) partialConnectionTask;
        Device.ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask);
        if (!is(this.comPortPool).emptyOrOnlyWhiteSpace()) {
            engineConfigurationService
                    .findOutboundComPortPoolByName(this.comPortPool)
                    .ifPresent(scheduledConnectionTaskBuilder::setComPortPool);
        }
        scheduledConnectionTaskBuilder.setConnectionStrategy(getConnectionStrategy());
        scheduledConnectionTaskBuilder.setNextExecutionSpecsFrom(this.nextExecutionSpecs != null ? nextExecutionSpecs.asTemporalExpression() : null);
        scheduledConnectionTaskBuilder.setConnectionTaskLifecycleStatus(this.status);
        scheduledConnectionTaskBuilder.setSimultaneousConnectionsAllowed(this.allowSimultaneousConnections);
        if (this.comWindowEnd != null && this.comWindowStart != null) {
            scheduledConnectionTaskBuilder.setCommunicationWindow(new ComWindow(this.comWindowStart, this.comWindowEnd));
        }

        //--- adding properties
        if (this.properties != null) {
            for (PropertySpec propertySpec : partialConnectionTask.getPluggableClass().getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
                if (propertyValue != null || !hasInheritedPropertyValue(partialConnectionTask, propertySpec)) { // really should start using propertyHasValue!!!!!!!
                    scheduledConnectionTaskBuilder.setProperty(propertySpec.getName(), propertyValue);
                } // no 'else': we're creating a connection task: no existing property to remove!
            }
        }
        return scheduledConnectionTaskBuilder.add();
    }
}
