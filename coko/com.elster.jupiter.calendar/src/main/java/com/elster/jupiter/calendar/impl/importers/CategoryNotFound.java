/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class CategoryNotFound extends LocalizedException {
    CategoryNotFound(Thesaurus thesaurus, String categoryName) {
        super(thesaurus, MessageSeeds.CATEGORY_NOT_FOUND, categoryName);
    }
}
