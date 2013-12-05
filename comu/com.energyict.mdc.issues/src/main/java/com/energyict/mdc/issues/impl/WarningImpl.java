package com.energyict.mdc.issues.impl;

import com.energyict.mdc.issues.Warning;

import java.util.Date;

/**
 * Models an {@link com.energyict.mdc.issues.Issue Issue} that can be worked around.
 *
 * @param <S> The type of source object
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public class WarningImpl<S> extends IssueDefaultImplementation<S> implements Warning<S> {

    public WarningImpl (Date timestamp, String description) {
        super(timestamp, description);
    }

    public WarningImpl (Date timestamp, S source, String description, Object... arguments) {
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