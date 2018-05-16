/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest.exception;

import com.elster.jupiter.bpm.rest.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;


public class BpmProcessVariableNotAvailableInFlow extends LocalizedException {

    public BpmProcessVariableNotAvailableInFlow(Thesaurus thesaurus, Object... args) {
        super(thesaurus, MessageSeeds.PROCESS_VARIABLE_NOT_AVAILABLE_IN_FLOW, args);
    }
}
