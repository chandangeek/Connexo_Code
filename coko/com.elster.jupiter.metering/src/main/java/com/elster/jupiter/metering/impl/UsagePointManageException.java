package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class UsagePointManageException extends LocalizedException {

    private UsagePointManageException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static UsagePointManageException incorrectState(Thesaurus thesaurus, String usagePointMrid) {
        return new UsagePointManageException(thesaurus, MessageSeeds.USAGE_POINT_INCORRECT_STATE, usagePointMrid);
    }
}
