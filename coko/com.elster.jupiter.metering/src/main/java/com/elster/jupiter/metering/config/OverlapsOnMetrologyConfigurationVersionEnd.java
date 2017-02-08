/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class OverlapsOnMetrologyConfigurationVersionEnd extends LocalizedException {
    public OverlapsOnMetrologyConfigurationVersionEnd(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.THIS_DATE_IS_OVERLAPPED_BY_OTHER_METROLOGYCONFIGURATION_VERSION);
    }
}
