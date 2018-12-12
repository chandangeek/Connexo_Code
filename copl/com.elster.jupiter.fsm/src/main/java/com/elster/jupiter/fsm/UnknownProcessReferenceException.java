/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to remove a {@link ProcessReference} from a {@link State}
 * that was not previously defined on that State.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-10 (09:46)
 */
public final class UnknownProcessReferenceException extends LocalizedException {

    public UnknownProcessReferenceException(Thesaurus thesaurus, State state, BpmProcessDefinition process) {
        super(thesaurus, MessageSeeds.NO_SUCH_PROCESS_ON_STATE, process.getId(), state.getName(), state.getFiniteStateMachine().getName());
        this.set("finiteStateMachineId", state.getFiniteStateMachine().getId());
        this.set("processId", process.getId());
        this.set("stateName", state.getName());
    }

    public UnknownProcessReferenceException(Thesaurus thesaurus, State state, EndPointConfiguration endPointConfiguration) {
        super(thesaurus, MessageSeeds.NO_SUCH_ENDPOINT_CONFUGURATION_ON_STATE, endPointConfiguration.getId(), state.getName(), state.getFiniteStateMachine().getName());
        this.set("finiteStateMachineId", state.getFiniteStateMachine().getId());
        this.set("endPointConfigurationId", endPointConfiguration.getId());
        this.set("stateName", state.getName());
    }
}