/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedFieldValidationException;

public class CannotStartCalendarBeforeNow extends LocalizedFieldValidationException {
    public CannotStartCalendarBeforeNow() {
        super(MessageSeeds.CANNOT_START_BEFORE_NOW, "fromTime");
    }
}