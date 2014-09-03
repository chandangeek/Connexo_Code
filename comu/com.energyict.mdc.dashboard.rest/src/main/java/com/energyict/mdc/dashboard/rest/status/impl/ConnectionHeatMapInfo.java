package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTaskHeatMap;
import com.energyict.mdc.dashboard.ConnectionTaskHeatMapRow;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Created by bvn on 8/19/14.
 */
public class ConnectionHeatMapInfo {

    @JsonIgnore
    private static final SuccessIndicatorAdapter successIndicatorAdapter = new SuccessIndicatorAdapter();

    public List<HeatMapRowInfo> heatMap;
    @XmlJavaTypeAdapter(BreakdownOptionAdapter.class)
    public BreakdownOption breakdown;

    public <H extends HasName & HasId> ConnectionHeatMapInfo(ConnectionTaskHeatMap<H> heatMap, BreakdownOption breakdown, Thesaurus thesaurus)
            throws Exception {
        this.breakdown = breakdown;
        this.heatMap=new ArrayList<>();
        createHeatMap(heatMap, breakdown, thesaurus);
    }

    private <H extends HasName & HasId> void createHeatMap(ConnectionTaskHeatMap<H> heatMap, BreakdownOption breakdown, Thesaurus thesaurus) throws Exception {
        if (heatMap!=null) {
            for (ConnectionTaskHeatMapRow<H> row : heatMap) {
                HeatMapRowInfo heatMapRowInfo = new HeatMapRowInfo();
                heatMapRowInfo.displayValue = row.getTarget().getName(); // CPP name, device type name, ...
                heatMapRowInfo.id = row.getTarget().getId(); // ID of the object
                heatMapRowInfo.alias = breakdown; // Type of object
                heatMapRowInfo.data = new ArrayList<>();
                for (ComSessionSuccessIndicatorOverview counters : row) {
                    for (Counter<ComSession.SuccessIndicator> successIndicatorCounter : counters) {
                        TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                        taskCounterInfo.id = successIndicatorAdapter.marshal(successIndicatorCounter.getCountTarget());
                        taskCounterInfo.displayName = thesaurus.getString(successIndicatorAdapter.marshal(successIndicatorCounter.getCountTarget()), successIndicatorAdapter.marshal(successIndicatorCounter.getCountTarget()));
                        taskCounterInfo.count = successIndicatorCounter.getCount();
                        heatMapRowInfo.data.add(taskCounterInfo);
                    }
                    TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                    taskCounterInfo.id = MessageSeeds.AT_LEAST_ONE_FAILED.getKey();
                    taskCounterInfo.displayName = thesaurus.getString(MessageSeeds.AT_LEAST_ONE_FAILED.getKey(), MessageSeeds.AT_LEAST_ONE_FAILED.getKey());
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
