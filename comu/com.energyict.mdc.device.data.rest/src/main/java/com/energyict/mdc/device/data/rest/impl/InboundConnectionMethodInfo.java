package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class InboundConnectionMethodInfo extends ConnectionMethodInfo<InboundConnectionTask> {

    public InboundConnectionMethodInfo() {
    }

    public InboundConnectionMethodInfo(InboundConnectionTask partialInboundConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        super(partialInboundConnectionTask, uriInfo, mdcPropertyUtils);
    }

    @Override
    protected void writeTo(InboundConnectionTask connectionTask, EngineModelService engineModelService) {
        super.writeTo(connectionTask, engineModelService);
        connectionTask.setComPortPool(Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace() ? null : (InboundComPortPool) engineModelService.findComPortPool(this.comPortPool));
    }

    @Override
    public ConnectionTask<?,?> createTask(EngineModelService engineModelService, Device device, MdcPropertyUtils mdcPropertyUtils) {
        PartialConnectionTask partialConnectionTask = findMyPartialConnectionTask(device);
        if (partialConnectionTask==null) {
            throw new WebApplicationException("No such partial connection task", Response.Status.BAD_REQUEST);
        }
        if (!PartialInboundConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            throw new WebApplicationException("Expected partial connection task to be 'Inbound'", Response.Status.BAD_REQUEST);
        }
        Device.InboundConnectionTaskBuilder builder = device.getInboundConnectionTaskBuilder((PartialInboundConnectionTask) partialConnectionTask);
        if (!Checks.is(comPortPool).emptyOrOnlyWhiteSpace()) {
            builder.setComPortPool((InboundComPortPool) engineModelService.findComPortPool(this.comPortPool));
        }
        if (this.properties !=null) {
            for (PropertySpec<?> propertySpec : partialConnectionTask.getPluggableClass().getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
                if (propertyValue!=null) {
                    builder.setProperty(propertySpec.getName(), propertyValue);
                }
            }
        }

        return builder.add();
    }

}
