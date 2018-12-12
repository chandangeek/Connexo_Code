/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import static org.mockito.Mockito.mock;

public class MockModule extends AbstractModule {
    private BundleContext bundleContext;
    private EventAdmin eventAdmin;
    private ProtocolPluggableService protocolPluggableService;

    public MockModule(BundleContext bundleContext) {
        super();
        this.bundleContext = bundleContext;
        this.eventAdmin =  mock(EventAdmin.class);
        this.protocolPluggableService = mock(ProtocolPluggableService.class);
    }

    @Override
    protected void configure() {
        bind(BundleContext.class).toInstance(bundleContext);
        bind(EventAdmin.class).toInstance(eventAdmin);
        bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
        bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
    }
}
