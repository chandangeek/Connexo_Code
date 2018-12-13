/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FormatterCompliesWithDestinationsValidator implements ConstraintValidator<FormatterCompliesWithDestinations, ExportTaskImpl> {
    private String messageTemplate;

    @Override
    public void initialize(FormatterCompliesWithDestinations annotation) {
        messageTemplate = annotation.message();
    }

    @Override
    public boolean isValid(ExportTaskImpl exportTask, ConstraintValidatorContext context) {
        if (NullDataFormatterFactory.getNameTranslationKey().getKey().equals(exportTask.getDataFormatterFactory().getName())
                && exportTask.getDestinations().stream().anyMatch(destination -> Destination.Type.DATA != destination.getType())) {
            context.buildConstraintViolationWithTemplate(messageTemplate)
                    .addPropertyNode("destinations")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}
