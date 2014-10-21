package com.elster.jupiter.time.rest.impl;

import com.elster.jupiter.time.RelativePeriodCategory;

import java.util.ArrayList;
import java.util.List;

public class RelativePeriodCategoryInfo {
    public Long id;
    public String name;

    public RelativePeriodCategoryInfo() {}

    public RelativePeriodCategoryInfo(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    static public List<RelativePeriodCategoryInfo> from (List<RelativePeriodCategory> categories) {
        List<RelativePeriodCategoryInfo> categoryInfos = new ArrayList<>();
        categories.stream().forEach(c -> categoryInfos.add(new RelativePeriodCategoryInfo(c.getId(), c.getName())));
        return categoryInfos;
    }
}
