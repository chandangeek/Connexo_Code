/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest.exception;

import com.elster.jupiter.bpm.rest.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class NoBpmConnectionException extends LocalizedException {

    public NoBpmConnectionException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.NO_BPM_CONNECTION);
    }
}

