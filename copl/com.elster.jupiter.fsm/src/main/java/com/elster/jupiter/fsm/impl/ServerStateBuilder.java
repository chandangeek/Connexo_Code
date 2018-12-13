/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FiniteStateMachineBuilder;

/**
 * Defines additional behavior for {@link FiniteStateMachineBuilder.StateBuilder}s
 * that is reserverd for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-09 (15:49)
 */
public interface ServerStateBuilder extends FiniteStateMachineBuilder.StateBuilder {

    /**
     * Gets the {@link StateImpl} that is being constructed by this builder.
     *
     * @return The State under construction
     */
    public StateImpl getUnderConstruction();

}