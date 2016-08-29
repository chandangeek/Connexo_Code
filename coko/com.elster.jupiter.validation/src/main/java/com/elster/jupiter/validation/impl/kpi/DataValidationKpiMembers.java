package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.validation.kpi.DataValidationKpiScore;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DataValidationKpiMembers {
    private final Map<MonitoredDataValidationKpiMemberTypes, KpiMember> kpiMembers = new EnumMap<>(MonitoredDataValidationKpiMemberTypes.class);

    DataValidationKpiMembers(List<? extends KpiMember> kpiMembers) {
        super();
        for (KpiMember kpiMember : kpiMembers) {
            this.kpiMembers.put(MonitoredDataValidationKpiMemberTypes.valueOf(kpiMember.getName().substring(0, kpiMember.getName().indexOf("_"))), kpiMember);
        }
    }

    Optional<DataValidationKpiScore> getScores(Range<Instant> interval) {
        Map<MonitoredDataValidationKpiMemberTypes, BigDecimal> suspectMap = Stream.of(MonitoredDataValidationKpiMemberTypes.values()).limit(3)
                .collect(Collectors.toMap(
                        s -> s,
                        s -> this.kpiMembers.get(s)
                                .getScores(interval)
                                .stream()
                                .map(KpiEntry::getScore)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        Map<MonitoredDataValidationKpiMemberTypes, BigDecimal> dataValidationStatusMap = Stream.of(MonitoredDataValidationKpiMemberTypes.values())
                .filter(kpiMemberType -> kpiMemberType.equals(MonitoredDataValidationKpiMemberTypes.ALLDATAVALIDATED))
                .collect(Collectors.toMap(
                        s -> s,
                        s -> this.kpiMembers.get(s)
                                .getScores(interval)
                                .stream()
                                .map(KpiEntry::getScore)
                                .allMatch(a -> a.longValue() == 1) ? BigDecimal.ONE : BigDecimal.ZERO));
        Map<MonitoredDataValidationKpiMemberTypes, BigDecimal> validatorsMap = Stream.of(MonitoredDataValidationKpiMemberTypes.values()).skip(4)
                .collect(Collectors.toMap(
                        s -> s,
                        s -> this.kpiMembers.get(s)
                                .getScores(interval)
                                .stream()
                                .map(KpiEntry::getScore)
                                .parallel()
                                .anyMatch(a -> a.longValue() == 1) ? BigDecimal.ONE : BigDecimal.ZERO));
        suspectMap.putAll(dataValidationStatusMap);
        suspectMap.putAll(validatorsMap);
        if (suspectMap.entrySet().stream()
                .filter(element -> !element.getKey().equals(MonitoredDataValidationKpiMemberTypes.ALLDATAVALIDATED))
                .allMatch(a -> a.getValue().compareTo(BigDecimal.ZERO) == 0)) {
            return Optional.empty();
        } else {
            Instant timestamp = kpiMembers
                                    .entrySet()
                                    .stream()
                                    .filter(member -> member.getKey().equals(MonitoredDataValidationKpiMemberTypes.SUSPECT))
                                    .map(member -> member.getValue().getScores(interval))
                                    .flatMap(Collection::stream)
                                    .filter(score -> score.getScore().compareTo(BigDecimal.ZERO) == 1)
                                    .map(KpiEntry::getTimestamp)
                                    .max(Comparator.naturalOrder()).get();
            return newScore(timestamp, suspectMap);
        }
    }

    private Optional<DataValidationKpiScore> newScore(Instant timestamp, Map<MonitoredDataValidationKpiMemberTypes, BigDecimal> scores) {
        return Optional.of(new DataValidationKpiScoreImpl(timestamp, scores));
    }

}