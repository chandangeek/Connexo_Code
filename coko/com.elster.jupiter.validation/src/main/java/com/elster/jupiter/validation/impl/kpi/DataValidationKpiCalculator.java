package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.kpi.DataValidationKpi;
import com.elster.jupiter.validation.kpi.DataValidationReportService;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private DataValidationKpiImpl dataValidationKpiClone;
    private ZonedDateTime currentZonedDateTime;

    private Map<String, List<DataValidationStatus>> registerSuspects;
    private Map<String, List<DataValidationStatus>> channelsSuspects;
    private Boolean allDataValidated;
    private long totalSuspects;
    private Set<String> ruleValidators;
    private Range<Instant> range;
    private long dayCount;
    private long runnningDeviceId;

    DataValidationKpiCalculator(DataValidationKpiImpl dataValidationKpi, Logger logger, DataValidationReportService dataValidationReportService, Clock clock) {
        this.dataValidationKpi = dataValidationKpi;
        this.logger = logger;
        this.dataValidationReportService = dataValidationReportService;
        this.clock = clock;
    }

    @Override
    public void calculate() {
        dataValidationKpi.updateMembers();
        if (dataValidationKpi.isCancelled()) {
            dataValidationKpi.dropDataValidationKpi();
            return;
        }
        dataValidationKpiClone = dataValidationKpi.clone();
        currentZonedDateTime = clock.instant().atZone(ZoneId.systemDefault()).with(LocalTime.MIDNIGHT).with(ChronoField.MILLI_OF_DAY, 0L);
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(LocalTime.MIDNIGHT).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        dayCount = ChronoUnit.DAYS.between(start, end);
        range = Range.closed(start.toInstant(), end.toInstant());
        registerSuspects = dataValidationReportService.getRegisterSuspects(dataValidationKpiClone.getDeviceGroup(), range);
        channelsSuspects = dataValidationReportService.getChannelsSuspects(dataValidationKpiClone.getDeviceGroup(), range);
        range = Range.closedOpen(currentZonedDateTime.minus(Period.ofDays(1)).toInstant(), currentZonedDateTime.toInstant());
        currentZonedDateTime = currentZonedDateTime.plus(Period.ofDays(1));
    }

    @Override
    public void store(long endDeviceId) {
        runnningDeviceId = endDeviceId;
        if (dataValidationKpi.isCancelled()) {
            dataValidationKpi.dropDataValidationKpi();
            return;
        }
        Optional<? extends List<? extends KpiMember>> memberList = dataValidationKpiClone.getDataValidationKpiChildren().stream()
                .filter(kpi -> !kpi.getChildKpi().getMembers().isEmpty()
                        && dataValidationKpiClone.deviceIdAsString(kpi.getChildKpi().getMembers().get(0)).equals(String.valueOf(runnningDeviceId)))
                //.equals("ValidationKpi_grp" + dataValidationKpiClone.getDeviceGroup().getId() + "_dev" + endDeviceId))
                .map(foundKpi -> foundKpi.getChildKpi().getMembers()).findFirst();
        if (memberList.isPresent()) {
            for (int i = 0; i < dayCount; ++i) {
                allDataValidated = true;
                totalSuspects = 0;
                ruleValidators = new HashSet<>();
                Instant localTimeStamp = currentZonedDateTime.minusDays(i).toInstant();
                memberList.get().forEach(member -> {
                    if (dataValidationKpi.isCancelled()) {
                        dataValidationKpi.dropDataValidationKpi();
                        return;
                    }
                    if (registerSuspects.get(member.getName()) != null) {
                        score(member, registerSuspects, localTimeStamp);
                    }
                    if (channelsSuspects.get(member.getName()) != null) {
                        score(member, channelsSuspects, localTimeStamp);
                    }
                    if ((DataValidationKpiMemberTypes.SUSPECT.fieldName() + endDeviceId).equals(member.getName())) {
                        member.score(localTimeStamp, BigDecimal.valueOf(totalSuspects));
                    }
                    if ((DataValidationKpiMemberTypes.ALLDATAVALIDATED.fieldName() + endDeviceId).equals(member.getName())) {
                        member.score(localTimeStamp, allDataValidated ? BigDecimal.ONE : BigDecimal.ZERO);
                    }
                    if (ruleValidators.stream().anyMatch(r -> r.equals(member.getName()))) {
                        member.score(localTimeStamp, ruleValidators.size() > 0 ? BigDecimal.ONE : BigDecimal.ZERO);
                    }
                });
                range = Range.closedOpen(localTimeStamp.minus(Period.ofDays(1)), localTimeStamp);
                logger.log(Level.INFO, ">>>>>>>>>>> CalculateAndStore !!!" + " date " + localTimeStamp + " count " + i);
            }
        } else {
            return;
        }
    }


    private void aggregateRuleValidators(List<DataValidationStatus> list) {
        ruleValidators.addAll(list.stream()
                .map(DataValidationStatus::getOffendedRules)
                .flatMap(Collection::stream)
                .map(rule -> rule.getImplementation()
                        .substring(rule.getImplementation().lastIndexOf(".") + 1).toUpperCase() + "_" + String.valueOf(runnningDeviceId))
                .collect(Collectors.toSet()));

    }

    public DataValidationKpi getDataValidationKpi() {
        return dataValidationKpi;
    }

    private void score(KpiMember member, Map<String, List<DataValidationStatus>> map, Instant localTimeStamp) {
        List<DataValidationStatus> dataValidationStatus = map.get(member.getName());
        List<DataValidationStatus> dailyDataValidationStatus = dataValidationStatus.stream()
                .filter(val -> val.getReadingTimestamp().atOffset(ZoneOffset.UTC).toLocalDate().equals(localTimeStamp.atOffset(ZoneOffset.UTC).toLocalDate()))
                .collect(Collectors.toList());
        dailyDataValidationStatus.stream().forEach(status -> allDataValidated &= status.completelyValidated());
        long count = dailyDataValidationStatus.size();
        totalSuspects += count;
        aggregateRuleValidators(dailyDataValidationStatus);
        member.score(localTimeStamp, new BigDecimal(count));
    }
}