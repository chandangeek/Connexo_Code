package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.inject.Inject;


public class TranslatableApplicationException extends LocalizedException {

    @Inject
    public TranslatableApplicationException(Thesaurus thesaurus, MessageSeed messageSeed, Object ...args) {
        super(thesaurus, messageSeed, args);
    }
}
