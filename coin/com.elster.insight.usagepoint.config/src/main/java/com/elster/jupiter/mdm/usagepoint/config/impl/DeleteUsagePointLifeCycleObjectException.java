/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class DeleteUsagePointLifeCycleObjectException extends LocalizedException {

    private DeleteUsagePointLifeCycleObjectException(Thesaurus thesaurus, MessageSeed seed, Object... args) {
        super(thesaurus, seed, args);
    }

    public static DeleteUsagePointLifeCycleObjectException canNotDeleteActiveLifeCycle(Thesaurus thesaurus) {
        return new DeleteUsagePointLifeCycleObjectException(thesaurus, MessageSeeds.CAN_NOT_DELETE_ACTIVE_LIFE_CYCLE);
    }

    public static DeleteUsagePointLifeCycleObjectException canNotDeleteActiveState(Thesaurus thesaurus) {
        return new DeleteUsagePointLifeCycleObjectException(thesaurus, MessageSeeds.CAN_NOT_DELETE_ACTIVE_STATE);
    }
}
