/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by bbl on 18/06/2016.
 */
public class GatewayRetriever {

    private final TopologyService topologyService;
    private final Map<Device, Device> gatewayCache;

    public GatewayRetriever(TopologyService topologyService) {
        this(topologyService, null);
    }

    public GatewayRetriever(TopologyService topologyService, List<Device> deviceList) {
        this.topologyService = topologyService;
        if (deviceList != null) {
            gatewayCache = topologyService.getPhycicalGateways(deviceList);
        } else {
            gatewayCache = null;
        }
    }

    public Optional<Device> getPhysicalGateway(Device device) {
        if (gatewayCache != null) {
            return gatewayCache.containsKey(device) ? Optional.of(gatewayCache.get(device)) : Optional.empty();
        }
        return topologyService.getPhysicalGateway(device);
    }
}
