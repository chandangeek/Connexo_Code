package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 17/11/2014
 * Time: 10:44
 */
public class UniqueReadingTypeDataExportTaskNameValidator implements ConstraintValidator<UniqueName, ReadingTypeDataExportTask> {

    private final IDataExportService dataExportService;

    @Inject
    public UniqueReadingTypeDataExportTaskNameValidator(IDataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Override
    public void initialize(UniqueName uniqueName) {

    }

    @Override
    public boolean isValid(ReadingTypeDataExportTask readingTypeDataExportTask, ConstraintValidatorContext context) {
        List<? extends ReadingTypeDataExportTask> existing = dataExportService.getReadingTypeDataExportTaskQuery().select(Where.where("name").isEqualTo(readingTypeDataExportTask.getName()));
        if (existing.isEmpty()) {
            return true;
        } else if (existing.size() == 1 && readingTypeDataExportTask.getId() == existing.get(0).getId()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("name").addConstraintViolation();
        return false;
    }
}
