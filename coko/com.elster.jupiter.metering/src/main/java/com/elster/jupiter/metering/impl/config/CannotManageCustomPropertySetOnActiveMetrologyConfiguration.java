/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class CannotManageCustomPropertySetOnActiveMetrologyConfiguration extends LocalizedException {

    public CannotManageCustomPropertySetOnActiveMetrologyConfiguration(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.FAIL_MANAGE_CPS_ON_ACTIVE_METROLOGY_CONFIGURATION);
    }

}