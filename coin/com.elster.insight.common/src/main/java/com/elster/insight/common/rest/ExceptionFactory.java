package com.elster.insight.common.rest;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.inject.Inject;
import java.util.function.Supplier;

/**
 * Created by bvn on 6/23/14.
 */
public class ExceptionFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ExceptionFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public LocalizedException newException(MessageSeed messageSeed) {
        return new RestException(thesaurus, messageSeed);
    }

    public LocalizedException newException(MessageSeed messageSeed, Object... args) {
        return new RestException(thesaurus, messageSeed, args);
    }

    public Supplier<LocalizedException> newExceptionSupplier(MessageSeed messageSeed, Object... args) {
        return () -> new RestException(thesaurus, messageSeed, args);
    }

    class RestException extends LocalizedException {

        public RestException(Thesaurus thesaurus, MessageSeed messageSeed) {
            super(thesaurus, messageSeed);
        }

        public RestException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }


    }
}
