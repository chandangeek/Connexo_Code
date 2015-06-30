package com.energyict.mdc.tasks;

import com.elster.jupiter.bpm.BpmService;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import static org.mockito.Mockito.mock;

public class MockModule extends AbstractModule {

    private BundleContext bundleContext;
    private EventAdmin eventAdmin;

    public MockModule(BundleContext bundleContext) {
        super();
        this.bundleContext = bundleContext;
        this.eventAdmin =  mock(EventAdmin.class);
    }

    @Override
    protected void configure() {
        bind(BpmService.class).toInstance(mock(BpmService.class));
        bind(BundleContext.class).toInstance(bundleContext);
        bind(EventAdmin.class).toInstance(eventAdmin);
    }

}
