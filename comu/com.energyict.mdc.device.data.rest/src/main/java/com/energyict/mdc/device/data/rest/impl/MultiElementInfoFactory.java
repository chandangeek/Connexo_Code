/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;

import javax.inject.Inject;
import java.util.Optional;

public class MultiElementInfoFactory {
    private volatile TopologyService topologyService;

    @Inject
    public MultiElementInfoFactory(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public MultiElementInfo fromDevice(Device device) {
        MultiElementInfo multiInfo = new MultiElementInfo();
        multiInfo.isMultiElementSlave = device.getDeviceType().isMultiElementSlave();
        if (multiInfo.isMultiElementSlave) {
            Optional<Device> physicalGateway = topologyService.getPhysicalGateway(device);
            if (physicalGateway.isPresent()) {
                multiInfo.masterDeviceName = physicalGateway.get().getName();
            }
        }
        return multiInfo;
    }
}
