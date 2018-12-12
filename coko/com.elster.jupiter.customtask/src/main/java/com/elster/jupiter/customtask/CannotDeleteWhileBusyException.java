/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public abstract class CannotDeleteWhileBusyException extends LocalizedException {
    protected CannotDeleteWhileBusyException(Thesaurus thesaurus, MessageSeed messageSeed, CustomTask task) {
        super(thesaurus, messageSeed, task.getId());
    }
}
