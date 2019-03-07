/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.DeviceMicroCheckFactory;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class TestMicroCheck implements MicroCheck {

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
        return getKey().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestMicroCheck that = (TestMicroCheck) o;
        return getKey() != null ? getKey().equals(that.getKey()) : that.getKey() == null;
    }

    public static class Factory implements DeviceMicroCheckFactory {

        @Override
        public Optional<Class<? extends MicroCheck>> from(String microActionKey) {
            return Optional.of(TestMicroCheck.class);
        }

        @Override
        public Set<Class<? extends MicroCheck>> getAllChecks() {
            return Collections.singleton(TestMicroCheck.class);
        }
    }
}
