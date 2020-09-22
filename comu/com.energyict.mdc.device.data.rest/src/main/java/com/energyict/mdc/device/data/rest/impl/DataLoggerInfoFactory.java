/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;

import javax.inject.Inject;
import java.util.Optional;

public class DataLoggerInfoFactory {
    private volatile TopologyService topologyService;

    @Inject
    public DataLoggerInfoFactory(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public DataLoggerInfo fromDevice(Device device) {
        DataLoggerInfo loggerInfo = new DataLoggerInfo();
        loggerInfo.isDataLoggerSlave = device.getDeviceType().isDataloggerSlave();
        if (loggerInfo.isDataLoggerSlave) {
            Optional<Device> physicalGateway = topologyService.getPhysicalGateway(device);
            if (physicalGateway.isPresent()) {
                loggerInfo.masterDeviceName = physicalGateway.get().getName();
            }
        }
        return loggerInfo;
    }
}
