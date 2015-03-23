package com.energyict.mdc.device.lifecycle;

import java.util.List;

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

    private final List<DeviceLifeCycleActionViolation> violations;

    private MultipleMicroCheckViolationsException(List<DeviceLifeCycleActionViolation> violations) {
        super();
        this.violations = violations;
    }

    // Todo: override getLocalizedMessage and format all violations
}