package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to disable a {@link TransitionBusinessProcess}
 * that is still in use by at least one {@link AuthorizedBusinessProcessAction}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-30 (10:37)
 */
public class TransitionBusinessProcessInUseException extends LocalizedException {

    public TransitionBusinessProcessInUseException(Thesaurus thesaurus, MessageSeed messageSeed, TransitionBusinessProcess process) {
        super(thesaurus, messageSeed, process.getDeploymentId(), process.getProcessId());
        this.set("deploymentId", process.getDeploymentId());
        this.set("processId", process.getProcessId());
    }

}