package com.elster.jupiter.time.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.rest.impl.RelativePeriodCategoryInfo;

import java.util.ArrayList;
import java.util.List;

public class RelativePeriodInfo {
    public Long id;
    public String name;
    public RelativeDateInfo from;
    public RelativeDateInfo to;
    public List<RelativePeriodCategoryInfo> categories = new ArrayList<>();

    public RelativePeriodInfo() {

    }

    public RelativePeriodInfo(RelativePeriod relativePeriod) {
        this.id = relativePeriod.getId();
        this.name = relativePeriod.getName();
    }

    public RelativePeriodInfo(RelativePeriod relativePeriod, Thesaurus thesaurus) {
        this.id = relativePeriod.getId();
        this.name = relativePeriod.getName();
        this.from = new RelativeDateInfo(relativePeriod.getRelativeDateFrom());
        this.to = new RelativeDateInfo(relativePeriod.getRelativeDateTo());
        this.categories = RelativePeriodCategoryInfo.from(relativePeriod.getRelativePeriodCategories(), thesaurus);
    }

    public RelativePeriodInfo(Long id, String name, RelativeDateInfo from, RelativeDateInfo to, List<RelativePeriodCategoryInfo> categories) {
        this.id = id;
        this.name = name;
        this.from = from;
        this.to = to;
        this.categories = categories;
    }


}
