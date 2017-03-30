/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Created by david on 8/12/2016.
 */
public class CannotDeleteUsedRelativePeriodException extends LocalizedException {

    public CannotDeleteUsedRelativePeriodException(RelativePeriod relativePeriod, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, relativePeriod.getName());
    }
}
