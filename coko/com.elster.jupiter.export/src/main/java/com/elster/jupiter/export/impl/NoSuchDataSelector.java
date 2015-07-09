package com.elster.jupiter.export.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class NoSuchDataSelector extends LocalizedException {
    NoSuchDataSelector(Thesaurus thesaurus, String formatterName) {
        super(thesaurus, MessageSeeds.NO_SUCH_SELECTOR, formatterName);
    }
}
