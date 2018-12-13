/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class MissingHandlerNameException extends LocalizedException {

    public MissingHandlerNameException(Thesaurus thesaurus, MessageSeed messageSeed, ServiceCallHandler serviceCallHandler) {
        super(thesaurus, messageSeed, serviceCallHandler.getClass().getSimpleName());
    }
}
