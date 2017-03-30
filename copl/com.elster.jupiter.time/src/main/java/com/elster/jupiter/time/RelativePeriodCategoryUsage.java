/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface RelativePeriodCategoryUsage {
    RelativePeriod getRelativePeriod();

    RelativePeriodCategory getRelativePeriodCategory();

    boolean sameRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory);
}