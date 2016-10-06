package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.kpi.DataValidationReportService;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DataValidationKpiCalculator implements DataManagementKpiCalculator {

    private volatile DataValidationKpiImpl dataValidationKpi;
    private volatile Logger logger;
    private volatile DataValidationReportService dataValidationReportService;
    private volatile Clock clock;

    DataValidationKpiCalculator(DataValidationKpiImpl dataValidationKpi, Logger logger, DataValidationReportService dataValidationReportService, Clock clock) {
        this.dataValidationKpi = dataValidationKpi;
        this.logger = logger;
        this.dataValidationReportService = dataValidationReportService;
        this.clock = clock;
    }

    @Override
    public void calculateAndStore() {
        dataValidationKpi.updateMembers();
        if (dataValidationKpi.isCancelled()) {
            dataValidationKpi.dropDataValidationKpi();
            return;
        }
        DataValidationKpiImpl dataValidationKpiClone = dataValidationKpi.clone();
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(LocalTime.MIDNIGHT).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        long dayCount = ChronoUnit.DAYS.between(start, end);
        ZonedDateTime currentZonedDateTime = clock.instant().atZone(ZoneId.systemDefault()).with(LocalTime.MIDNIGHT).with(ChronoField.MILLI_OF_DAY, 0L);
        Range<Instant> range = Range.closedOpen(currentZonedDateTime.minus(Period.ofDays(1)).toInstant(), currentZonedDateTime.toInstant());
        currentZonedDateTime = currentZonedDateTime.plus(Period.ofDays(1));
        for (int i = 0; i < dayCount; ++i) {
            if (dataValidationKpi.isCancelled()) {
                dataValidationKpi.dropDataValidationKpi();
                return;
            }
            Instant localTimeStamp = currentZonedDateTime.minusDays(i).toInstant();
            Map<String, List<DataValidationStatus>> registerSuspects = dataValidationReportService.getRegisterSuspects(dataValidationKpiClone.getDeviceGroup(), range);
            Map<String, List<DataValidationStatus>> channelsSuspects = dataValidationReportService.getChannelsSuspects(dataValidationKpiClone.getDeviceGroup(), range);
            Map<String, Boolean> allDataValidated = dataValidationReportService.getAllDataValidated(dataValidationKpiClone.getDeviceGroup(), range);
            Map<String, BigDecimal> totalSuspects = aggregateSuspects(registerSuspects, channelsSuspects);
            List<String> ruleValidators = aggregateRuleValidators(registerSuspects, channelsSuspects);
            dataValidationKpiClone.getDataValidationKpiChildren().forEach(kpi -> {
                if (dataValidationKpi.isCancelled()) {
                    dataValidationKpi.dropDataValidationKpi();
                    return;
                }
                kpi.getChildKpi().getMembers()
                        .forEach(member -> {
                            if (dataValidationKpi.isCancelled()) {
                                dataValidationKpi.dropDataValidationKpi();
                                return;
                            }
                            if (registerSuspects.get(member.getName()) != null) {
                                member.score(localTimeStamp, new BigDecimal(registerSuspects.get(member.getName()).size()));
                            }
                            if (channelsSuspects.get(member.getName()) != null) {
                                member.score(localTimeStamp, new BigDecimal(channelsSuspects.get(member.getName()).size()));
                            }
                            if (totalSuspects.get(member.getName()) != null) {
                                member.score(localTimeStamp, totalSuspects.get(member.getName()));
                            }
                            if (allDataValidated.get(member.getName()) != null) {
                                member.score(localTimeStamp, allDataValidated.get(member.getName()) ? BigDecimal.ONE : BigDecimal.ZERO);
                            }
                            if (ruleValidators.stream().anyMatch(r -> r.equals(member.getName()))) {
                                member.score(localTimeStamp, BigDecimal.ONE);
                            }
                        });
            });
            range = Range.closedOpen(localTimeStamp.minus(Period.ofDays(1)), localTimeStamp);
        }
    }

    private List<String> aggregateRuleValidators(Map<String, List<DataValidationStatus>> registerSuspects, Map<String, List<DataValidationStatus>> channelsSuspects) {
        return Stream.concat(
                registerSuspects.entrySet().stream()
                        .flatMap(entry -> entry.getValue().stream()
                                .map(DataValidationStatus::getOffendedRules)
                                .flatMap(Collection::stream)
                                .map(rule -> rule.getImplementation().substring(rule.getImplementation().lastIndexOf(".") + 1))
                                .map(rule -> rule.toUpperCase() + "_" + entry.getKey().replace(DataValidationKpiMemberTypes.REGISTER.fieldName(), ""))),
                channelsSuspects.entrySet().stream()
                        .flatMap(entry -> entry.getValue().stream()
                                .map(DataValidationStatus::getOffendedRules)
                                .flatMap(Collection::stream)
                                .map(rule -> rule.getImplementation().substring(rule.getImplementation().lastIndexOf(".") + 1))
                                .map(rule -> rule.toUpperCase() + "_" + entry.getKey().replace(DataValidationKpiMemberTypes.CHANNEL.fieldName(), "")))
        ).distinct().collect(Collectors.toList());

    }

    private Map<String, BigDecimal> aggregateSuspects(Map<String, List<DataValidationStatus>> registerSuspects, Map<String, List<DataValidationStatus>> channelsSuspects) {
        return Stream.concat(
                registerSuspects.keySet()
                        .stream()
                        .map(s -> s.replace(DataValidationKpiMemberTypes.REGISTER.fieldName(), "")),
                channelsSuspects.keySet()
                        .stream()
                        .map(s -> s.replace(DataValidationKpiMemberTypes.CHANNEL.fieldName(), "")))
                .distinct()
                .map(suspect -> DataValidationKpiMemberTypes.SUSPECT.fieldName() + suspect)
                .collect(Collectors.toMap(suspect -> suspect,
                        s -> new BigDecimal(registerSuspects.get(s.replace(DataValidationKpiMemberTypes.SUSPECT.fieldName(), DataValidationKpiMemberTypes.REGISTER
                                .fieldName())).size())
                                .add(new BigDecimal(channelsSuspects.get(s.replace(DataValidationKpiMemberTypes.SUSPECT.fieldName(), DataValidationKpiMemberTypes.CHANNEL
                                        .fieldName())).size()))
                ));
    }

}