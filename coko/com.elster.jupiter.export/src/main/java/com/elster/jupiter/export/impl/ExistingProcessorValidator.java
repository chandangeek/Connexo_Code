package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataProcessorFactory;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link IsExistingProcessor} constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-01 (16:47)
 */
public class ExistingProcessorValidator implements ConstraintValidator<IsExistingProcessor, String> {

    private final IDataExportService dataExportService;

    @Inject
    public ExistingProcessorValidator(IDataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Override
    public void initialize(IsExistingProcessor annotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(String processorName, ConstraintValidatorContext context) {
        return dataExportService.getAvailableProcessors().stream()
                .map(DataProcessorFactory::getName)
                .anyMatch(name -> name.equals(processorName));
    }

}