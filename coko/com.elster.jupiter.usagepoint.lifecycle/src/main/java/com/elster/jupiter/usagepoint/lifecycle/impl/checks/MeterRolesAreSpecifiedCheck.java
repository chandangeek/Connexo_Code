/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckViolation;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultTransition;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MeterRolesAreSpecifiedCheck extends TranslatableCheck {

    @Override
    public String getCategory() {
        return MicroCategory.INSTALLATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(UsagePoint usagePoint, Instant transitionTime) {
        Map<MeterRole, MeterActivation> activationsPerRole = usagePoint.getMeterActivations(transitionTime)
                .stream()
                .collect(Collectors.toMap(ma -> ma.getMeterRole().get(), Function.identity()));
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> emc = usagePoint.getEffectiveMetrologyConfiguration(transitionTime);
        if (emc.isPresent() && !emc.get().getMetrologyConfiguration().getMeterRoles()
                .stream()
                .map(activationsPerRole::get)
                .noneMatch(Objects::isNull)) {
            return fail(MicroCheckTranslationKeys.METER_ROLES_ARE_SPECIFIED_MESSAGE);
        }
        return Optional.empty();
    }

    @Override
    protected Set<DefaultTransition> getTransitionCandidates() {
        return EnumSet.of(DefaultTransition.INSTALL_ACTIVE, DefaultTransition.INSTALL_INACTIVE);
    }
}
