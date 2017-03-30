/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.config.EngineConfigurationService;
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
    protected void writeTo(InboundConnectionTask connectionTask, PartialConnectionTask partialConnectionTask, EngineConfigurationService engineConfigurationService, MdcPropertyUtils mdcPropertyUtils) {
        super.writeTo(connectionTask, partialConnectionTask, engineConfigurationService, mdcPropertyUtils);
        if (Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace()) {
            connectionTask.setComPortPool(null);
        }
        else {
            connectionTask.setComPortPool(engineConfigurationService.findInboundComPortPoolByName(this.comPortPool).orElse(null));
        }
    }

    @Override
    public ConnectionTask<?, ?> createTask(EngineConfigurationService engineConfigurationService, Device device, MdcPropertyUtils mdcPropertyUtils, PartialConnectionTask partialConnectionTask) {
        if (!(partialConnectionTask instanceof PartialInboundConnectionTask)) {
            throw new WebApplicationException("Expected partial connection task to be 'Inbound'", Response.Status.BAD_REQUEST);
        }
        PartialInboundConnectionTask partialInboundConnectionTask = (PartialInboundConnectionTask) partialConnectionTask;
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask);
        if (!Checks.is(comPortPool).emptyOrOnlyWhiteSpace()) {
            engineConfigurationService
                    .findInboundComPortPoolByName(this.comPortPool)
                    .ifPresent(inboundConnectionTaskBuilder::setComPortPool);
        }
        inboundConnectionTaskBuilder.setConnectionTaskLifecycleStatus(this.status);
        return inboundConnectionTaskBuilder.add();
    }

}