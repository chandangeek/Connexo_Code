package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class CannotDeleteBecauseStillInUseException extends LocalizedException {

    private CannotDeleteBecauseStillInUseException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    public static CannotDeleteBecauseStillInUseException webServiceStillInUseException(Thesaurus thesaurus,
                                                                                       MessageSeed messageSeed) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, messageSeed);
    }
}