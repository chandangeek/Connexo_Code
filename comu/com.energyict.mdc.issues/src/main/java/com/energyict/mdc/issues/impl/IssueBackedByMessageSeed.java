package com.energyict.mdc.issues.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.upl.tasks.Issue;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Serves as the root for classes that implement one of the {@link Issue} sub-interfaces
 * that is backed by a {@link MessageSeed} to produce its description.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-21 (15:13)
 */
abstract class IssueBackedByMessageSeed implements Issue {

    private final Object source;
    private final Instant timestamp;
    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final Object[] messageArguments;
    private Optional<Exception> exception = Optional.empty();

    IssueBackedByMessageSeed(Object source, Instant timestamp, Thesaurus thesaurus, MessageSeed messageSeed, Object... arguments) {
        this.source = source;
        this.timestamp = timestamp;
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.messageArguments = Stream.of(arguments).map(o -> {
            if (o instanceof Exception) {
                exception = Optional.of((Exception) o);
                return null;
            } else {
                return o;
            }
        }).toArray(Object[]::new);
    }

    @Override
    public Instant getTimestamp() {
        return this.timestamp;
    }

    @Override
    public String getDescription() {
        return this.thesaurus.getFormat(this.messageSeed).format(this.messageArguments);
    }

    @Override
    public Object getSource() {
        return this.source;
    }

    @Override
    public boolean isWarning() {
        return false;
    }

    @Override
    public boolean isProblem() {
        return false;
    }

    @Override
    public Optional<Exception> getException() {
        return exception;
    }
}