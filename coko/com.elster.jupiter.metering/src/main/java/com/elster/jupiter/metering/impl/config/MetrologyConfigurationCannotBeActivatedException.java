/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;


public class MetrologyConfigurationCannotBeActivatedException extends LocalizedException {
    MetrologyConfigurationCannotBeActivatedException(Thesaurus thesaurus, MessageSeed messageSeed, String description) {
        super(thesaurus, messageSeed);
    }
}
