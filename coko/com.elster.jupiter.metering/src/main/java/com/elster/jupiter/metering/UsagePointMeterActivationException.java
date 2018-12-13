/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class UsagePointMeterActivationException extends LocalizedException {

    protected UsagePointMeterActivationException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static class IncorrectLifeCycleStage extends UsagePointMeterActivationException {
        public IncorrectLifeCycleStage(Thesaurus thesaurus, String meter, String usagePoint, String date) {
            super(thesaurus, MessageSeeds.INVALID_END_DEVICE_STAGE_WITHOUT_MC, meter, usagePoint, date);
        }
    }

    public static class MeterCannotBeUnlinked extends UsagePointMeterActivationException {
        public MeterCannotBeUnlinked(Thesaurus thesaurus, String meter, String usagePoint, String date) {
            super(thesaurus, MessageSeeds.METER_CANNOT_BE_UNLINKED, meter, usagePoint, date);
        }
    }

    public static class UsagePointIncorrectStage extends UsagePointMeterActivationException{
        public UsagePointIncorrectStage(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }
    }

    public static UsagePointIncorrectStage usagePointIncorrectStage(Thesaurus thesaurus){
        return new UsagePointIncorrectStage(thesaurus, MessageSeeds.USAGE_POINT_INCORRECT_STAGE);
    }
}
