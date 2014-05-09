package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import javax.inject.Inject;

public class ComScheduleExceptionFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ComScheduleExceptionFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public LocalizedException createCanNotAddComTaskToComScheduleException() {
        return new CanNotAddComTaskToComScheduleException(thesaurus, MessageSeeds.COM_TASK_NOT_ENABLED);
    }

    private class CanNotAddComTaskToComScheduleException extends LocalizedException {

        protected CanNotAddComTaskToComScheduleException(Thesaurus thesaurus, MessageSeed messageSeed) {
            super(thesaurus, messageSeed);
        }
    }
}
