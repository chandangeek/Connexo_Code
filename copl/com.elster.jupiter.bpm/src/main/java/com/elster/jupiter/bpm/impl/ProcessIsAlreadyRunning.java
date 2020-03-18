/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class ProcessIsAlreadyRunning extends LocalizedException {

    public ProcessIsAlreadyRunning(Thesaurus thesaurus, Object... args) {
        super(thesaurus, MessageSeeds.PROCESS_IS_ALREADY_RUNNING, args);
    }
}
