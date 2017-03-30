/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
* Decorator for the fixed set of {@link KpiMember}s
* that are created for every {@link DataCollectionKpiImpl}.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-10-07 (13:30)
*/
class KpiMembers {
    private final Map<MonitoredTaskStatus, KpiMember> kpiMembers = new EnumMap<>(MonitoredTaskStatus.class);

    KpiMembers(List<? extends KpiMember> kpiMembers) {
        super();
        for (KpiMember kpiMember : kpiMembers) {
            this.kpiMembers.put(MonitoredTaskStatus.valueOf(kpiMember.getName()), kpiMember);
        }
    }

    private MonitoredTaskStatus targetMemberStatus() {
        return MonitoredTaskStatus.Total;
    }

    private KpiMember targetMember() {
        return this.kpiMembers.get(this.targetMemberStatus());
    }

    List<DataCollectionKpiScore> getScores(Range<Instant> interval) {
        KpiMember targetMember = this.targetMember();
        List<List<? extends KpiEntry>> entries =
                Stream.of(MonitoredTaskStatus.values()).
                    map(s -> this.kpiMembers.get(s).getScores(interval)).
                    collect(Collectors.toList());
        List<DataCollectionKpiScore> scores = new ArrayList<>();
        for (int i = 0; i < entries.get(0).size(); i++) {
            Instant timestamp = entries.get(0).get(i).getTimestamp();
            List<KpiEntry> kpiEntries = new ArrayList<>(MonitoredTaskStatus.values().length);
            for (int s = 0; s < MonitoredTaskStatus.values().length; s++) {
                kpiEntries.add(entries.get(s).get(i));
            }
            scores.add(this.newScore(timestamp, targetMember, kpiEntries));
        }
        return scores;
    }

    private DataCollectionKpiScore newScore(Instant timestamp, KpiMember targetMember, List<KpiEntry> kpiEntries) {
        return new DataCollectionKpiScoreImpl(
                timestamp,
                targetMember,
                Arrays.asList(MonitoredTaskStatus.values()),
                kpiEntries);
    }

}