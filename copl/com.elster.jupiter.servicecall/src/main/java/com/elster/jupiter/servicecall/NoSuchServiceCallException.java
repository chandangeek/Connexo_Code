/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class NoSuchServiceCallException extends LocalizedException {

    public NoSuchServiceCallException(Thesaurus thesaurus, MessageSeed messageSeed, long serviceCall) {
        super(thesaurus, messageSeed, serviceCall);
    }
}
