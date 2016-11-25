package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class VetoDeleteUsagePointGroupException extends LocalizedException {
    public VetoDeleteUsagePointGroupException(Thesaurus thesaurus, UsagePointGroup group) {
        super(thesaurus, MessageSeeds.VETO_USAGEPOINTGROUP_DELETION, group.getName());
    }
}