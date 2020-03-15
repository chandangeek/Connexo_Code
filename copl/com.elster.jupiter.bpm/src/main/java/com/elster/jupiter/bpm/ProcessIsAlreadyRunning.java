/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm;

import com.elster.jupiter.bpm.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class ProcessIsAlreadyRunning extends LocalizedException {

    public ProcessIsAlreadyRunning(Thesaurus thesaurus, Object... args) {
        super(thesaurus,  MessageSeeds.PROCESS_IS_ALREADY_RUNNING, args);
    }
}
