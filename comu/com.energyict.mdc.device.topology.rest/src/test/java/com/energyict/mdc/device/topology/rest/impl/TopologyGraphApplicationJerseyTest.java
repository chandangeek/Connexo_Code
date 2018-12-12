/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.rest.impl;


import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.impl.ServerTopologyService;
import com.energyict.mdc.device.topology.rest.GraphLayerService;

import javax.ws.rs.core.Application;

import java.time.Clock;

import org.mockito.Mock;

import static org.mockito.Mockito.spy;

public class TopologyGraphApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    ServerTopologyService topologyService;
    @Mock
    DeviceService deviceService;
    @Mock
    GraphLayerService graphLayerService;

    protected DeviceGraphFactory deviceGraphFactory;

    @Override
    protected Application getApplication() {
        TopologyGraphApplication application = new TopologyGraphApplication();
        application.setDeviceService(deviceService);
        application.setTopologyService(topologyService);
        application.setGraphLayerService(graphLayerService);
        application.setClock(Clock.systemDefaultZone());
        application.setNlsService(nlsService);
        application.setDeviceGraphFactory(deviceGraphFactory = new DeviceGraphFactory(topologyService, graphLayerService, Clock.systemDefaultZone()));
        return application;
    }

}
