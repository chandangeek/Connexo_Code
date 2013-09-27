package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.util.time.Clock;
import org.osgi.service.component.annotations.*;

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

    @Activate
    public void activate() {
        clock = new DefaultClock();
        Bus.setServiceLocator(this);
    }
    
    @Deactivate
    public void deactivate() {
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
