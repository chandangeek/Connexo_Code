/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedFieldValidationException;

public class CannotStartCalendarPriorToLatest extends LocalizedFieldValidationException {
    public CannotStartCalendarPriorToLatest() {
        super(MessageSeeds.CANNOT_START_PRIOR_TO_LATEST_CALENDAR_OF_SAME_CATEGORY, "fromTime");
    }
}