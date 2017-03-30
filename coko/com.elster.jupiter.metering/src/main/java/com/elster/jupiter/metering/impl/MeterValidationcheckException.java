/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Created by akuryuk on 25.04.2016.
 */
public class MeterValidationcheckException extends LocalizedException {
    MeterValidationcheckException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }
}
