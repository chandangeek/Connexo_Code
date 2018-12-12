/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTaskHeatMap;
import com.energyict.mdc.dashboard.ConnectionTaskHeatMapRow;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.device.data.tasks.history.ComSession;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by bvn on 8/19/14.
 */
public class ConnectionHeatMapInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ConnectionHeatMapInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public <H extends HasName & HasId> ConnectionHeatMapInfo asInfo(ConnectionTaskHeatMap<H> heatMap, HeatMapBreakdownOption breakdown) {
        if (heatMap!=null) {
            ConnectionHeatMapInfo info = new ConnectionHeatMapInfo();
            info.breakdown = breakdown;
            info.heatMap=new ArrayList<>();
            info.alias=breakdown.filterOption();

            for (ConnectionTaskHeatMapRow<H> row : heatMap) {
                HeatMapRowInfo heatMapRowInfo = new HeatMapRowInfo();
                heatMapRowInfo.displayValue = row.getTarget().getName(); // CPP name, device type name, ...
                heatMapRowInfo.id = row.getTarget().getId(); // ID of the object
                heatMapRowInfo.alias = FilterOption.latestResults; // Type of object
                heatMapRowInfo.data = new ArrayList<>();
                for (ComSessionSuccessIndicatorOverview counters : row) {
                    for (Counter<ComSession.SuccessIndicator> successIndicatorCounter : counters) {
                        TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                        taskCounterInfo.id = successIndicatorCounter.getCountTarget().name();
                        taskCounterInfo.displayName = ComSessionSuccessIndicatorTranslationKeys.translationFor(successIndicatorCounter.getCountTarget(), thesaurus);
                        taskCounterInfo.count = successIndicatorCounter.getCount();
                        heatMapRowInfo.data.add(taskCounterInfo);
                    }
                    TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                    taskCounterInfo.id = TranslationKeys.SUCCESS_WITH_FAILED_TASKS.getKey();
                    taskCounterInfo.displayName = thesaurus.getFormat(TranslationKeys.SUCCESS_WITH_FAILED_TASKS).format();
                    taskCounterInfo.count = counters.getAtLeastOneTaskFailedCount();
                    heatMapRowInfo.data.add(taskCounterInfo);
                }
                Collections.sort(heatMapRowInfo.data, new SuccessIndicatorTaskCounterInfoComparator());
                info.heatMap.add(heatMapRowInfo);
            }
            Collections.sort(info.heatMap, new HeatMapRowInfoComparator());
            return info;
        }
        return null;
    }
}
