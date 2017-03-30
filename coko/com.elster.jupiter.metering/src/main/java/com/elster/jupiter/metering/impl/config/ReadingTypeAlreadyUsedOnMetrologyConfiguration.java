/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Created by igh on 13/04/2016.
 */
public class ReadingTypeAlreadyUsedOnMetrologyConfiguration extends LocalizedException {

    public ReadingTypeAlreadyUsedOnMetrologyConfiguration(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.READING_TYPE_FOR_DELIVERABLE_ALREADY_USED);
    }

}
