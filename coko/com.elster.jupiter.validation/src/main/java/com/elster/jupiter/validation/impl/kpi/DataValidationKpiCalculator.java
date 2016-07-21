package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.validation.kpi.DataValidationReportService;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Period;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DataValidationKpiCalculator implements DataManagementKpiCalculator {

    private final DataValidationKpiImpl dataValidationKpi;
    private final Logger logger;
    private final DataValidationReportService dataValidationReportService;
    private final Instant timestamp;

    public DataValidationKpiCalculator(DataValidationKpiImpl dataValidationKpi, Instant timestamp, Logger logger, DataValidationReportService dataValidationReportService) {
        this.timestamp = timestamp;
        this.dataValidationKpi = dataValidationKpi;
        this.logger = logger;
        this.dataValidationReportService = dataValidationReportService;
    }

    @Override
    public void calculateAndStore() {
        //FixMe will be implemented in next story CXO-1611;
        //Clean up - inject clock for accuracy
        /*
        private Range<Instant> lastMonth() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return Range.openClosed(start.toInstant(), end.toInstant());
    }
         */
        Range<Instant> range = Range.closed(timestamp.minus(Period.ofDays(30)), timestamp);
        int i = 0;
        for (; i <= 30; i++) {
            Instant localTimeStamp = timestamp.minus(Period.ofDays(i));
            Map<String, BigDecimal> registerSuspects = dataValidationReportService.getRegisterSuspects(dataValidationKpi.getDeviceGroup(), range);
            Map<String, BigDecimal> channelsSuspects = dataValidationReportService.getChannelsSuspects(dataValidationKpi.getDeviceGroup(), range);
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
