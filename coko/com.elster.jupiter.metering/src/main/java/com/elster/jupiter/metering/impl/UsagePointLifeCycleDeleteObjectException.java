/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class UsagePointLifeCycleDeleteObjectException extends LocalizedException {

    public UsagePointLifeCycleDeleteObjectException(Thesaurus thesaurus, MessageSeed seed, Object... args) {
        super(thesaurus, seed, args);
    }

    public static UsagePointLifeCycleDeleteObjectException canNotDeleteActiveLifeCycle(Thesaurus thesaurus) {
        return new UsagePointLifeCycleDeleteObjectException(thesaurus, PrivateMessageSeeds.CAN_NOT_DELETE_ACTIVE_LIFE_CYCLE);
    }

    public static UsagePointLifeCycleDeleteObjectException canNotDeleteActiveState(Thesaurus thesaurus) {
        return new UsagePointLifeCycleDeleteObjectException(thesaurus, PrivateMessageSeeds.CAN_NOT_DELETE_ACTIVE_STATE);
    }
}
