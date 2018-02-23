/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import java.util.Optional;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockModule extends AbstractModule {

    private BundleContext bundleContext;
    private EventAdmin eventAdmin;
    private TimeService timeService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;

    public MockModule(BundleContext bundleContext) {
        super();
        this.bundleContext = bundleContext;
        this.eventAdmin = mock(EventAdmin.class);
        this.timeService = mock(TimeService.class);
        this.deviceMessageSpecificationService = mock(DeviceMessageSpecificationService.class);

        when(deviceMessageSpecificationService.findCategoryById(anyInt())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return Optional.of(DeviceMessageTestCategories.values()[((int) args[0])]);
        });
    }

    @Override
    protected void configure() {
        bind(BundleContext.class).toInstance(bundleContext);
        bind(EventAdmin.class).toInstance(eventAdmin);
        bind(TimeService.class).toInstance(this.timeService);
        bind(DeviceMessageSpecificationService.class).toInstance(this.deviceMessageSpecificationService);
        bind(SearchService.class).toInstance(mock(SearchService.class));
        bind(LicenseService.class).toInstance(mock(LicenseService.class));
        bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        bind(HttpService.class).toInstance(mock(HttpService.class));
    }

}