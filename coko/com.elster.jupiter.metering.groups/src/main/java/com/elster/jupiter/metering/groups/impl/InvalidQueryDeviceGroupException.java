package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class InvalidQueryDeviceGroupException extends LocalizedException {

    public InvalidQueryDeviceGroupException(Thesaurus thesaurus, MessageSeeds messageSeed, Throwable throwable, Object... args) {
        super(thesaurus, messageSeed, throwable, args);
    }

    public InvalidQueryDeviceGroupException(Thesaurus thesaurus, MessageSeeds messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }
}
