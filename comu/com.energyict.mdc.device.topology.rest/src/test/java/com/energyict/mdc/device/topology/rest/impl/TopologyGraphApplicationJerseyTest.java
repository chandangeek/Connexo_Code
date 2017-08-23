/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.rest.impl;


import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphLayerService;

import javax.ws.rs.core.Application;

import java.time.Clock;

import org.mockito.Mock;

public class TopologyGraphApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    TopologyService topologyService;
    @Mock
    DeviceService deviceService;
    @Mock
    GraphLayerService graphLayerService;

    @Override
    protected Application getApplication() {
        TopologyGraphApplication application = new TopologyGraphApplication();
        application.setDeviceService(deviceService);
        application.setTopologyService(topologyService);
        application.setGraphLayerService(graphLayerService);
        application.setClock(Clock.systemDefaultZone());
        application.setNlsService(nlsService);
        return application;
    }

}
