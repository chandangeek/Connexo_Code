package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to disable a {@link TransitionBusinessProcess}
 * that was not enable before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-30 (10:37)
 */
public class UnknownTransitionBusinessProcessException extends LocalizedException {

    public UnknownTransitionBusinessProcessException(Thesaurus thesaurus, MessageSeed messageSeed, String deploymentId, String processId) {
        super(thesaurus, messageSeed, deploymentId, processId);
        this.set("deploymentId", deploymentId);
        this.set("processId", processId);
    }

}