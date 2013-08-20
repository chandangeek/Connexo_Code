package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.util.time.Clock;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;

import java.util.Date;
import java.util.TimeZone;

/**
 * Osgi component that implements the Clock interface.
 */
@Component(name = "com.elster.jupiter.time.clock", service = { Clock.class }, immediate = true, property="name=" + Bus.COMPONENTNAME)
public class ClockServiceImpl implements ServiceLocator, Clock {

    private volatile Clock clock;

    public ClockServiceImpl() {
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    public void activate(ComponentContext componentContext) {
        clock = new DefaultClock();
        Bus.setServiceLocator(this);
    }
    
    public void deactivate(ComponentContext componentContext) {
    	Bus.setServiceLocator(null);
    }

    @Override
    public TimeZone getTimeZone() {
        return clock.getTimeZone();
    }

    @Override
    public Date now() {
        return clock.now();
    }
}
