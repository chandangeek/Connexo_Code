package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.execution.impl;

import com.elster.jupiter.mdm.usagepoint.lifecycle.impl.execution.UsagePointLifeCycleViolation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when
 * an {@link com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition}
 * is executed by the user but some of {@link com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck}s
 * or {@link com.elster.jupiter.mdm.usagepoint.lifecycle.MicroAction}
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