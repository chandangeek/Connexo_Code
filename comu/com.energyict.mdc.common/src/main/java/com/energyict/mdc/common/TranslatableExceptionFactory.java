package com.energyict.mdc.common;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.Provider;
import javax.inject.Inject;

public class TranslatableExceptionFactory implements TranslatableExceptionCreator, TranslatableException {

    private Provider<Thesaurus> thesaurusProvider;

    @Inject
    public TranslatableExceptionFactory(Provider<Thesaurus> thesaurusProvider) {
        this.thesaurusProvider = thesaurusProvider;
    }

    @Override
    public TranslatableApplicationException create(MessageSeed messageSeed) {
        return new TranslatableApplicationException(thesaurusProvider.get(), messageSeed);
    }
}
