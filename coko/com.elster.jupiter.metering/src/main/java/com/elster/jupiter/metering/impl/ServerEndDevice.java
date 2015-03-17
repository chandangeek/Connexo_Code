package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDevice;

/**
 * Add behavior to {@link EndDevice} that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-17 (08:49)
 */
public interface ServerEndDevice extends EndDevice {

    /**
     * Changes the {@link State} of this EndDevice
     * to the newly specified State. There is no
     * need to save the device as that is done for you.
     *
     * @param newState The new State
     */
    public void changeState(State newState);

}