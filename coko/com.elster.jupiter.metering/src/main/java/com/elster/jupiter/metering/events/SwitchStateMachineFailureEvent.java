package com.elster.jupiter.metering.events;

/**
 * Models the event that switching the
 * {@link com.elster.jupiter.fsm.FiniteStateMachine}
 * of an {@link com.elster.jupiter.metering.EndDevice}
 * failed because the new FiniteStateMachine no longer
 * has a State with the same name as the State
 * of the EndDevice at the time of switching.
 * The EndDevice is untouched but will most likely
 * be inconsistent because of the failure.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-21 (13:07)
 */

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SwitchStateMachineFailureEvent {

    /**
     * The unique identifier of the {@link com.elster.jupiter.metering.EndDevice}
     * for which the switch to a new {@link com.elster.jupiter.fsm.FiniteStateMachine}
     * failed.
     *
     * @return The unique identifier
     * @see com.elster.jupiter.metering.MeteringService#findEndDevice(long)
     */
    public long getEndDeviceId();

    /**
     * Gets the name of the {@link com.elster.jupiter.fsm.State}
     * of the {@link com.elster.jupiter.metering.EndDevice}
     * before the switch to a new {@link com.elster.jupiter.fsm.FiniteStateMachine}
     * failed. If nothing happened in between to the EndDevice
     * that should still be the current State of the EndDevice.
     *
     * @return The State of the EndDevice before the switch
     */
    public String getEndDeviceStateName();

    /**
     * Gets the unique identifier of the {@link com.elster.jupiter.fsm.FiniteStateMachine}
     * of the {@link com.elster.jupiter.metering.EndDevice} before the switch.
     * If no other switches have occurred, that should still be the current
     * FiniteStateMachine of the EndDevice.
     *
     * @return The unique identifier of the FiniteStateMachine before the switch
     */
    public long getOldFiniteStateMachineId();

    /**
     * Gets the unique identifier of the {@link com.elster.jupiter.fsm.FiniteStateMachine}
     * to which the {@link com.elster.jupiter.metering.EndDevice} was trying to switch.
     *
     * @return The unique identifier of the new FiniteStateMachine
     */
    public long getNewFiniteStateMachineId();

}