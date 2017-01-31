/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class UsagePointManagementException extends UsagePointMeterActivationException {

    private UsagePointManagementException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static UsagePointManagementException incorrectStage(Thesaurus thesaurus) {
        return new UsagePointManagementException(thesaurus, MessageSeeds.USAGE_POINT_INCORRECT_STAGE);
    }
}
