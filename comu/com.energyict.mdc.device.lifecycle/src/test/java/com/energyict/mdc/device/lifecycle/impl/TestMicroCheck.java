/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheck;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DeviceMicroCheckFactory;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class TestMicroCheck implements ExecutableMicroCheck {
    @FunctionalInterface
    public interface TriFunction<D, I, S, R> {
        R apply(D d, I i, S s);
    }

    private final TriFunction<Device, Instant, State, Optional<ExecutableMicroCheckViolation>> onExecute;

    public TestMicroCheck(TriFunction<Device, Instant, State, Optional<ExecutableMicroCheckViolation>> onExecute) {
        this.onExecute = onExecute;
    }

    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        return this.getClass().getName();
    }

    @Override
    public String getCategory() {
        return this.getClass().getPackage().getName();
    }

    @Override
    public String getCategoryName() {
        return this.getClass().getPackage().getName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || o instanceof TestMicroCheck
                && Objects.equals(getKey(), ((TestMicroCheck) o).getKey());
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant transitionTime, State state) {
        if (this.onExecute != null) {
            return this.onExecute.apply(device, transitionTime, state);
        }
        return Optional.empty();
    }

    public static final class Factory implements DeviceMicroCheckFactory {
        private TriFunction<Device, Instant, State, Optional<ExecutableMicroCheckViolation>> onExecute;

        @Override
        public Optional<MicroCheck> from(String microActionKey) {
            return Optional.of(new TestMicroCheck(this.onExecute));
        }

        @Override
        public Set<MicroCheck> getAllChecks() {
            return Collections.singleton(new TestMicroCheck(this.onExecute));
        }

        public void setOnExecute(TriFunction<Device, Instant, State, Optional<ExecutableMicroCheckViolation>> onExecute) {
            this.onExecute = onExecute;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj
                    || obj instanceof Factory;
        }
    }
}
