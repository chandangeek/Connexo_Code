/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest.impl;

import com.elster.jupiter.time.RelativePeriodCategory;

import java.util.List;
import java.util.stream.Collectors;

public class RelativePeriodCategoryInfo {
    public Long id;
    public String name;

    public RelativePeriodCategoryInfo() {}

    private RelativePeriodCategoryInfo(Long id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public RelativePeriodCategoryInfo(RelativePeriodCategory category) {
        this(category.getId(), category.getDisplayName());
    }

    public static List<RelativePeriodCategoryInfo> from(List<RelativePeriodCategory> categories) {
        return categories.stream().map(RelativePeriodCategoryInfo::new).collect(Collectors.toList());
    }

}