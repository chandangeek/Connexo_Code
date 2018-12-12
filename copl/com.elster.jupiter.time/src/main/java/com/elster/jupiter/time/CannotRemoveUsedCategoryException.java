package com.elster.jupiter.time;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class CannotRemoveUsedCategoryException extends LocalizedException {

    public CannotRemoveUsedCategoryException(String category, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, category);
    }
}
