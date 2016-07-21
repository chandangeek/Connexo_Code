package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.validation.kpi.DataValidationKpiScore;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataValidationKpiMembers {
    private final Map<MonitoredDataValidationKpiMemberTypes, KpiMember> kpiMembers = new EnumMap<>(MonitoredDataValidationKpiMemberTypes.class);

    DataValidationKpiMembers(List<? extends KpiMember> kpiMembers) {
        super();
        for (KpiMember kpiMember : kpiMembers) {
            this.kpiMembers.put(MonitoredDataValidationKpiMemberTypes.valueOf(kpiMember.getName().substring(0,kpiMember.getName().indexOf("_"))), kpiMember);
        }
    }

    Optional<DataValidationKpiScore> getScores(Range<Instant> interval) {

       Map<MonitoredDataValidationKpiMemberTypes, BigDecimal> entryList = Stream.of(MonitoredDataValidationKpiMemberTypes.values())
                .collect(Collectors.toMap(s -> s, s -> this.kpiMembers.get(s).getScores(interval).stream().map(el -> el.getScore())
                        .reduce(BigDecimal.ZERO, BigDecimal::add)));

        Instant timestamp = interval.hasUpperBound() ? interval.upperEndpoint() : Instant.now();
        return newScore(timestamp, entryList);

    }

    private Optional<DataValidationKpiScore> newScore(Instant timestamp, Map<MonitoredDataValidationKpiMemberTypes, BigDecimal> scores) {
        return Optional.of(new DataValidationKpiScoreImpl(
                timestamp,scores
                ));
    }
}
