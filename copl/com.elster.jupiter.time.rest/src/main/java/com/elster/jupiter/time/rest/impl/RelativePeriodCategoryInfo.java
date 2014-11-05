package com.elster.jupiter.time.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriodCategory;

import java.util.ArrayList;
import java.util.List;

public class RelativePeriodCategoryInfo {
    public Long id;
    public String name;

    public RelativePeriodCategoryInfo() {}

    private RelativePeriodCategoryInfo(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public RelativePeriodCategoryInfo(RelativePeriodCategory category , Thesaurus thesaurus) {
        this(category.getId(), getName(category, thesaurus));
    }

    static public List<RelativePeriodCategoryInfo> from (List<RelativePeriodCategory> categories, Thesaurus thesaurus) {
        List<RelativePeriodCategoryInfo> categoryInfos = new ArrayList<>();
        categories.stream().forEach(c -> categoryInfos.add(new RelativePeriodCategoryInfo(c.getId(), getName(c, thesaurus))));
        return categoryInfos;
    }

    private static String getName(RelativePeriodCategory category, Thesaurus thesaurus) {
        return thesaurus.getStringBeyondComponent(category.getName(), category.getName());
    }
}
