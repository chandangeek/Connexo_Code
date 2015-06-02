package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTaskHeatMap;
import com.energyict.mdc.dashboard.ConnectionTaskHeatMapRow;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.device.data.rest.ComSessionSuccessIndicatorAdapter;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.HasName;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bvn on 8/19/14.
 */
public class ConnectionHeatMapInfo {

    @JsonIgnore
    private static final ComSessionSuccessIndicatorAdapter COM_SESSION_SUCCESS_INDICATOR_ADAPTER = new ComSessionSuccessIndicatorAdapter();

    @XmlJavaTypeAdapter(BreakdownOptionAdapter.class)
    public HeatMapBreakdownOption breakdown;
    public FilterOption alias;
    public List<HeatMapRowInfo> heatMap;

    public <H extends HasName & HasId> ConnectionHeatMapInfo(ConnectionTaskHeatMap<H> heatMap, HeatMapBreakdownOption breakdown, Thesaurus thesaurus) {
        this.breakdown = breakdown;
        this.heatMap=new ArrayList<>();
        this.alias=breakdown.filterOption();
        createHeatMap(heatMap, thesaurus);
    }

    private <H extends HasName & HasId> void createHeatMap(ConnectionTaskHeatMap<H> heatMap, Thesaurus thesaurus) {
        if (heatMap!=null) {
            for (ConnectionTaskHeatMapRow<H> row : heatMap) {
                HeatMapRowInfo heatMapRowInfo = new HeatMapRowInfo();
                heatMapRowInfo.displayValue = row.getTarget().getName(); // CPP name, device type name, ...
                heatMapRowInfo.id = row.getTarget().getId(); // ID of the object
                heatMapRowInfo.alias = FilterOption.latestResults; // Type of object
                heatMapRowInfo.data = new ArrayList<>();
                for (ComSessionSuccessIndicatorOverview counters : row) {
                    for (Counter<ComSession.SuccessIndicator> successIndicatorCounter : counters) {
                        TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                        taskCounterInfo.id = COM_SESSION_SUCCESS_INDICATOR_ADAPTER.marshal(successIndicatorCounter.getCountTarget());
                        taskCounterInfo.displayName = thesaurus.getString(COM_SESSION_SUCCESS_INDICATOR_ADAPTER.marshal(successIndicatorCounter.getCountTarget()), COM_SESSION_SUCCESS_INDICATOR_ADAPTER.marshal(successIndicatorCounter.getCountTarget()));
                        taskCounterInfo.count = successIndicatorCounter.getCount();
                        heatMapRowInfo.data.add(taskCounterInfo);
                    }
                    TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                    taskCounterInfo.id = MessageSeeds.SUCCESS_WITH_FAILED_TASKS.getKey();
                    taskCounterInfo.displayName = thesaurus.getString(MessageSeeds.SUCCESS_WITH_FAILED_TASKS.getKey(), MessageSeeds.SUCCESS_WITH_FAILED_TASKS.getKey());
                    taskCounterInfo.count = counters.getAtLeastOneTaskFailedCount();
                    heatMapRowInfo.data.add(taskCounterInfo);
                }
                Collections.sort(heatMapRowInfo.data, new SuccessIndicatorTaskCounterInfoComparator());
                this.heatMap.add(heatMapRowInfo);
            }
            Collections.sort(this.heatMap, new HeatMapRowInfoComparator());
        }
    }
}
