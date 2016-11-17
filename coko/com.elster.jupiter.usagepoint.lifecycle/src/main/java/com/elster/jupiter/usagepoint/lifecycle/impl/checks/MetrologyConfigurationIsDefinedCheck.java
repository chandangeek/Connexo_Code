package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckViolation;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

import java.time.Instant;
import java.util.Optional;

public class MetrologyConfigurationIsDefinedCheck extends TranslatableCheck {

    @Override
    public String getCategory() {
        return MicroCategory.INSTALLATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(UsagePoint usagePoint, Instant transitionTime) {
        return Optional.empty();
    }
}
