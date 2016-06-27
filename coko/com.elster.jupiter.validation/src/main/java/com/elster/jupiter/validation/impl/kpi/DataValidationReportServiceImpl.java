package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.kpi.DataValidationReportService;


public class DataValidationReportServiceImpl implements DataValidationReportService {

    private final ValidationService validationService;

    public DataValidationReportServiceImpl(ValidationService validationService){
        this.validationService = validationService;
    }

}
