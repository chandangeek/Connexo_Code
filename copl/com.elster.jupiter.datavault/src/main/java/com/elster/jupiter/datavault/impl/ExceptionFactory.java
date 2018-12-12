/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import javax.inject.Inject;

/**
 * Created by bvn on 9/16/14.
 */
public class ExceptionFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ExceptionFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public LocalizedException newException(MessageSeed messageSeed, Object... args) {
        return new ApplicationException(thesaurus, messageSeed, args);
    }


    class ApplicationException extends LocalizedException {

        protected ApplicationException(Thesaurus thesaurus, MessageSeed messageSeed) {
            super(thesaurus, messageSeed);
        }

        protected ApplicationException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }
    }


}
