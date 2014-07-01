package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
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
    protected void writeTo(InboundConnectionTask connectionTask, PartialConnectionTask partialConnectionTask, DeviceDataService deviceDataService, EngineModelService engineModelService, MdcPropertyUtils mdcPropertyUtils) {
        super.writeTo(connectionTask, partialConnectionTask, deviceDataService, engineModelService, mdcPropertyUtils);
        connectionTask.setComPortPool(Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace() ? null : (InboundComPortPool) engineModelService.findComPortPool(this.comPortPool));
    }

    @Override
    public ConnectionTask<?,?> createTask(DeviceDataService deviceDataService, EngineModelService engineModelService, Device device, MdcPropertyUtils mdcPropertyUtils, PartialConnectionTask partialConnectionTask, boolean incomplete) {
        if (!PartialInboundConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            throw new WebApplicationException("Expected partial connection task to be 'Inbound'", Response.Status.BAD_REQUEST);
        }
        PartialInboundConnectionTask partialInboundConnectionTask = (PartialInboundConnectionTask) partialConnectionTask;
        InboundComPortPool inboundComPortPool=null;
        if (!Checks.is(comPortPool).emptyOrOnlyWhiteSpace()) {
            inboundComPortPool = (InboundComPortPool) engineModelService.findComPortPool(this.comPortPool);
        }

        InboundConnectionTask inboundConnectionTask = deviceDataService.newInboundConnectionTask(device, partialInboundConnectionTask, inboundComPortPool);
        writeTo(inboundConnectionTask, partialInboundConnectionTask, deviceDataService, engineModelService, mdcPropertyUtils);
        inboundConnectionTask.save();
        return inboundConnectionTask;
    }

}
