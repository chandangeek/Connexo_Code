package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.validation.kpi.DataValidationReportService;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DataValidationKpiCalculator implements DataManagementKpiCalculator {

    private volatile DataValidationKpiImpl dataValidationKpi;
    private volatile Logger logger;
    private volatile DataValidationReportService dataValidationReportService;
    private volatile Clock clock;

    public DataValidationKpiCalculator(DataValidationKpiImpl dataValidationKpi, Logger logger, DataValidationReportService dataValidationReportService, Clock clock) {
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
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(LocalTime.MIDNIGHT).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        Range<Instant> range = Range.openClosed(start.toInstant(), end.toInstant());
        long dayCount = ChronoUnit.DAYS.between(start,end);
        ZonedDateTime currentZonedDateTime = clock.instant().atZone(ZoneId.systemDefault()).with(LocalTime.MIDNIGHT).with(ChronoField.MILLI_OF_DAY, 0L);
        for (int i = 0 ; i <= dayCount; ++i) {
            Instant localTimeStamp = currentZonedDateTime.minusDays(i).toInstant();
            Map<String, BigDecimal> registerSuspects = dataValidationReportService.getRegisterSuspects(dataValidationKpi.getDeviceGroup(), range);
            Map<String, BigDecimal> channelsSuspects = dataValidationReportService.getChannelsSuspects(dataValidationKpi.getDeviceGroup(), range);
            Map<String, Boolean> allDataValidated = dataValidationReportService.getAllDataValidated(dataValidationKpi.getDeviceGroup(), range);
            Map<String, BigDecimal> totalSuspects = aggregateSuspects(registerSuspects, channelsSuspects);
            dataValidationKpi.getDataValidationKpiChildren().stream().forEach(kpi -> kpi.getChildKpi().getMembers().stream()
                    .forEach(member -> {
                        if (registerSuspects.get(member.getName()) != null && (registerSuspects.get(member.getName()).compareTo(new BigDecimal(0)) == 1)) {
                            member.score(localTimeStamp, registerSuspects.get(member.getName()));
                        }
                        if (channelsSuspects.get(member.getName()) != null && (channelsSuspects.get(member.getName()).compareTo(new BigDecimal(0)) == 1)) {
                            member.score(localTimeStamp, channelsSuspects.get(member.getName()));
                        }
                        if (totalSuspects.get(member.getName()) != null && (totalSuspects.get(member.getName()).compareTo(new BigDecimal(0)) == 1)) {
                            member.score(localTimeStamp, totalSuspects.get(member.getName()));
                        }
                        if(allDataValidated.get(member.getName()) != null){
                            member.score(localTimeStamp, allDataValidated.get(member.getName()) ? BigDecimal.ONE : BigDecimal.ZERO);
                        }
                    }));
            range = Range.closedOpen(localTimeStamp.minus(Period.ofDays(1)), localTimeStamp);
            logger.log(Level.INFO, ">>>>>>>>>>> CalculateAndStore !!!" + " date " + localTimeStamp + " count " + i);
        }
    }

    private Map<String, BigDecimal> aggregateSuspects(Map<String, BigDecimal> registerSuspects, Map<String, BigDecimal> channelsSuspects) {
        return Stream.concat(
                registerSuspects.keySet()
                        .stream()
                        .filter(register -> registerSuspects.get(register).compareTo(new BigDecimal(0)) == 1)
                        .map(s -> s.replace(DataValidationKpiMemberTypes.REGISTER.fieldName(), "")),
                channelsSuspects.keySet()
                        .stream()
                        .filter(channel -> channelsSuspects.get(channel).compareTo(new BigDecimal(0)) == 1)
                        .map(s -> s.replace(DataValidationKpiMemberTypes.CHANNEL.fieldName(), "")))
                .distinct()
                .map(suspect -> DataValidationKpiMemberTypes.SUSPECT.fieldName() + suspect)
                .collect(Collectors.toMap(suspect -> suspect,
                        s -> registerSuspects.get(s.replace(DataValidationKpiMemberTypes.SUSPECT.fieldName(), DataValidationKpiMemberTypes.REGISTER
                                .fieldName()))
                                .add(channelsSuspects.get(s.replace(DataValidationKpiMemberTypes.SUSPECT.fieldName(), DataValidationKpiMemberTypes.CHANNEL
                                        .fieldName())))
                ));
    }

}
