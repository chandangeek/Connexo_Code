/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.List;

public class UsagePointManagementException extends UsagePointMeterActivationException {
    public static UsagePointManagementException incorrectStage(Thesaurus thesaurus) {
        return new UsagePointManagementException(thesaurus, MessageSeeds.USAGE_POINT_INCORRECT_STAGE);
    }

    public static UsagePointManagementException incorrectMeterActivationRequirements(Thesaurus thesaurus, List<String> purposes) {
        return new UsagePointManagementException(thesaurus, MessageSeeds.METER_ACTIVATION_INVALID_REQUIREMENTS, purposes);
    }

    protected UsagePointManagementException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }
}