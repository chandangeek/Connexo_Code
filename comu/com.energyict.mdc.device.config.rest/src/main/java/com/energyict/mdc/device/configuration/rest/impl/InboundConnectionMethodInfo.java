/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.ws.rs.core.UriInfo;

public class InboundConnectionMethodInfo extends ConnectionMethodInfo<PartialInboundConnectionTask> {

    public InboundConnectionMethodInfo() {
    }

    public InboundConnectionMethodInfo(PartialInboundConnectionTask partialInboundConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        super(partialInboundConnectionTask, uriInfo, mdcPropertyUtils, thesaurus);
    }

    @Override
    protected void writeTo(PartialInboundConnectionTask partialConnectionTask, DeviceType deviceType, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService) {
        super.writeTo(partialConnectionTask, deviceType, engineConfigurationService, protocolPluggableService);
        if (!Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace()) {
            engineConfigurationService.findInboundComPortPoolByName(this.comPortPool).ifPresent(partialConnectionTask::setComportPool);
        } else {
            partialConnectionTask.setComportPool(null);
        }
        partialConnectionTask.setDefault(this.isDefault);
    }

    @Override
    public PartialConnectionTask createPartialTask(DeviceConfiguration deviceConfiguration, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        ConnectionTypePluggableClass connectionTypePluggableClass = findConnectionTypeOrThrowException(this.connectionTypePluggableClass.id, protocolPluggableService);
        InboundComPortPool inboundComPortPool = (InboundComPortPool) engineConfigurationService.findComPortPoolByName(this.comPortPool).orElse(null);
        PartialInboundConnectionTaskBuilder connectionTaskBuilder =
                deviceConfiguration
                        .newPartialInboundConnectionTask(name, connectionTypePluggableClass, protocolDialectConfigurationProperties)
                        .comPortPool(inboundComPortPool)
                        .connectionFunction(getConnectionFunction(deviceConfiguration.getDeviceType()))
                        .asDefault(this.isDefault);
        addPropertiesToPartialConnectionTask(connectionTaskBuilder, connectionTypePluggableClass);
        return connectionTaskBuilder.build();
    }

}