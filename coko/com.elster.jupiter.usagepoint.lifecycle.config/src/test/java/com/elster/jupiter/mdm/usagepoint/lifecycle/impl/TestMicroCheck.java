package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointMicroCheckFactory;

import java.util.Optional;

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

    public static class Factory implements UsagePointMicroCheckFactory {

        @Override
        public Optional<MicroCheck> from(String microActionKey) {
            return Optional.of(new TestMicroCheck());
        }
    }
}
