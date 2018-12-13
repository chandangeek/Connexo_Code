/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.WebServiceDestination;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ChangeEndPointIsSetForExportOfUpdatedDataValidator implements ConstraintValidator<ChangeEndPointIsSetForExportOfUpdatedData, ExportTaskImpl> {
    private String messageTemplate;

    @Override
    public void initialize(ChangeEndPointIsSetForExportOfUpdatedData annotation) {
        messageTemplate = annotation.message();
    }

    @Override
    public boolean isValid(ExportTaskImpl exportTask, ConstraintValidatorContext context) {
        if (exportTask.getReadingDataSelectorConfig()
                .map(ReadingDataSelectorConfigImpl::isExportUpdate)
                .filter(Boolean::booleanValue)
                .isPresent()
                && exportTask.getDestinations().stream()
                .filter(WebServiceDestination.class::isInstance)
                .map(WebServiceDestination.class::cast)
                .anyMatch(destination -> !destination.getChangeWebServiceEndpoint().isPresent())) {
            context.buildConstraintViolationWithTemplate(messageTemplate)
                    .addPropertyNode("destinations")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}
