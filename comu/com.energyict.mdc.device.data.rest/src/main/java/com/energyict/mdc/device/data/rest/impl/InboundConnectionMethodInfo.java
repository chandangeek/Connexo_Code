/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class InboundConnectionMethodInfo extends ConnectionMethodInfo<InboundConnectionTask> {

    public InboundConnectionMethodInfo() {
    }

    public InboundConnectionMethodInfo(InboundConnectionTask partialInboundConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        super(partialInboundConnectionTask, uriInfo, mdcPropertyUtils, thesaurus);
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
        inboundConnectionTaskBuilder.setProtocolDialectConfigurationProperties(getProtocolDialectConfigurationProperties(device));
        if (!Checks.is(comPortPool).emptyOrOnlyWhiteSpace()) {
            engineConfigurationService
                    .findInboundComPortPoolByName(this.comPortPool)
                    .ifPresent(inboundConnectionTaskBuilder::setComPortPool);
        }
        inboundConnectionTaskBuilder.setConnectionTaskLifecycleStatus(this.status);
        return inboundConnectionTaskBuilder.add();
    }

    private ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties(Device device) {
        return device.getDeviceConfiguration().getProtocolDialectConfigurationPropertiesList().stream().filter(protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getName().equals(this.protocolDialect)).findFirst().orElse(null);
    }

}