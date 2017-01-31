/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class UsagePointLifeCycleRemoveException extends LocalizedException {
    private UsagePointLifeCycleRemoveException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static UsagePointLifeCycleRemoveException lifeCycleIsDefault(Thesaurus thesaurus) {
        return new UsagePointLifeCycleRemoveException(thesaurus, MessageSeeds.CAN_NOT_REMOVE_DEFAULT_LIFE_CYCLE);
    }
}
