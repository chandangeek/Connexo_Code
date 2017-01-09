package com.elster.jupiter.metering;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * @deprecated Use {@link UsagePointManagementException}
 */
@Deprecated
public class UsagePointManageException extends LocalizedException {

    private UsagePointManageException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static UsagePointManageException incorrectState(Thesaurus thesaurus, String usagePointName) {
        return new UsagePointManageException(thesaurus, MessageSeeds.USAGE_POINT_INCORRECT_STATE, usagePointName);
    }
}