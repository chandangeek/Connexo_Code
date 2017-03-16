/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.MessageSeeds;

class ForbiddenException extends LocalizedException {

    ForbiddenException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.INSUFFICIENT_PRIVILEGES);
    }
}
