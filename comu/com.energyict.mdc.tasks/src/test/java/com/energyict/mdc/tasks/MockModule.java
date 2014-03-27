package com.energyict.mdc.tasks;

import com.elster.jupiter.events.EventService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
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
        bind(BundleContext.class).toInstance(bundleContext);
        bind(EventAdmin.class).toInstance(eventAdmin);
        bind(EventService.class).to(SpyEventService.class).in(Scopes.SINGLETON);
    }

}
