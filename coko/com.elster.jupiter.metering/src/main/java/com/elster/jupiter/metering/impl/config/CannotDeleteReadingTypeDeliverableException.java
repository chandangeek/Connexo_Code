/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class CannotDeleteReadingTypeDeliverableException extends LocalizedException {

    public CannotDeleteReadingTypeDeliverableException(Thesaurus thesaurus, String deliverableName) {
        super(thesaurus, PrivateMessageSeeds.CAN_NOT_DELETE_READING_TYPE_DELIVERABLE_IN_USE, deliverableName);
    }

}