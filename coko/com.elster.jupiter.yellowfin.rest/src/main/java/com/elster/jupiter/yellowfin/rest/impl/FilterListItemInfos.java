/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.yellowfin.YellowfinFilterListItemInfo;
import com.elster.jupiter.yellowfin.YellowfinReportInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Albertv on 12/3/2014.
 */
public class FilterListItemInfos {

    public int total;

    public List<FilterListItemInfo> listitems = new ArrayList<>();

    public FilterListItemInfos() {
    }

    public FilterListItemInfos(YellowfinFilterListItemInfo reportInfo) {
        add(reportInfo);
    }


    public FilterListItemInfo add(YellowfinFilterListItemInfo yellowfinReportInfo) {
        FilterListItemInfo reportInfo = new FilterListItemInfo(yellowfinReportInfo);
        listitems.add(reportInfo);
        total++;
        return reportInfo;
    }

    void addAll(Iterable<? extends YellowfinFilterListItemInfo> reports) {
        for (YellowfinFilterListItemInfo each : reports) {
            add(each);
        }
    }
}
