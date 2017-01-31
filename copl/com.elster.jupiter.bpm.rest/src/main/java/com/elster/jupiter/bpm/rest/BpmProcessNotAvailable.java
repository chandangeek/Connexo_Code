/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.bpm.rest.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;


public class BpmProcessNotAvailable extends LocalizedException {

    public BpmProcessNotAvailable(Thesaurus thesaurus, Object... args) {
        super(thesaurus, MessageSeeds.PROCESS_NOT_AVAILABLE, args);
    }
}
