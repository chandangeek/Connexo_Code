/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to remove a {@link ProcessReference} from a {@link State}
 * that was not previously defined on that State.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-10 (09:46)
 */
public final class UnknownProcessReferenceException extends LocalizedException {

    public UnknownProcessReferenceException(Thesaurus thesaurus, State state, StateChangeBusinessProcess process) {
        super(thesaurus, MessageSeeds.NO_SUCH_PROCESS_ON_STATE, process.getDeploymentId(), process.getProcessId(), state.getName(), state.getFiniteStateMachine().getName());
        this.set("finiteStateMachineId", state.getFiniteStateMachine().getId());
        this.set("deploymentId", process.getDeploymentId());
        this.set("processId", process.getProcessId());
        this.set("stateName", state.getName());
    }

}