/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Created by kirkosh on 27.06.2016.
 */
public class UnsatisfiedMerologyConfigurationEndDateInThePast extends LocalizedException {
    public UnsatisfiedMerologyConfigurationEndDateInThePast(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.END_DATE_CANT_BE_IN_THE_PAST_FOR_CURRENT_METROLOGYCONFIGURATION_VERSION);
    }
}
