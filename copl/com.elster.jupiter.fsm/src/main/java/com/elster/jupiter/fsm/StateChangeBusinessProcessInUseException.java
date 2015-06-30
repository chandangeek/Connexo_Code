package com.elster.jupiter.fsm;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to disable a {@link StateChangeBusinessProcess}
 * that is still in use by at least one {@link State}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-30 (16:14)
 */
public final class StateChangeBusinessProcessInUseException extends LocalizedException {

    public StateChangeBusinessProcessInUseException(Thesaurus thesaurus, StateChangeBusinessProcess process) {
        super(thesaurus, MessageSeeds.STATE_CHANGE_PROCESS_IN_USE, process.getDeploymentId(), process.getProcessId());
        this.set("deploymentId", process.getDeploymentId());
        this.set("processId", process.getProcessId());
    }

}