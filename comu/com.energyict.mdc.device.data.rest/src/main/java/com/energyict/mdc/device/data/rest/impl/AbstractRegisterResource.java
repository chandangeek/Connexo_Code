package com.energyict.mdc.device.data.rest.impl;

import java.time.Clock;

/**
 * Created by dvy on 2/02/2017.
 */
public abstract class AbstractRegisterResource {
    protected final Clock clock;

    public AbstractRegisterResource(Clock clock) {
        this.clock = clock;
    }

}
