package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.validation.kpi.DataValidationReportService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;


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
        dataValidationKpi.getDataValidationKpi().getMembers().stream()
                .forEach(member -> {
                    member.score(timestamp, new BigDecimal(100));
                    member.score(timestamp, new BigDecimal(101));
                });
        logger.log(Level.INFO, ">>>>>>>>>>> CalculateAndStore !!!");

    }
}
