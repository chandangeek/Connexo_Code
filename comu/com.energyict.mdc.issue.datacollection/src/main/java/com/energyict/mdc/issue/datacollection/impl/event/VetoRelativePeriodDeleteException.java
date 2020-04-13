package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriod;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

public class VetoRelativePeriodDeleteException extends LocalizedException {
    public VetoRelativePeriodDeleteException(Thesaurus thesaurus, RelativePeriod relativePeriod) {
        super(thesaurus, MessageSeeds.RELATIVE_PERIOD_IN_USE, relativePeriod.getName());
    }
}
