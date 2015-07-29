package com.energyict.mdc.device.config;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.IncompatibleFiniteStateMachineChangeException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to change the {@link DeviceLifeCycle} of a {@link DeviceType}
 * but that would cause an incompatibility problem for
 * devices of the DeviceType that are <strong>currently</strong> using
 * {@link State}s that no longer exists in the new DeviceLifeCycle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-18 (10:58)
 */
public class IncompatibleDeviceLifeCycleChangeException extends RuntimeException {

    private final List<State> missingStates;

    public static IncompatibleDeviceLifeCycleChangeException wrapping(IncompatibleFiniteStateMachineChangeException cause) {
        return new IncompatibleDeviceLifeCycleChangeException(cause);
    }

    private IncompatibleDeviceLifeCycleChangeException(IncompatibleFiniteStateMachineChangeException cause) {
        super(cause);
        this.missingStates = cause.getMissingStates();
    }

    public List<State> getMissingStates() {
        return Collections.unmodifiableList(this.missingStates);
    }

}