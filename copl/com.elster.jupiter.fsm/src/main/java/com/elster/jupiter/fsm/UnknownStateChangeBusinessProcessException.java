package com.elster.jupiter.fsm;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to disable a {@link StateChangeBusinessProcess}
 * that was not enable before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-29 (15:58)
 */
public final class UnknownStateChangeBusinessProcessException extends LocalizedException {

    public UnknownStateChangeBusinessProcessException(Thesaurus thesaurus, String deploymentId, String processId) {
        super(thesaurus, MessageSeeds.NO_SUCH_PROCESS, deploymentId, processId);
        this.set("deploymentId", deploymentId);
        this.set("processId", processId);
    }

}