package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class CannotDeleteReadingTypeDeliverableException extends LocalizedException {

    public CannotDeleteReadingTypeDeliverableException(Thesaurus thesaurus, String deliverableName) {
        super(thesaurus, MessageSeeds.CAN_NOT_DELETE_READING_TYPE_DELIVERABLE_IN_USE, deliverableName);
    }
}
