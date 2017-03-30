/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to change the {@link FiniteStateMachine} of a set of {@link EndDevice}s
 * but that would cause a compatibility problem for at least one EndDevice
 * that is <strong>currently</strong> using a {@link State}
 * that no longer exists in the new FiniteStateMachine.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-19 (11:45)
 */
public class IncompatibleFiniteStateMachineChangeException extends RuntimeException {

    private final List<State> missingStates;

    public IncompatibleFiniteStateMachineChangeException(State... missingStates) {
        super();
        this.missingStates = Arrays.asList(missingStates);
    }

    public IncompatibleFiniteStateMachineChangeException(List<State> missingStates) {
        super();
        this.missingStates = new ArrayList<>(missingStates);
    }

    public List<State> getMissingStates() {
        return Collections.unmodifiableList(this.missingStates);
    }

}