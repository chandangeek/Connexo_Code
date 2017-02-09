/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class UnsatisfiedMerologyConfigurationStartDateRelativelyLatestEnd extends LocalizedException {

    public UnsatisfiedMerologyConfigurationStartDateRelativelyLatestEnd(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.START_DATE_SHOULD_BE_GREATER_THAN_LATEST_END_DATE);
    }

}
