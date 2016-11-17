package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckViolation;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class AllDataValidCheck extends TranslatableCheck {
    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getCategory() {
        return MicroCategory.VALIDATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(UsagePoint usagePoint, Instant transitionTime, Map<String, Object> properties) {
        return null;
    }
}
