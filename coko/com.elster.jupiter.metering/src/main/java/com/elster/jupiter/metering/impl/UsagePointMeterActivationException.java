/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class UsagePointMeterActivationException extends LocalizedException {

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

    public static MeterActiveOnDifferentUsagePoint meterActiveOnDifferentUsagePoint(Thesaurus thesaurus, Meter meter, MeterRole currentRole, UsagePoint meterCurrentUsagePoint) {
        return new MeterActiveOnDifferentUsagePoint(
                thesaurus, PrivateMessageSeeds.METER_ALREADY_LINKED_TO_USAGEPOINT,
                meter.getName(), meterCurrentUsagePoint.getName(), currentRole.getDisplayName());
    }

    public static class MeterActiveWithDifferentMeterRole extends UsagePointMeterActivationException {

        private MeterActiveWithDifferentMeterRole(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }

    }

    public static MeterActiveWithDifferentMeterRole meterActiveWithDifferentMeterRole(Thesaurus thesaurus) {
        return new MeterActiveWithDifferentMeterRole(thesaurus, PrivateMessageSeeds.THE_SAME_METER_ACTIVATED_TWICE_ON_USAGE_POINT);
    }

    public static class ActivationFailedByCustomValidator extends UsagePointMeterActivationException {

        private ActivationFailedByCustomValidator(Thesaurus thesaurus, MessageSeed messageSeed, CustomUsagePointMeterActivationValidationException cause, Object... args) {
            super(thesaurus, messageSeed, cause, args);
        }

    }

    public static ActivationFailedByCustomValidator activationFailedByCustomValidator(Thesaurus thesaurus, CustomUsagePointMeterActivationValidationException cause) {
        return new ActivationFailedByCustomValidator(thesaurus, PrivateMessageSeeds.ACTIVATION_FAILED_BY_CUSTOM_VALIDATORS, cause, cause.getLocalizedMessage());
    }

}