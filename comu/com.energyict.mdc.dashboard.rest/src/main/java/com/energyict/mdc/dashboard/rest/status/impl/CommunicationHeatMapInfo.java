package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMap;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMapRow;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bvn on 8/19/14.
 */
public class CommunicationHeatMapInfo {

    private static final CompletionCodeAdapter completionCodeAdapter = new CompletionCodeAdapter();
    private static final CompletionCodeTaskCounterInfoComparator completionCodeTaskCounterInfoComparator = new CompletionCodeTaskCounterInfoComparator();

    public List<HeatMapRowInfo> heatMap;

    public CommunicationHeatMapInfo() {
    }

    public <H extends HasName & HasId> CommunicationHeatMapInfo(CommunicationTaskHeatMap heatMap, Thesaurus thesaurus)
            throws Exception {
        this.heatMap=new ArrayList<>();
        this.heatMap.addAll(createHeatMap(heatMap, thesaurus));
    }

    private <H extends HasName & HasId> List<HeatMapRowInfo> createHeatMap(CommunicationTaskHeatMap heatMap, Thesaurus thesaurus) throws Exception {
        List<HeatMapRowInfo> heatMapInfoList = new ArrayList<>();
        if (heatMap!=null) {
            for (CommunicationTaskHeatMapRow row : heatMap) {
                HeatMapRowInfo heatMapRowInfo = new HeatMapRowInfo();
                heatMapRowInfo.displayValue = row.getTarget().getName(); // CPP name, device type name, ...
                heatMapRowInfo.id = row.getTarget().getId(); // ID of the object
                heatMapRowInfo.alias = BreakdownOption.deviceType; // Type of object
                heatMapRowInfo.data = new ArrayList<>();
                for (ComCommandCompletionCodeOverview counters : row) {
                    for (Counter<CompletionCode> completionCodeCounter : counters) {
                        TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                        taskCounterInfo.id = completionCodeAdapter.marshal(completionCodeCounter.getCountTarget());
                        taskCounterInfo.displayName = thesaurus.getString(completionCodeAdapter.marshal(completionCodeCounter.getCountTarget()), null);
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
