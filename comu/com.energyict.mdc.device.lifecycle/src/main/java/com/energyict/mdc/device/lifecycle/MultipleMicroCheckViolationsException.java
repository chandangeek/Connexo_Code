/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MultipleMicroCheckViolationsException extends DeviceLifeCycleActionViolationException {

    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final List<ExecutableMicroCheckViolation> violations;

    public MultipleMicroCheckViolationsException(Thesaurus thesaurus, MessageSeed messageSeed, List<ExecutableMicroCheckViolation> violations) {
        super();
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
        return this.violations
                .stream()
                .map(ExecutableMicroCheckViolation::getLocalizedMessage)
                .collect(Collectors.joining(", "));
    }
}
