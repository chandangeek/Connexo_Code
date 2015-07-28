package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that all {@link com.elster.jupiter.issue.share.entity.Issue}s
 * on a device are closed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-17 (12:47)
 */
public class AllIssuesAreClosed extends TranslatableServerMicroCheck {

    public AllIssuesAreClosed(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    protected MicroCheck getMicroCheck() {
        return MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (device.hasOpenIssues()) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.ALL_ISSUES_AND_ALARMS_ARE_CLOSED,
                            MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED));
        }
        else {
            return Optional.empty();
        }
    }

}