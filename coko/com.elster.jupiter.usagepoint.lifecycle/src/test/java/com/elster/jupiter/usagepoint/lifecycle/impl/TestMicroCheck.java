/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckViolation;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroCheckFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public class TestMicroCheck implements ExecutableMicroCheck {
    private final BiFunction<UsagePoint, Instant, Optional<ExecutableMicroCheckViolation>> onExecute;

    public TestMicroCheck(BiFunction<UsagePoint, Instant, Optional<ExecutableMicroCheckViolation>> onExecute) {
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

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(UsagePoint usagePoint, Instant transitionTime) {
        if (this.onExecute != null) {
            return this.onExecute.apply(usagePoint, transitionTime);
        }
        return Optional.empty();
    }

    public static class Factory implements UsagePointMicroCheckFactory {
        private BiFunction<UsagePoint, Instant, Optional<ExecutableMicroCheckViolation>> onExecute;

        @Override
        public Optional<MicroCheck> from(String microActionKey) {
            return Optional.of(new TestMicroCheck(this.onExecute));
        }

        @Override
        public Set<MicroCheck> getAllChecks() {
            return Collections.singleton(new TestMicroCheck(this.onExecute));
        }

        public void setOnExecute(BiFunction<UsagePoint, Instant, Optional<ExecutableMicroCheckViolation>> onExecute) {
            this.onExecute = onExecute;
        }
    }
}
