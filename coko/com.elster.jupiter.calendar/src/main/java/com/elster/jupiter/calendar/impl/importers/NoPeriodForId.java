/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class NoPeriodForId extends LocalizedException {
    NoPeriodForId(Thesaurus thesaurus, int id) {
        super(thesaurus, MessageSeeds.NO_PERIOD_DEFINED_WITH_ID, id);
    }
}
