package com.energyict.mdc.issues.impl;

import com.energyict.mdc.issues.Issue;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Provides a default implementation for the {@link Issue} interface.
 *
 * @param <S> The type of source object
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public abstract class IssueDefaultImplementation<S> implements Issue<S> {

    private String description;
    private S source;
    private Date timestamp;

    public IssueDefaultImplementation (Date timestamp, String description) {
        this(timestamp, null, description);
    }

    public IssueDefaultImplementation(Date timestamp, S source, String description, Object... arguments) {
        super();
        this.timestamp = timestamp;
        this.source = source;
        this.description = MessageFormat.format(description, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getTimestamp () {
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
    public S getSource () {
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