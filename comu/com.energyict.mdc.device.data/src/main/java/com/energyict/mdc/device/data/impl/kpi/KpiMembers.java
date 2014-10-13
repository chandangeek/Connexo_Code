package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.util.time.Interval;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
* Decorator for the fixed set of {@link KpiMember}s
* that are created for every {@link DataCollectionKpiImpl}
* as defined by {@link DataCollectionKpiService#MONITORED_STATUSSES}.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-10-07 (13:30)
*/
class KpiMembers {
    private final Map<TaskStatus, KpiMember> kpiMembers = new EnumMap<>(TaskStatus.class);

    KpiMembers(List<? extends KpiMember> kpiMembers) {
        super();
        for (KpiMember kpiMember : kpiMembers) {
            this.kpiMembers.put(TaskStatus.valueOf(kpiMember.getName()), kpiMember);
        }
    }

    private TaskStatus targetMemberStatus() {
        return TaskStatus.Waiting;
    }

    private KpiMember targetMember() {
        return this.kpiMembers.get(this.targetMemberStatus());
    }

    List<DataCollectionKpiScore> getScores(Interval interval) {
        KpiMember targetMember = this.targetMember();
        List<List<? extends KpiEntry>> entries =
                this.sortedMonitoredStatusses().
                    map(s -> this.kpiMembers.get(s).getScores(interval)).
                    collect(Collectors.toList());
        List<DataCollectionKpiScore> scores = new ArrayList<>();
        for (int i = 0; i < entries.get(0).size(); i++) {
            Date timestamp = entries.get(0).get(i).getTimestamp();
            List<KpiEntry> kpiEntries = new ArrayList<>(DataCollectionKpiService.MONITORED_STATUSSES.size());
            for (int s = 0; s < DataCollectionKpiService.MONITORED_STATUSSES.size(); s++) {
                kpiEntries.add(entries.get(s).get(i));
            }
            scores.add(this.newScore(timestamp, targetMember, kpiEntries));
        }
        return scores;
    }

    private Stream<TaskStatus> sortedMonitoredStatusses() {
        return DataCollectionKpiService.MONITORED_STATUSSES.stream().sorted();
    }

    private DataCollectionKpiScore newScore(Date timestamp, KpiMember targetMember, List<KpiEntry> kpiEntries) {
        return new DataCollectionKpiScoreImpl(
                timestamp,
                targetMember,
                this.sortedMonitoredStatusses().collect(Collectors.toList()),
                kpiEntries);
    }

}