/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.RelativePeriodCategoryUsage;

import javax.inject.Inject;
import java.time.Instant;

class RelativePeriodCategoryUsageImpl implements RelativePeriodCategoryUsage {

    private Reference<RelativePeriod> relativePeriod = ValueReference.absent();
    private Reference<RelativePeriodCategory> relativePeriodCategory = ValueReference.absent();
    // Audit fields
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Inject
    RelativePeriodCategoryUsageImpl() {
        super();
    }

    RelativePeriodCategoryUsageImpl(RelativePeriod relativePeriod, RelativePeriodCategory relativePeriodCategory) {
        this();
        this.relativePeriod.set(relativePeriod);
        this.relativePeriodCategory.set(relativePeriodCategory);
    }

    @Override
    public RelativePeriod getRelativePeriod() {
        return relativePeriod.get();
    }

    @Override
    public RelativePeriodCategory getRelativePeriodCategory() {
        return relativePeriodCategory.get();
    }

    @Override
    public boolean sameRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory) {
        return this.getRelativePeriodCategory().getId() == relativePeriodCategory.getId();
    }
}