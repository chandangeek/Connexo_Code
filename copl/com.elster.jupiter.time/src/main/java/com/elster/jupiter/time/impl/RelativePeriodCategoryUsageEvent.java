/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import com.elster.jupiter.time.RelativePeriodCategoryUsage;

public class RelativePeriodCategoryUsageEvent {

    private RelativePeriodCategoryUsage usage;

    private long relativePeriodId;
    private long relativePeriodCategoryId;

    public RelativePeriodCategoryUsageEvent() {
    }

    RelativePeriodCategoryUsageEvent(RelativePeriodCategoryUsage usage) {
        this.relativePeriodId = usage.getRelativePeriod().getId();
        this.relativePeriodCategoryId = usage.getRelativePeriodCategory().getId();
    }

    public RelativePeriodCategoryUsage getUsage() {
        return usage;
    }

    public void setRelativePeriodId(long relativePeriodId) {
        this.relativePeriodId = relativePeriodId;
    }

    public void setRelativePeriodCategoryId(long relativePeriodCategoryId) {
        this.relativePeriodCategoryId = relativePeriodCategoryId;
    }

    public long getRelativePeriodId() {
        return relativePeriodId;
    }

    public long getRelativePeriodCategoryId() {
        return relativePeriodCategoryId;
    }
}
