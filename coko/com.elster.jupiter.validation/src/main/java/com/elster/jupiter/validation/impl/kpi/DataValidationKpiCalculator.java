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
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(LocalTime.MIDNIGHT).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        dayCount = ChronoUnit.DAYS.between(start, end);
        registerSuspects = dataValidationReportService.getRegisterSuspects(dataValidationKpiClone.getDeviceGroup(), Range.openClosed(start.toInstant(), end.toInstant()));
        channelsSuspects = dataValidationReportService.getChannelsSuspects(dataValidationKpiClone.getDeviceGroup(), Range.openClosed(start.toInstant(), end.toInstant()));
        currentZonedDateTime = end;
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
                    if (channelsSuspects.get(member.getName()) != null) {
                        score(member, channelsSuspects, localTimeStamp);
                    } else if (registerSuspects.get(member.getName()) != null) {
                        score(member, registerSuspects, localTimeStamp);
                    } else if ((DataValidationKpiMemberTypes.SUSPECT.fieldName() + endDeviceId).equals(member.getName())) {
                        member.score(localTimeStamp, BigDecimal.valueOf(totalSuspects));
                    } else if ((DataValidationKpiMemberTypes.ALLDATAVALIDATED.fieldName() + endDeviceId).equals(member.getName())) {
                        member.score(localTimeStamp, allDataValidated ? BigDecimal.ONE : BigDecimal.ZERO);
                    }
                });
                updateRuleValidatorData(memberList.get(), localTimeStamp);
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

    private void updateRuleValidatorData(List<? extends KpiMember> memberList, Instant localTimeStamp) {
        Set<String> validatorList = Stream.of(MonitoredDataValidationKpiMemberTypes.values()).skip(4)
                .map(memberType -> memberType.name().toUpperCase() + "_" + runnningDeviceId).collect(Collectors.toSet());
        memberList.stream().filter(member -> validatorList.contains(member.getName())).forEach(foundElement -> {
                    if (ruleValidators.contains(foundElement.getName())) {
                        foundElement.score(localTimeStamp, BigDecimal.ONE);
                    } else {
                        foundElement.score(localTimeStamp, BigDecimal.ZERO);
                    }
                }
        );
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