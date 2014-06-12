package com.energyict.mdc.issues.impl;

import com.energyict.mdc.issues.Warning;

import java.util.Date;

/**
 * Models an {@link com.energyict.mdc.issues.Issue Issue} that can be worked around.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public class WarningImpl extends IssueDefaultImplementation implements Warning {

    public WarningImpl (Date timestamp, String description) {
        super(timestamp, description);
    }

    public WarningImpl (Date timestamp, Object source, String description, Object... arguments) {
        super(timestamp, source, description, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarning() {
        return true;
    }

}