/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import java.time.Instant;
import java.util.Optional;

/**
 * Models the implementation behavior of the {@link MicroCheck}
 * interface and is therefore reserved for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-23 (09:58)
 */
public interface ExecutableMicroCheck extends MicroCheck {

    /**
     * Evaluates this {@link MicroCheck} against the {@link Device}
     * and returns an appropriate {@link ExecutableMicroCheckViolation}
     * when it fails.
     *
     * @param device             The Device
     * @param effectiveTimestamp The effective timestamp of the transition
     * @param toState              The new target state
     * @return The violation if the check fails
     */
    Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State toState);
}
