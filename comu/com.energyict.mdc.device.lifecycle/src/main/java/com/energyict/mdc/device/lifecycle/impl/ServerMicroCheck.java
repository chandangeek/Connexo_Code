/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
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
public interface ServerMicroCheck {

    /**
     * Evaluates this {@link MicroCheck} against the {@link Device}
     * and returns an appropriate {@link DeviceLifeCycleActionViolation}
     * when it fails.
     *
     * @param device The Device
     * @param effectiveTimestamp The effective timestamp of the transition
     * @param state
     * @return The violation if the check fails
     */
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp, State state);

    String getName();

    String getDescription();

    String getCategoryName();
}