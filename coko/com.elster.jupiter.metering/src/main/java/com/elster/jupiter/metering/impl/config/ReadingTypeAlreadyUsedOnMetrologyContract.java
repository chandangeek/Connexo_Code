package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class ReadingTypeAlreadyUsedOnMetrologyContract extends LocalizedException {

    public ReadingTypeAlreadyUsedOnMetrologyContract(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.READING_TYPE_FOR_DELIVERABLE_ALREADY_USED_ON_CONTRACT);
    }

}
