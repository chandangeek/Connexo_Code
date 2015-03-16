package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.associations.Effectivity;

/**
 * Models the {@link State} of an {@link EndDevice} over time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-13 (16:51)
 */
public interface EndDeviceLifeCycleStatus extends Effectivity {

    /**
     * Gets the {@link EndDevice} for which {@link State}
     * is maintained over time.
     *
     * @return The EndDevice
     */
    public EndDevice getEndDevice();

    /**
     * Gets the {@link State} of the {@link EndDevice}.
     *
     * @return The EndDevice
     */
    public State getState();

}