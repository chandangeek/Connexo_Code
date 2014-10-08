package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class NotUniqueKeyException extends LocalizedException {

    public NotUniqueKeyException(Thesaurus thesaurus, Object... args) {
        super(thesaurus, MessageSeeds.NOT_UNIQUE_KEY, args);
    }
}
