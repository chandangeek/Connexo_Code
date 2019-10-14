package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class WebElementFactoryNotFound extends LocalizedException {

    public WebElementFactoryNotFound(final Thesaurus thesaurus, final Object... args) {
        super(thesaurus, MessageSeeds.WEB_ELEMENT_FACTORY_NOT_FOUND, args);
    }
}
