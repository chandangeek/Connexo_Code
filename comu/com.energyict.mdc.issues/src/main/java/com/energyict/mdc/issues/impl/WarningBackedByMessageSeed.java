package com.energyict.mdc.issues.impl;

import com.energyict.mdc.issues.Warning;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.time.Instant;

/**
 * Provides an implementation for the {@link Warning} interface
 * that is backed by a {@link MessageSeed} to produce its description.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-21 (15:18)
 */
class WarningBackedByMessageSeed extends IssueBackedByMessageSeed implements Warning {

    WarningBackedByMessageSeed(Object source, Instant timestamp, Thesaurus thesaurus, MessageSeed messageSeed, Object... arguments) {
        super(source, timestamp, thesaurus, messageSeed, arguments);
    }

    @Override
    public boolean isWarning() {
        return true;
    }

}