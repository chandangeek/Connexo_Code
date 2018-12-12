package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class ReadingTypeAlreadyUsedOnMetrologyContract extends LocalizedException {

    public ReadingTypeAlreadyUsedOnMetrologyContract(Thesaurus thesaurus) {
        super(thesaurus, PrivateMessageSeeds.READING_TYPE_FOR_DELIVERABLE_ALREADY_USED_ON_CONTRACT);
    }

}