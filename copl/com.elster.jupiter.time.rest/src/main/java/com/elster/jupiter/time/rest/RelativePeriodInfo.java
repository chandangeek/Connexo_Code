/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest;

import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.rest.impl.RelativePeriodCategoryInfo;

import aQute.bnd.annotation.ProviderType;

import java.util.ArrayList;
import java.util.List;

@ProviderType
public class RelativePeriodInfo {
    public Long id;
    public String name;
    public RelativeDateInfo from;
    public RelativeDateInfo to;
    public List<RelativePeriodCategoryInfo> categories = new ArrayList<>();
    public long version;

    public RelativePeriodInfo() {}

    public static RelativePeriodInfo from(RelativePeriod relativePeriod) {
        RelativePeriodInfo info = new RelativePeriodInfo();
        info.id = relativePeriod.getId();
        info.name = relativePeriod.getName();
        info.version = relativePeriod.getVersion();
        return info;
    }

    public static RelativePeriodInfo withCategories(RelativePeriod relativePeriod) {
        RelativePeriodInfo info = RelativePeriodInfo.from(relativePeriod);
        info.from = new RelativeDateInfo(relativePeriod.getRelativeDateFrom());
        info.to = new RelativeDateInfo(relativePeriod.getRelativeDateTo());
        info.categories = RelativePeriodCategoryInfo.from(relativePeriod.getRelativePeriodCategories());
        return info;
    }

}