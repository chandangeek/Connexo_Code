package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class VetoDeleteUsagePointGroupException extends LocalizedException {
    public VetoDeleteUsagePointGroupException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.GROUP_IS_USED_BY_ANOTHER_GROUP);
    }
}