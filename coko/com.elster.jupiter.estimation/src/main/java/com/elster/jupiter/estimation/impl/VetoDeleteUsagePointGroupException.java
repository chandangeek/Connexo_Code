/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class VetoDeleteUsagePointGroupException extends LocalizedException {
    public VetoDeleteUsagePointGroupException(Thesaurus thesaurus, UsagePointGroup group) {
        super(thesaurus, MessageSeeds.VETO_USAGEPOINTGROUP_DELETION, group.getName());
    }
}