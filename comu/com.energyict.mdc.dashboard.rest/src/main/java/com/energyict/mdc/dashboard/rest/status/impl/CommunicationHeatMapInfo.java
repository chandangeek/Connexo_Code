package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMap;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMapRow;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bvn on 8/19/14.
 */
public class CommunicationHeatMapInfo {

    private static final CompletionCodeTaskCounterInfoComparator completionCodeTaskCounterInfoComparator = new CompletionCodeTaskCounterInfoComparator();

    public HeatMapBreakdownOption breakdown;
    public FilterOption alias;
    public List<HeatMapRowInfo> heatMap;

    public CommunicationHeatMapInfo() {
    }

    public CommunicationHeatMapInfo(CommunicationTaskHeatMap heatMap, Thesaurus thesaurus) {
        this();
        this.breakdown = HeatMapBreakdownOption.deviceTypes;
        this.alias = breakdown.filterOption();
        this.heatMap = new ArrayList<>();
        this.heatMap.addAll(createHeatMap(heatMap,thesaurus));
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