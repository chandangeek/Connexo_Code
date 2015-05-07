package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Provides an implementation for the {@link StateTimeSlice} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-07 (08:51)
 */
public class StateTimeSliceImpl implements StateTimeSlice {

    private final State state;
    private final Range<Instant> period;

    public static StateTimeSliceImpl from(EndDeviceLifeCycleStatus endDeviceLifeCycleStatus) {
        return new StateTimeSliceImpl(endDeviceLifeCycleStatus.getState(), endDeviceLifeCycleStatus.getRange());
    }

    private StateTimeSliceImpl(State state, Range<Instant> period) {
        super();
        this.state = state;
        this.period = period;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Range<Instant> getPeriod() {
        return period;
    }

}