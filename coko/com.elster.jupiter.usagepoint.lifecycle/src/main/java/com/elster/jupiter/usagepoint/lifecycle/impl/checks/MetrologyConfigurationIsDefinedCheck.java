/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckViolation;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultTransition;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class MetrologyConfigurationIsDefinedCheck extends TranslatableCheck {

    @Override
    public String getCategory() {
        return MicroCategory.INSTALLATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(UsagePoint usagePoint, Instant transitionTime) {
        if (!usagePoint.getEffectiveMetrologyConfiguration(transitionTime).isPresent()) {
            return fail(MicroCheckTranslationKeys.METROLOGY_CONF_IS_DEFINED_MESSAGE);
        }
        return Optional.empty();
    }

    @Override
    protected Set<DefaultTransition> getTransitionCandidates() {
        return EnumSet.of(DefaultTransition.INSTALL_ACTIVE, DefaultTransition.INSTALL_INACTIVE);
    }
}
