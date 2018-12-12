/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class NoSuchDataFormatter extends LocalizedException {
    NoSuchDataFormatter(Thesaurus thesaurus, String formatterName) {
        super(thesaurus, MessageSeeds.NO_SUCH_FORMATTER, formatterName);
    }
}
