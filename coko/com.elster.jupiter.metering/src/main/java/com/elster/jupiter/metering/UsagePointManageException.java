package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class UsagePointManageException extends UsagePointMeterActivationException {

    private UsagePointManageException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static UsagePointManageException incorrectStage(Thesaurus thesaurus) {
        return new UsagePointManageException(thesaurus, MessageSeeds.USAGE_POINT_INCORRECT_STAGE);
    }
}
