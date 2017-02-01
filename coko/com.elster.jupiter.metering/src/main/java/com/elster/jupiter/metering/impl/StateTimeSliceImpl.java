/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.users.User;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link StateTimeSlice} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-07 (08:51)
 */
public class StateTimeSliceImpl implements StateTimeSlice {

    private final State state;
    private final Range<Instant> period;
    private final Optional<User> user;

    public static StateTimeSliceImpl from(EndDeviceLifeCycleStatus endDeviceLifeCycleStatus) {
        return new StateTimeSliceImpl(
                endDeviceLifeCycleStatus.getState(),
                endDeviceLifeCycleStatus.getRange(),
                endDeviceLifeCycleStatus.getUser());
    }

    private StateTimeSliceImpl(State state, Range<Instant> period, Optional<User> user) {
        super();
        this.state = state;
        this.period = period;
        this.user = user;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Range<Instant> getPeriod() {
        return period;
    }

    @Override
    public Optional<User> getUser() {
        return user;
    }

}