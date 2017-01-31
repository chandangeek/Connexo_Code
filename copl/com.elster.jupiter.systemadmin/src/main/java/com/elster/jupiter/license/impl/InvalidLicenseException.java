/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.license.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class InvalidLicenseException extends LocalizedException {

    public InvalidLicenseException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    public InvalidLicenseException(Thesaurus thesaurus, Throwable cause) {
        super(thesaurus, MessageSeeds.INVALID_LICENSE, cause);
    }
}