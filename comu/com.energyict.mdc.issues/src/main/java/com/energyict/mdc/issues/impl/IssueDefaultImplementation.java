package com.energyict.mdc.issues.impl;

import com.energyict.mdc.issues.Issue;

import java.text.MessageFormat;
import java.time.Instant;

/**
 * Provides a default implementation for the {@link Issue} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public abstract class IssueDefaultImplementation implements Issue {

    private String description;
    private Object source;
    private Instant timestamp;

    public IssueDefaultImplementation (Instant timestamp, String description) {
        this(timestamp, null, description);
    }

    public IssueDefaultImplementation(Instant timestamp, Object source, String description, Object... arguments) {
        super();
        this.timestamp = timestamp;
        this.source = source;
        this.description = MessageFormat.format(description, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant getTimestamp () {
        return this.timestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription () {
        return this.description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getSource () {
        return this.source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarning () {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProblem () {
        return false;
    }

}