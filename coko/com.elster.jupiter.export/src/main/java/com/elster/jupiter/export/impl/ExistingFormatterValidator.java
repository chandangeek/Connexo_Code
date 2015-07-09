package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataFormatterFactory;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link IsExistingFormatter} constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-01 (16:47)
 */
public class ExistingFormatterValidator implements ConstraintValidator<IsExistingFormatter, String> {

    private final IDataExportService dataExportService;

    @Inject
    public ExistingFormatterValidator(IDataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Override
    public void initialize(IsExistingFormatter annotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(String formatterName, ConstraintValidatorContext context) {
        return dataExportService.getAvailableFormatters().stream()
                .map(DataFormatterFactory::getName)
                .anyMatch(name -> name.equals(formatterName));
    }

}