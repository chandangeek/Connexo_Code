/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issues.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issues.Warning;

import java.time.Instant;

/**
 * Models an {@link com.energyict.mdc.issues.Issue Issue} that can be worked around.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public class WarningImpl extends IssueDefaultImplementation implements Warning {

    public WarningImpl(Thesaurus thesaurus, Instant timestamp, String description) {
        super(thesaurus, timestamp, description);
    }

    public WarningImpl(Thesaurus thesaurus, Instant timestamp, Object source, String description, Object... arguments) {
        super(thesaurus, timestamp, source, description, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarning() {
        return true;
    }

}