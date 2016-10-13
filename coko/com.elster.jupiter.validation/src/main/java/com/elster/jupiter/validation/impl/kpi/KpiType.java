package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ServerValidationService;
import com.elster.jupiter.validation.kpi.DataValidationKpi;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;

import java.time.Clock;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum KpiType {

    VALIDATION {
        @Override
        public String recurrentTaskName(Long id) {
            return String.valueOf(id) + " - Validation KPI";
        }

        @Override
        protected DataManagementKpiCalculator newValidationCalculator(DataValidationKpiImpl dataValidationKpi, Clock clock, ValidationService validationService) {
            DataModel dataModel = ((ServerValidationService) validationService).dataModel();
            return new DataValidationKpiCalculator(validationService, dataModel, dataValidationKpi, clock);
        }
    };

    private static final Logger LOGGER = Logger.getLogger(DataManagementKpiCalculatorHandler.class.getName());

    private static final Pattern PAYLOAD_PARSE_PATTERN = Pattern.compile("(\\w*)-(\\d*)");

    public abstract String recurrentTaskName(Long id);

    public String recurrentPayload(long id) {
        return this.name() + '-' + id;
    }

    protected abstract DataManagementKpiCalculator newValidationCalculator(DataValidationKpiImpl dataValidationKpi, Clock clock, ValidationService validationService);

    public static DataManagementKpiCalculator calculatorForRecurrentPayload(TaskOccurrence taskOccurrence, Clock clock,
                                                                            ValidationService validationService, DataValidationKpiService dataValidationKpiService) {
        String payload = taskOccurrence.getPayLoad();
        Matcher matcher = PAYLOAD_PARSE_PATTERN.matcher(payload);
        if (matcher.matches()) {
            KpiType kpiType = KpiType.valueOf(matcher.group(1));
            try {
                long id = Long.parseLong(matcher.group(2));
                Optional<DataValidationKpi> dataValidationKpi = dataValidationKpiService.findDataValidationKpi(id);
                return dataValidationKpi
                        .map(DataValidationKpiImpl.class::cast)
                        .map(kpi -> kpiType.newValidationCalculator(kpi, clock, validationService))
                        .orElseGet(() -> new DataManagementKpiDoesNotExist(LOGGER, payload));
            } catch (NumberFormatException e) {
                return new PayloadContainsInvalidId(LOGGER, payload, e);
            }
        } else {
            return new UnexpectedPayloadFormat(LOGGER, payload);
        }
    }

}