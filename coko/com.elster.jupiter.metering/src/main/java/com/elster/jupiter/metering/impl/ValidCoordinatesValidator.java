package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.geo.SpatialCoordinates;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by david on 4/28/2016.
 */
public class ValidCoordinatesValidator implements ConstraintValidator<ValidCoordinates, SpatialCoordinates> {

    @Override
    public void initialize(ValidCoordinates deviceConfigurationIsPresentAndActive) {

    }

    @Override
    public boolean isValid(SpatialCoordinates coordinates, ConstraintValidatorContext constraintValidatorContext) {
        return true;
    }
}
