/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class CannotDeactivateMetrologyConfiguration extends LocalizedException {

    public CannotDeactivateMetrologyConfiguration(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.FAILED_TO_DEACTIVATE_METROLOGY_CONFIGURATION);
    }

}
