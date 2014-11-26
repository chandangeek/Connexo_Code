package com.elster.jupiter.export.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class NoSuchDataProcessor extends LocalizedException {
    NoSuchDataProcessor(Thesaurus thesaurus, String processorName) {
        super(thesaurus, MessageSeeds.NO_SUCH_PROCESSOR, processorName);
    }
}
