/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMap;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMapRow;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bvn on 8/19/14.
 */
public class CommunicationHeatMapInfoFactory {

    private static final CompletionCodeTaskCounterInfoComparator completionCodeTaskCounterInfoComparator = new CompletionCodeTaskCounterInfoComparator();

    private final Thesaurus thesaurus;

    @Inject
    public CommunicationHeatMapInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public CommunicationHeatMapInfo from(CommunicationTaskHeatMap heatMap) {
        CommunicationHeatMapInfo info = new CommunicationHeatMapInfo();
        info.breakdown = HeatMapBreakdownOption.deviceTypes;
        info.alias = info.breakdown.filterOption();
        info.heatMap = new ArrayList<>();
        info.heatMap.addAll(createHeatMap(heatMap,thesaurus));
        return info;
    }

    private List<HeatMapRowInfo> createHeatMap(CommunicationTaskHeatMap heatMap, Thesaurus thesaurus) {
        List<HeatMapRowInfo> heatMapInfoList = new ArrayList<>();
        if (heatMap!=null) {
            for (CommunicationTaskHeatMapRow row : heatMap) {
                HeatMapRowInfo heatMapRowInfo = new HeatMapRowInfo();
                heatMapRowInfo.displayValue = row.getTarget().getName(); // CPP name, device type name, ...
                heatMapRowInfo.id = row.getTarget().getId(); // ID of the object
                heatMapRowInfo.alias = FilterOption.latestResults; // Type of object
                heatMapRowInfo.data = new ArrayList<>();
                for (ComCommandCompletionCodeOverview counters : row) {
                    for (Counter<CompletionCode> completionCodeCounter : counters) {
                        TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                        taskCounterInfo.id = completionCodeCounter.getCountTarget().name();
                        taskCounterInfo.displayName = CompletionCodeTranslationKeys.translationFor(completionCodeCounter.getCountTarget(), thesaurus);
                        taskCounterInfo.count = completionCodeCounter.getCount();
                        heatMapRowInfo.data.add(taskCounterInfo);
                    }
                    Collections.sort(heatMapRowInfo.data, completionCodeTaskCounterInfoComparator);
                }
                heatMapInfoList.add(heatMapRowInfo);
            }
            Collections.sort(heatMapInfoList, new HeatMapRowInfoComparator());
        }
        return heatMapInfoList;
    }

}