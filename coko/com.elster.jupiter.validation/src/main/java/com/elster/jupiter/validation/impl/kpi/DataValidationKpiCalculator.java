package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.validation.kpi.DataValidationReportService;

import java.math.BigDecimal;
import java.time.Instant;
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
        this.timestamp = timestamp ;
        this.dataValidationKpi = dataValidationKpi;
        this.logger = logger;
        this.dataValidationReportService = dataValidationReportService;
    }

    @Override
    public void calculateAndStore() {
        //FixMe will be implemented in next story CXO-1611;
        Map<String, BigDecimal> registerSuspects = dataValidationReportService.getRegisterSuspects(dataValidationKpi.getDeviceGroup());
        Map<String, BigDecimal> channelsSuspects = dataValidationReportService.getChannelsSuspects(dataValidationKpi.getDeviceGroup());
        Map<String, BigDecimal> totalSuspects = aggregateSuspects(registerSuspects, channelsSuspects);
        dataValidationKpi.getDataValidationKpi().getMembers().stream()
                .forEach(member -> {
                    if (registerSuspects.get(member.getName()) != null && (registerSuspects.get(member.getName()).compareTo(new BigDecimal(0)) == 1)) {
                        member.score(timestamp, registerSuspects.get(member.getName()));
                    }
                    if (channelsSuspects.get(member.getName()) != null && (channelsSuspects.get(member.getName()).compareTo(new BigDecimal(0)) == 1)) {
                        member.score(timestamp, channelsSuspects.get(member.getName()));
                    }
                    if (totalSuspects.get(member.getName()) != null && (totalSuspects.get(member.getName()).compareTo(new BigDecimal(0)) == 1)) {
                        member.score(timestamp, totalSuspects.get(member.getName()));
                    }
                });
        logger.log(Level.INFO, ">>>>>>>>>>> CalculateAndStore !!!");

    }

    private Map<String, BigDecimal> aggregateSuspects(Map<String, BigDecimal> registerSuspects, Map<String, BigDecimal> channelsSuspects){
        return Stream.concat(
                registerSuspects.keySet()
                        .stream()
                        .filter(register -> registerSuspects.get(register).compareTo(new BigDecimal(0)) == 1)
                        .map(s -> s.replace(DataValidationKpiImpl.DataValidationKpiMembers.REGISTER.fieldName(), "")),
                channelsSuspects.keySet()
                        .stream()
                        .filter(channel -> channelsSuspects.get(channel).compareTo(new BigDecimal(0)) == 1)
                        .map(s -> s.replace(DataValidationKpiImpl.DataValidationKpiMembers.CHANNELS.fieldName(), "")))
                .distinct()
                .map(suspect -> DataValidationKpiImpl.DataValidationKpiMembers.SUSPECT.fieldName() + suspect)
                .collect(Collectors.toMap(suspect -> suspect,
                        s -> registerSuspects.get(s.replace(DataValidationKpiImpl.DataValidationKpiMembers.SUSPECT.fieldName(), DataValidationKpiImpl.DataValidationKpiMembers.REGISTER
                                .fieldName()))
                                .add(channelsSuspects.get(s.replace(DataValidationKpiImpl.DataValidationKpiMembers.SUSPECT.fieldName(), DataValidationKpiImpl.DataValidationKpiMembers.CHANNELS
                                        .fieldName())))
                ));
    }

}
