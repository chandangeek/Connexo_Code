/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.yellowfin.YellowfinReportInfo;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Albertv on 12/3/2014.
 */
public class ReportInfos {

    public int total;

    public List<ReportInfo> reports = new ArrayList<>();

    public ReportInfos() {
    }

    public ReportInfos(YellowfinReportInfo reportInfo) {
        add(reportInfo);
    }


    public ReportInfo add(YellowfinReportInfo yellowfinReportInfo) {
        ReportInfo reportInfo = new ReportInfo(yellowfinReportInfo);
        reports.add(reportInfo);
        total++;
        return reportInfo;
    }

    void addAll(Iterable<? extends YellowfinReportInfo> reports) {
        for (YellowfinReportInfo each : reports) {
            add(each);
        }
    }
}
