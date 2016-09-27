package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.kpi.DataValidationKpi;
import com.elster.jupiter.validation.kpi.DataValidationReportService;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
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
        this.setClock(clock);
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void calculateAndStore() {
        dataValidationKpi.updateMembers();
        if (dataValidationKpi.isCancelled()) {
            dataValidationKpi.dropDataValidationKpi();
            return;
        }
        DataValidationKpi dataValidationKpiClone = dataValidationKpi.clone();
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault())
                .with(LocalTime.MIDNIGHT)
                .plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        long dayCount = ChronoUnit.DAYS.between(start, end);

        for (int i = 0; i < dayCount; ++i) {
            if (dataValidationKpi.isCancelled()) {
                dataValidationKpi.dropDataValidationKpi();
                return;
            }
            Range<Instant> range = Range.closedOpen(end.minusDays(2 + i).toInstant(), end.minusDays(1 + i).toInstant());

            Instant localTimeStamp = end.minusDays(i).toInstant();
            Map<String, List<DataValidationStatus>> registerSuspects = dataValidationReportService.getRegisterSuspects(dataValidationKpiClone
                    .getDeviceGroup(), range);
            Map<String, List<DataValidationStatus>> channelsSuspects = dataValidationReportService.getChannelsSuspects(dataValidationKpiClone
                    .getDeviceGroup(), range);
            Map<String, Boolean> allDataValidated = dataValidationReportService.getAllDataValidated(dataValidationKpiClone
                    .getDeviceGroup(), range);
            Map<String, BigDecimal> totalSuspects = aggregateSuspects(registerSuspects, channelsSuspects);
            Set<String> ruleValidators = aggregateRuleValidators(registerSuspects, channelsSuspects);
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
                                member.score(localTimeStamp, new BigDecimal(registerSuspects.get(member.getName())
                                        .size()));
                            }
                            if (channelsSuspects.get(member.getName()) != null) {
                                member.score(localTimeStamp, new BigDecimal(channelsSuspects.get(member.getName())
                                        .size()));
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
            logger.log(Level.INFO, ">>>>>>>>>>> CalculateAndStore !!!" + " date " + localTimeStamp + " count " + i);
        }
    }

    private Set<String> aggregateRuleValidators(Map<String, List<DataValidationStatus>> registerSuspects, Map<String, List<DataValidationStatus>> channelsSuspects) {
        return Stream.of(
                offendedRuleNames(registerSuspects.entrySet().stream(), DataValidationKpiMemberTypes.REGISTER.fieldName()),
                offendedRuleNames(channelsSuspects.entrySet().stream(), DataValidationKpiMemberTypes.CHANNEL.fieldName())
        )
                .flatMap(Function.identity())
                .collect(Collectors.toSet());

    }

    private Stream<String> offendedRuleNames(Stream<Map.Entry<String, List<DataValidationStatus>>> stream, String fieldName) {
        return stream
                .flatMap(entry -> entry.getValue().stream()
                        .map(DataValidationStatus::getOffendedRules)
                        .flatMap(Collection::stream)
                        .map(rule -> rule.getImplementation()
                                .substring(rule.getImplementation().lastIndexOf(".") + 1))
                        .map(rule -> {
                            return rule.toUpperCase() + "_" + entry.getKey()
                                    .replace(fieldName, "");
                        }));
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