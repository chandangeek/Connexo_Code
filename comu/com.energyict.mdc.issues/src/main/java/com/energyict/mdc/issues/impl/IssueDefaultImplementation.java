package com.energyict.mdc.issues.impl;

import com.energyict.mdc.issues.Issue;

import com.elster.jupiter.nls.Thesaurus;

import java.text.MessageFormat;
import java.time.Instant;

/**
 * Provides a default implementation for the {@link Issue} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
public abstract class IssueDefaultImplementation implements Issue {

    private final String description;
    private final Object source;
    private final Instant timestamp;

    public IssueDefaultImplementation(Thesaurus thesaurus, Instant timestamp, String description) {
        this(thesaurus, timestamp, null, description);
    }

    public IssueDefaultImplementation(Thesaurus thesaurus, Instant timestamp, Object source, String description, Object... arguments) {
        super();
        this.timestamp = timestamp;
        this.source = source;
        this.description = MessageFormat.format(convertSingleQuoteArgumentsToDoubleQuoteArguments(thesaurus.getStringBeyondComponent(description, description)), arguments);
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

    /**
     * Due to the behavior of the MessageFormat object, we need to apply another single quote if we want to quote an argument
     *
     * @param description the description that can contain single quote arguments (like '{0}' etc.)
     * @return the description with double quote arguments (like ''{0}'')
     */
    private String convertSingleQuoteArgumentsToDoubleQuoteArguments(String description) {
        String openBracketsReplaced = description.replaceAll(" '\\{\\b", " ''{");
        String closedBracketsReplaced = openBracketsReplaced.replaceAll("\\b\\}' ", "}'' ");
        StringBuilder stringBuilder = new StringBuilder();
        if(closedBracketsReplaced.startsWith("'{")){
            stringBuilder.append("'");
        }
        stringBuilder.append(closedBracketsReplaced);
        if(closedBracketsReplaced.endsWith("}'")){
            stringBuilder.append("'");
        }

        return stringBuilder.toString();
    }
}