package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class UnsatisfiedMetrologyConfigurationEndDate extends LocalizedException {

    public UnsatisfiedMetrologyConfigurationEndDate(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.END_DATE_MUST_BE_GREATER_THAN_START_DATE);
    }

}
