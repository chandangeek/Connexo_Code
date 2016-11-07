package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class CannotDeleteUsagePointLifeCycleException extends LocalizedException {

    public CannotDeleteUsagePointLifeCycleException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.CAN_NOT_DELETE_ACTIVE_LIFE_CYCLE);
    }
}
