/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validates the {@link UniqueTranslation} constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-17 (10:08)
 */
public class UniqueTranslationValidator implements ConstraintValidator<UniqueTranslation, MultiplierTypeImpl> {

    private final DataModel dataModel;

    @Inject
    public UniqueTranslationValidator(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void initialize(UniqueTranslation constraintAnnotation) {
        // Currently no need to keep the annotation
    }

    @Override
    public boolean isValid(MultiplierTypeImpl multiplierType, ConstraintValidatorContext context) {
        Optional<MultiplierType> existing = dataModel.mapper(MultiplierType.class).getUnique("name", multiplierType.name(), "nameIsKey", multiplierType.nameIsKey());
        boolean valid = !existing.isPresent() || existing.get().equals(multiplierType);
        if (!valid) {
            String messageTemplate = context.getDefaultConstraintMessageTemplate();
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate(messageTemplate)
                .addPropertyNode("name")
                .addConstraintViolation();
        }
        return valid;
    }

}