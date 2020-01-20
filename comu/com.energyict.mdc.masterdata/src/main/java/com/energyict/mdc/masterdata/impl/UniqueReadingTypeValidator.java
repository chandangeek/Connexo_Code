/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.common.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if (xMeasurementType.isPresent() && xMeasurementType.get().getId() != value.getId()
                && haveSameDiscriminator(xMeasurementType.get(), value)) {
            context.disableDefaultConstraintViolation();
            context.
                    buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.REGISTER_TYPE_DUPLICATE_READING_TYPE + "}").
                    addPropertyNode(MeasurementTypeImpl.Fields.READING_TYPE.fieldName()).
                    addConstraintViolation();
            return false;
        }
        return true;
    }

    private Optional<String> getMeasurementTypeDiscriminator(MeasurementType value) {
        return MeasurementTypeImpl.IMPLEMENTERS.keySet().stream()
                .filter(d -> value != null && value.getClass().isAssignableFrom(MeasurementTypeImpl.IMPLEMENTERS.get(d)))
                .findFirst();
    }

    private boolean haveSameDiscriminator(MeasurementType... values) {
        Map<String, List<MeasurementType>> valuesDiscrimintors = Stream.of(values)
                .collect(Collectors.groupingBy(value -> getMeasurementTypeDiscriminator(value).orElse("")));
        return valuesDiscrimintors.size() == 1 && valuesDiscrimintors.values().iterator().next().size() == values.length;
    }

}