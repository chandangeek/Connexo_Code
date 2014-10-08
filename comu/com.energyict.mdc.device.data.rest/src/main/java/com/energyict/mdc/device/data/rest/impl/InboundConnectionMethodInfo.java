package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
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
    protected void writeTo(InboundConnectionTask connectionTask, PartialConnectionTask partialConnectionTask, EngineModelService engineModelService, MdcPropertyUtils mdcPropertyUtils) {
        super.writeTo(connectionTask, partialConnectionTask, engineModelService, mdcPropertyUtils);
        connectionTask.setComPortPool(Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace() ? null : (InboundComPortPool) engineModelService.findComPortPool(this.comPortPool));
    }

    @Override
    public ConnectionTask<?, ?> createTask(EngineModelService engineModelService, Device device, MdcPropertyUtils mdcPropertyUtils, PartialConnectionTask partialConnectionTask) {
        if (!PartialInboundConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            throw new WebApplicationException("Expected partial connection task to be 'Inbound'", Response.Status.BAD_REQUEST);
        }
        PartialInboundConnectionTask partialInboundConnectionTask = (PartialInboundConnectionTask) partialConnectionTask;

        InboundComPortPool inboundComPortPool = null;
        if (!Checks.is(comPortPool).emptyOrOnlyWhiteSpace()) {
            inboundComPortPool = (InboundComPortPool) engineModelService.findComPortPool(this.comPortPool);
        }
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask);
        inboundConnectionTaskBuilder.setComPortPool(inboundComPortPool);
        inboundConnectionTaskBuilder.setConnectionTaskLifecycleStatus(this.status);
        return inboundConnectionTaskBuilder.add();
    }

}
