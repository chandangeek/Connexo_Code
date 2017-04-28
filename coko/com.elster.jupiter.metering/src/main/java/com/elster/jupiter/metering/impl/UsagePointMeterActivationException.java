/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;

import java.time.Instant;

public class UsagePointMeterActivationException extends com.elster.jupiter.metering.UsagePointMeterActivationException {

    protected UsagePointMeterActivationException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    protected UsagePointMeterActivationException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause, Object... args) {
        super(thesaurus, messageSeed, cause, args);
    }

    public static class MeterActiveOnDifferentUsagePoint extends UsagePointMeterActivationException {

        private MeterActiveOnDifferentUsagePoint(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }

    }

    public static MeterActiveOnDifferentUsagePoint meterActiveOnDifferentUsagePoint(Thesaurus thesaurus, Meter meter,
            MeterRole currentRole, MeterRole desiredRole,
            UsagePoint meterCurrentUsagePoint,
            Range<Instant> conflictActivationRange) {
        return new MeterActiveOnDifferentUsagePoint(
                thesaurus,
                PrivateMessageSeeds.METER_ALREADY_LINKED_TO_USAGEPOINT,
                meter.getName(),
                meterCurrentUsagePoint.getName(),
                currentRole.getDisplayName());
    }

    public static class MeterActiveWithDifferentMeterRole extends UsagePointMeterActivationException {

        private MeterActiveWithDifferentMeterRole(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }

    }

    public static MeterActiveWithDifferentMeterRole meterActiveWithDifferentMeterRole(Thesaurus thesaurus, Meter meter, MeterRole currentRole, MeterRole desiredRole, Range<Instant> conflictActivationRange) {
        return new MeterActiveWithDifferentMeterRole(thesaurus, PrivateMessageSeeds.THE_SAME_METER_ACTIVATED_TWICE_ON_USAGE_POINT);
    }

    public static MeterCannotBeUnlinked meterCannotBeUnlinked(Thesaurus thesaurus, Meter meter, MeterRole meterRole, UsagePoint usagePoint, String date) {
        return new MeterCannotBeUnlinked(thesaurus, meter.getName(), usagePoint.getName(), date);
    }

    public static IncorrectStartTimeOfMeterAndMetrologyConfig incorrectStartTimeOfMeterAndMetrologyConfig(Thesaurus thesaurus, Meter meter, MeterRole meterRole,  String date) {
        return new IncorrectStartTimeOfMeterAndMetrologyConfig(thesaurus, meter.getName(), date);
    }

    public static class ActivationFailedByCustomValidator extends UsagePointMeterActivationException {
        private ActivationFailedByCustomValidator(Thesaurus thesaurus, MessageSeed messageSeed, CustomUsagePointMeterActivationValidationException cause, Object... args) {
            super(thesaurus, messageSeed, cause, args);
        }
    }

    public static ActivationFailedByCustomValidator activationFailedByCustomValidator(Thesaurus thesaurus, CustomUsagePointMeterActivationValidationException cause) {
        return new ActivationFailedByCustomValidator(thesaurus, PrivateMessageSeeds.ACTIVATION_FAILED_BY_CUSTOM_VALIDATORS, cause, cause.getLocalizedMessage());
    }

    public static class ActivationTimeBeforeUsagePointInstallationDate extends UsagePointMeterActivationException {
        public ActivationTimeBeforeUsagePointInstallationDate(Thesaurus thesaurus, String usagePointInstallationTime) {
            super(thesaurus, PrivateMessageSeeds.METER_ACTIVATION_BEFORE_UP_INSTALLATION_TIME, usagePointInstallationTime);
        }
    }

    public static class IncorrectDeviceStageWithoutMetrologyConfig extends UsagePointMeterActivationException {
        public IncorrectDeviceStageWithoutMetrologyConfig(Thesaurus thesaurus, String meter, String usagePoint, String date) {
            super(thesaurus, PrivateMessageSeeds.INVALID_END_DEVICE_STAGE, meter, usagePoint, date);
        }
    }

    public static class IncorrectStartTimeOfMeterAndMetrologyConfig extends UsagePointMeterActivationException {
        public IncorrectStartTimeOfMeterAndMetrologyConfig(Thesaurus thesaurus, String meter, String mcStartDate) {
            super(thesaurus, PrivateMessageSeeds.METER_ACTIVATION_INVALID_DATE, meter, mcStartDate);
        }
    }

    public static class IncorrectMeterActivationDateWhenGapsAreAllowed extends UsagePointMeterActivationException {
        public IncorrectMeterActivationDateWhenGapsAreAllowed(Thesaurus thesaurus, String meter, String usagePoint) {
            super(thesaurus, PrivateMessageSeeds.INVALID_END_DEVICE_STAGE_WITH_GAPS_ALLOWED, meter, usagePoint);
        }
    }

    public static class MeterCannotBeUnlinked extends UsagePointMeterActivationException {
        public MeterCannotBeUnlinked(Thesaurus thesaurus, String meter, String usagePoint, String date) {
            super(thesaurus, PrivateMessageSeeds.METER_CANNOT_BE_UNLINKED, meter, usagePoint, date);
        }
    }

}