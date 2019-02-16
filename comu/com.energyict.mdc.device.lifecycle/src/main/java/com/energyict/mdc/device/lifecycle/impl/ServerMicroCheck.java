/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.MicroCheckNew;

import java.time.Instant;
import java.util.Optional;

/**
 * Models the implementation behavior of the {@link MicroCheck}
 * interface and is therefore reserved for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-23 (09:58)
 */
public interface ServerMicroCheck extends MicroCheckNew {

    /**
     * Evaluates this {@link MicroCheck} against the {@link Device}
     * and returns an appropriate {@link DeviceLifeCycleActionViolation}
     * when it fails.
     *
     * @param device             The Device
     * @param effectiveTimestamp The effective timestamp of the transition
     * @return The violation if the check fails
     */
    Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp);

    /**
     * Evaluates this {@link MicroCheck} against the {@link Device}
     * and returns an appropriate {@link DeviceLifeCycleActionViolation}
     * when it fails.
     *
     * @param device             The Device
     * @param effectiveTimestamp The effective timestamp of the transition
     * @param state              The new target state
     * @return The violation if the check fails
     */
    default Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp, State state) {
        return evaluate(device, effectiveTimestamp);
    }
}