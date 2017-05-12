/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.event;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriod;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;

public class VetoRelativePeriodDeleteException extends LocalizedException {
    public VetoRelativePeriodDeleteException(Thesaurus thesaurus, RelativePeriod relativePeriod) {
        super(thesaurus, MessageSeeds.RELATIVE_PERIOD_IN_USE, relativePeriod.getName());
    }
}
