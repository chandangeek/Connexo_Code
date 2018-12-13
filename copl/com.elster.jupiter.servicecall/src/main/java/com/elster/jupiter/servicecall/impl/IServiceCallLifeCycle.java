/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;

import java.util.Optional;

/**
 * Created by TVN on 19/02/2016.
 */
public interface IServiceCallLifeCycle extends ServiceCallLifeCycle {
    Optional<State> getState(DefaultState defaultState);

    boolean canTransition(DefaultState currentState, DefaultState targetState);
}
