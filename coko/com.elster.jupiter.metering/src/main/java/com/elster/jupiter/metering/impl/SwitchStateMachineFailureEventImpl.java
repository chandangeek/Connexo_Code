package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.events.SwitchStateMachineFailureEvent;

/**
 * Provides an implementation for the {@link SwitchStateMachineFailureEvent} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-21 (09:54)
 */
public class SwitchStateMachineFailureEventImpl implements SwitchStateMachineFailureEvent {

    private final long endDeviceId;
    private final String endDeviceStateName;
    private final long oldFiniteStateMachineId;
    private final long newFiniteStateMachineId;

    public SwitchStateMachineFailureEventImpl(long endDeviceId, String endDeviceStateName, long oldFiniteStateMachineId, long newFiniteStateMachineId) {
        super();
        this.endDeviceId = endDeviceId;
        this.endDeviceStateName = endDeviceStateName;
        this.oldFiniteStateMachineId = oldFiniteStateMachineId;
        this.newFiniteStateMachineId = newFiniteStateMachineId;
    }

    @Override
    public long getEndDeviceId() {
        return endDeviceId;
    }

    @Override
    public String getEndDeviceStateName() {
        return endDeviceStateName;
    }

    @Override
    public long getOldFiniteStateMachineId() {
        return oldFiniteStateMachineId;
    }

    @Override
    public long getNewFiniteStateMachineId() {
        return newFiniteStateMachineId;
    }

}