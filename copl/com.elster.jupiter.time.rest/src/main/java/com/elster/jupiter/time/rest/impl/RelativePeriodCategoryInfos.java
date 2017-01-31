/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriodCategory;

import java.util.ArrayList;
import java.util.List;

public class RelativePeriodCategoryInfos {
    public int total;
    public List<RelativePeriodCategoryInfo> data = new ArrayList<>();

    public RelativePeriodCategoryInfos() {
    }

    public RelativePeriodCategoryInfos(List<RelativePeriodCategory> categories, Thesaurus thesaurus) {
        categories.stream().forEach(c -> data.add(new RelativePeriodCategoryInfo(c)));
        total = categories.size();
    }

}
