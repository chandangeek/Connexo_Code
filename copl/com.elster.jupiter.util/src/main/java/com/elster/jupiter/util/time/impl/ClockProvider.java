package com.elster.jupiter.util.time.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.*;

import java.time.Clock;

/**
 * Osgi component that implements the Clock interface.
 */
@Component(name = "com.elster.jupiter.time.clock", immediate = true)
public class ClockProvider  {

    private volatile ServiceRegistration<Clock> registration;

    public ClockProvider() {
    }

    @Activate
    public void activate(BundleContext context) {
    	registration = context.registerService(Clock.class, Clock.systemDefaultZone(), null);
    }
    
    @Deactivate
    public void deactivate() {
    	registration.unregister();
    }

}
