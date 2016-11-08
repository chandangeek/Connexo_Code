package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleViolation;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when
 * an {@link UsagePointTransition}
 * is executed by the user but some of {@link MicroCheck}s
 * or {@link MicroAction}
 * that are configured on the transition failed.
 */
public class MultipleViolationsException extends UsagePointLifeCycleViolationException {

    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final List<UsagePointLifeCycleViolation> violations;

    public MultipleViolationsException(Thesaurus thesaurus, MessageSeed messageSeed, List<UsagePointLifeCycleViolation> violations) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.violations = violations;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus.getFormat(this.messageSeed).format(this.violationMessagesAsCommaSeparatedList());
    }

    public List<UsagePointLifeCycleViolation> getViolations() {
        return Collections.unmodifiableList(this.violations);
    }

    private String violationMessagesAsCommaSeparatedList() {
        return this.violations
                .stream()
                .map(UsagePointLifeCycleViolation::getLocalizedMessage)
                .collect(Collectors.joining(", "));
    }
}