/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class LifeCycleIsStillInUseException extends LocalizedException {

    public LifeCycleIsStillInUseException(Thesaurus thesaurus, MessageSeed messageSeed, ServiceCallLifeCycle serviceCallLifeCycle) {
        super(thesaurus, messageSeed, serviceCallLifeCycle.getName());
    }
}
