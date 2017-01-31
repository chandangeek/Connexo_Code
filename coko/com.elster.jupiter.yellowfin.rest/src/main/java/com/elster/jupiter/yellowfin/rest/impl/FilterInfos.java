/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.yellowfin.YellowfinFilterInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Albertv on 12/5/2014.
 */
public class FilterInfos {
    public int total;

    public List<FilterInfo> filters = new ArrayList<>();

    public FilterInfos() {
    }

    public FilterInfos(YellowfinFilterInfo filterInfo) {
        add(filterInfo);
    }


    public FilterInfo add(YellowfinFilterInfo yellowfinFilterInfo) {
        FilterInfo filterInfo = new FilterInfo(yellowfinFilterInfo);
        filters.add(filterInfo);
        total++;
        return filterInfo;
    }

    void addAll(Iterable<? extends YellowfinFilterInfo> filters) {
        for (YellowfinFilterInfo each : filters) {
            add(each);
        }
    }
}
