/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public final class UnknownStateChangeBusinessProcessException extends LocalizedException {

    public UnknownStateChangeBusinessProcessException(Thesaurus thesaurus, String deploymentId, String processId) {
        super(thesaurus, MessageSeeds.NO_SUCH_PROCESS, deploymentId, processId);
        this.set("deploymentId", deploymentId);
        this.set("processId", processId);
    }

}