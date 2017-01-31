/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when an {@link UsagePointTransition}
 * is executed by the user but some of {@link MicroCheck}s that configured on the transition are failed.
 */
public class ExecutableMicroCheckException extends UsagePointStateChangeException {

    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final List<ExecutableMicroCheckViolation> violations;

    public ExecutableMicroCheckException(Thesaurus thesaurus, MessageSeed messageSeed, List<ExecutableMicroCheckViolation> violations) {
        super(messageSeed.getKey());
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.violations = violations;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus.getFormat(this.messageSeed).format(this.violationMessagesAsCommaSeparatedList());
    }

    public List<ExecutableMicroCheckViolation> getViolations() {
        return Collections.unmodifiableList(this.violations);
    }

    private String violationMessagesAsCommaSeparatedList() {
        return this.violations.stream()
                .map(violation -> new StringBuilder(violation.getMicroCheck().getName()).append(" (").append(violation.getLocalizedMessage()).append(")"))
                .collect(Collectors.joining("", "\n\t- ", ""));
    }
}