/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.DeviceMicroCheckFactory;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class TestMicroCheck implements MicroCheck {

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

    public static final class Factory implements DeviceMicroCheckFactory {

        @Override
        public Optional<MicroCheck> from(String microActionKey) {
            return Optional.of(new TestMicroCheck());
        }

        @Override
        public Set<MicroCheck> getAllChecks() {
            return Collections.singleton(new TestMicroCheck());
        }

        @Override
        public boolean equals(Object o) {
            return this == o
                    || o instanceof Factory;
        }
    }
}
