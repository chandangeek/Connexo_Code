package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when
 * an {@link com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction}
 * is executed by the user but some of the {@link com.energyict.mdc.device.lifecycle.config.MicroCheck}s
 * that are configured on the action failed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (16:41)
 */
public class MultipleMicroCheckViolationsException extends DeviceLifeCycleActionViolationException {

    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final List<DeviceLifeCycleActionViolation> violations;

    public MultipleMicroCheckViolationsException(Thesaurus thesaurus, MessageSeed messageSeed, List<DeviceLifeCycleActionViolation> violations) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.violations = violations;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus.getFormat(this.messageSeed).format(this.violationMessagesAsCommaSeparatedList());
    }

    public List<DeviceLifeCycleActionViolation> getViolatedChecks(){
        return Collections.unmodifiableList(this.violations);
    }

    private String violationMessagesAsCommaSeparatedList() {
        return this.violations
                .stream()
                .map(DeviceLifeCycleActionViolation::getLocalizedMessage)
                .collect(Collectors.joining(", "));
    }

}