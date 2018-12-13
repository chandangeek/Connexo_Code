/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.impl.kpi.RegisteredDevicesKpiServiceImpl;
import com.energyict.mdc.device.topology.impl.multielement.MultiElementDeviceServiceImpl;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (17:07)
 */
public class TopologyModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(NlsService.class);
        requireBinding(ConnectionTaskService.class);
        requireBinding(CommunicationTaskService.class);
        requireBinding(MessageService.class);
        requireBinding(TaskService.class);
        requireBinding(KpiService.class);
        bind(TopologyService.class).to(ServerTopologyService.class).in(Scopes.SINGLETON);
        bind(MultiElementDeviceService.class).to(MultiElementDeviceServiceImpl.class).in(Scopes.SINGLETON);
        bind(ServerTopologyService.class).to(TopologyServiceImpl.class).in(Scopes.SINGLETON);
        bind(RegisteredDevicesKpiService.class).to(RegisteredDevicesKpiServiceImpl.class).in(Scopes.SINGLETON);
    }

}