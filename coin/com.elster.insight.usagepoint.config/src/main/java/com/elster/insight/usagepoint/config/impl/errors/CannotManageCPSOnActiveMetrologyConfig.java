package com.elster.insight.usagepoint.config.impl.errors;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class CannotManageCPSOnActiveMetrologyConfig extends LocalizedException {
    public CannotManageCPSOnActiveMetrologyConfig(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.FAIL_MANAGE_CPS_ON_ACTIVE_M_CONFIG);
    }
}
