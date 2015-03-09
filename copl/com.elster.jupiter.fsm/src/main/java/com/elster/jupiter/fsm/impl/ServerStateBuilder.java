package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FinateStateMachineBuilder;

/**
 * Defines additional behavior for {@link FinateStateMachineBuilder.StateBuilder}s
 * that is reserverd for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-09 (15:49)
 */
public interface ServerStateBuilder extends FinateStateMachineBuilder.StateBuilder {

    /**
     * Gets the {@link StateImpl} that is being constructed by this builder.
     *
     * @return The State under construction
     */
    public StateImpl getUnderConstruction();

}