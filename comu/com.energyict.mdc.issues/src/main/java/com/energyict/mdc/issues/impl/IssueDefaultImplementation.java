package com.energyict.mdc.issues.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.upl.issue.Issue;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

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
    private final Object[] messageArguments;
    private Optional<Exception> exception = Optional.empty();

    public IssueDefaultImplementation(Thesaurus thesaurus, Instant timestamp, String description) {
        this(thesaurus, timestamp, null, description);
    }

    public IssueDefaultImplementation(Thesaurus thesaurus, Instant timestamp, Object source, String description, Object... arguments) {
        super();
        this.timestamp = timestamp;
        this.source = source;
        this.messageArguments = Stream.of(arguments).map(o -> {
            if (o instanceof Exception) {
                exception = Optional.of((Exception) o);
                return null;
            } else {
                return o;
            }
        }).toArray(Object[]::new);
        this.description = MessageFormat.format(convertSingleQuoteArgumentsToDoubleQuoteArguments(thesaurus.getString(description, description)), messageArguments);
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

    @Override
    public Optional<Exception> getException() {
        return exception;
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