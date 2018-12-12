/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class UniqueExportTaskNameValidator implements ConstraintValidator<UniqueName, ExportTask> {

    private final IDataExportService dataExportService;
    private final TaskService taskService;

    @Inject
    public UniqueExportTaskNameValidator(IDataExportService dataExportService, TaskService taskService) {
        this.dataExportService = dataExportService;
        this.taskService = taskService;
    }

    @Override
    public void initialize(UniqueName uniqueName) {

    }

    @Override
    public boolean isValid(ExportTask readingTypeDataExportTask, ConstraintValidatorContext context) {

        List<? extends RecurrentTask> existing =
                taskService.getTaskQuery().select(
                        Where.where("name").isEqualTo(readingTypeDataExportTask.getName()).and(
                                Where.where("application").isEqualTo("Pulse")));
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
