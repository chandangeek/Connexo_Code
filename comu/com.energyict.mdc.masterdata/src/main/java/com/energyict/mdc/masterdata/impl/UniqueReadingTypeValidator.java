/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueReadingTypeValidator implements ConstraintValidator<UniqueReadingType, MeasurementType> {

    private final MasterDataService masterDataService;

    @Inject
    public UniqueReadingTypeValidator(MasterDataService masterDataService) {
        super();
        this.masterDataService = masterDataService;
    }

    @Override
    public void initialize(UniqueReadingType constraintAnnotation) {
    }

    @Override
    public boolean isValid(MeasurementType value, ConstraintValidatorContext context) {
        Optional<MeasurementType> xMeasurementType = this.masterDataService.findMeasurementTypeByReadingType(value.getReadingType());
        if (xMeasurementType.isPresent() && xMeasurementType.get().getId() != value.getId()) {
            context.disableDefaultConstraintViolation();
            context.
                buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.REGISTER_TYPE_DUPLICATE_READING_TYPE + "}").
                addPropertyNode(MeasurementTypeImpl.Fields.READING_TYPE.fieldName()).
                addConstraintViolation();
            return false;
        }
        return true;
    }

}