/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest;

import com.elster.jupiter.time.RelativePeriod;

import java.util.ArrayList;
import java.util.List;

public class RelativePeriodInfos {

    public int total;
    public List<RelativePeriodInfo> data = new ArrayList<>();

    public RelativePeriodInfos() {
    }

    public RelativePeriodInfos(List<? extends RelativePeriod> periods) {
        periods.forEach(rp -> data.add(RelativePeriodInfo.withCategories(rp)));
    }

}