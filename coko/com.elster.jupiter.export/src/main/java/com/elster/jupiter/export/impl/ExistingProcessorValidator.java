package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.ReadingTypeDataExportTask;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.elster.jupiter.util.Checks.is;

/**
 * Validates the {@link IsExistingProcessor} constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-01 (16:47)
 */
public class ExistingProcessorValidator implements ConstraintValidator<IsExistingProcessor, ReadingTypeDataExportTask> {

    private final DataExportService dataExportService;

    @Inject
    public ExistingProcessorValidator(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Override
    public void initialize(IsExistingProcessor annotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ReadingTypeDataExportTask task, ConstraintValidatorContext context) {
        if (is(task.getDataFormatter()).empty()) {
            return true;
        }
        return dataExportService.getAvailableProcessors().stream()
                .map(DataProcessorFactory::getName)
                .anyMatch(task.getDataFormatter()::equals);
    }

}