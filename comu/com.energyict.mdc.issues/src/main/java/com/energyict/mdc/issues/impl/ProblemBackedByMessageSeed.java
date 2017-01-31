/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issues.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issues.Problem;

import java.time.Instant;

/**
 * Provides an implementation for the {@link Problem} interface
 * that is backed by a {@link MessageSeed} to produce its description.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-21 (15:18)
 */
class ProblemBackedByMessageSeed extends IssueBackedByMessageSeed implements Problem {

    ProblemBackedByMessageSeed(Object source, Instant timestamp, Thesaurus thesaurus, MessageSeed messageSeed, Object... arguments) {
        super(source, timestamp, thesaurus, messageSeed, arguments);
    }

    @Override
    public boolean isProblem() {
        return true;
    }

}