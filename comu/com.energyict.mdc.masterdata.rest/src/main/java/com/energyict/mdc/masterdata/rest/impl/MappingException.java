/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

class MappingException extends LocalizedException {

    MappingException(Thesaurus thesaurus, MessageSeed msg) {
        super(thesaurus, msg);
    }
}
