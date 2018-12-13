/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class UsagePointCustomPropertySetVersionIncorrectStartDateException extends LocalizedException {

    public UsagePointCustomPropertySetVersionIncorrectStartDateException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

}
