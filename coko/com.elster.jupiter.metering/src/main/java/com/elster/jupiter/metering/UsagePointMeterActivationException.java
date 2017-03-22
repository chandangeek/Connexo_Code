/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsagePointMeterActivationException extends LocalizedException {

    protected UsagePointMeterActivationException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static class MeterActiveOnDifferentUsagePoint extends UsagePointMeterActivationException {
        private Meter meter;
        private MeterRole currentRole;
        private MeterRole desiredRole;
        private UsagePoint meterCurrentUsagePoint;
        private Range<Instant> conflictActivationRange;

        private MeterActiveOnDifferentUsagePoint(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }

        // no usages -> no getters add if necessary
    }

    public static MeterActiveOnDifferentUsagePoint meterActiveOnDifferentUsagePoint(Thesaurus thesaurus, Meter meter,
                                                                                    MeterRole currentRole, MeterRole desiredRole,
                                                                                    UsagePoint meterCurrentUsagePoint,
                                                                                    Range<Instant> conflictActivationRange) {
        MeterActiveOnDifferentUsagePoint ex = new MeterActiveOnDifferentUsagePoint(thesaurus, MessageSeeds.METER_ALREADY_LINKED_TO_USAGEPOINT,
                meter.getName(), meterCurrentUsagePoint.getName(), currentRole.getDisplayName());
        ex.meter = meter;
        ex.currentRole = currentRole;
        ex.desiredRole = desiredRole;
        ex.meterCurrentUsagePoint = meterCurrentUsagePoint;
        ex.conflictActivationRange = conflictActivationRange;
        return ex;
    }

    public static class MeterActiveWithDifferentMeterRole extends UsagePointMeterActivationException {
        private Meter meter;
        private MeterRole currentRole;
        private MeterRole desiredRole;
        private Range<Instant> conflictActivationRange;

        private MeterActiveWithDifferentMeterRole(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }

        // no usages -> no getters add if necessary
    }

    public static MeterActiveWithDifferentMeterRole meterActiveWithDifferentMeterRole(Thesaurus thesaurus, Meter meter, MeterRole currentRole, MeterRole desiredRole, Range<Instant> conflictActivationRange) {
        MeterActiveWithDifferentMeterRole ex = new MeterActiveWithDifferentMeterRole(thesaurus, MessageSeeds.THE_SAME_METER_ACTIVATED_TWICE_ON_USAGE_POINT);
        ex.meter = meter;
        ex.currentRole = currentRole;
        ex.desiredRole = desiredRole;
        ex.conflictActivationRange = conflictActivationRange;
        return ex;
    }

    public static class UsagePointHasMeterOnThisRole extends UsagePointMeterActivationException {
        private Meter meterActiveOnRole;
        private MeterRole meterRole;
        private Range<Instant> conflictActivationRange;

        private UsagePointHasMeterOnThisRole(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }

        public Meter getMeter() {
            return this.meterActiveOnRole;
        }

        public MeterRole getMeterRole() {
            return this.meterRole;
        }

        public Range<Instant> getConflictActivationRange() {
            return this.conflictActivationRange;
        }

        // no usages -> no getters add if necessary
    }

    public static UsagePointHasMeterOnThisRole usagePointHasMeterOnThisRole(Thesaurus thesaurus, Meter meterActiveOnRole, MeterRole meterRole, Range<Instant> conflictActivationRange) {
        UsagePointHasMeterOnThisRole ex = new UsagePointHasMeterOnThisRole(thesaurus, MessageSeeds.USAGE_POINT_ALREADY_ACTIVE_WITH_GIVEN_ROLE, meterActiveOnRole.getName(), meterRole.getDisplayName());
        ex.meterActiveOnRole = meterActiveOnRole;
        ex.meterRole = meterRole;
        ex.conflictActivationRange = conflictActivationRange;
        return ex;
    }

    public static class MeterHasUnsatisfiedRequirements extends UsagePointMeterActivationException {
        private Meter meter;
        private MeterRole meterRole;
        private Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements;

        private MeterHasUnsatisfiedRequirements(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }

        public Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> getUnsatisfiedRequirements() {
            return this.unsatisfiedRequirements; // already unmodifiable
        }
    }

    public static MeterHasUnsatisfiedRequirements meterHasUnsatisfiedRequirements(Thesaurus thesaurus, Meter meter, MeterRole meterRole, Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements) {
        MeterHasUnsatisfiedRequirements ex = new MeterHasUnsatisfiedRequirements(thesaurus, MessageSeeds.UNSATISFIED_METROLOGY_REQUIREMENT,
                unsatisfiedRequirements.values().stream()
                        .flatMap(Collection::stream)
                        .map(ReadingTypeRequirement::getDescription)
                        .collect(Collectors.joining(", ")));
        ex.meter = meter;
        ex.meterRole = meterRole;
        ex.unsatisfiedRequirements = Collections.unmodifiableMap(unsatisfiedRequirements);
        return ex;
    }

    public static class ActivationWasFailedByCustomValidator extends UsagePointMeterActivationException {
        private Meter meter;
        private MeterRole meterRole;
        private UsagePoint usagePoint;
        private CustomUsagePointMeterActivationValidationException cause;

        private ActivationWasFailedByCustomValidator(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }

        // no usages -> no getters add if necessary
    }

    public static ActivationWasFailedByCustomValidator activationWasFailedByCustomValidator(Thesaurus thesaurus, Meter meter, MeterRole meterRole, UsagePoint usagePoint, CustomUsagePointMeterActivationValidationException cause) {
        ActivationWasFailedByCustomValidator ex = new ActivationWasFailedByCustomValidator(thesaurus, MessageSeeds.ACTIVATION_FAILED_BY_CUSTOM_VALIDATORS, cause.getLocalizedMessage());
        ex.meter = meter;
        ex.meterRole = meterRole;
        ex.usagePoint = usagePoint;
        ex.cause = cause;
        return ex;
    }

    public static class UsagePointIncorrectStage extends UsagePointMeterActivationException{
        public UsagePointIncorrectStage(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }
    }

    public static UsagePointIncorrectStage usagePointIncorrectStage(Thesaurus thesaurus){
        UsagePointIncorrectStage ex = new UsagePointIncorrectStage(thesaurus, MessageSeeds.USAGE_POINT_INCORRECT_STAGE);
        return ex;
    }
}
