package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDevice;

import java.time.Instant;

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
    public void changeState(State newState, Instant effective);

    /**
     * Changes this EndDevice's {@link FiniteStateMachine},
     * mapping the current {@link State} to the State in the
     * new FiniteStateMachine with the same name.
     *
     * @param newStateMachine The new FiniteStateMachine
     * @param effective The instant in time when the change over should be effective
     */
    public void changeStateMachine(FiniteStateMachine newStateMachine, Instant effective);

}