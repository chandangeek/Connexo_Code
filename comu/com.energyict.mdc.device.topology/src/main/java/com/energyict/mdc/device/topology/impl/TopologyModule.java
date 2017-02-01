/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;

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
        bind(TopologyService.class).to(TopologyServiceImpl.class).in(Scopes.SINGLETON);
        bind(ServerTopologyService.class).to(TopologyServiceImpl.class).in(Scopes.SINGLETON);
    }

}