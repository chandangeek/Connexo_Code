package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.GeoCoordinates;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by david on 4/28/2016.
 */
public class ValidCoordinatesValidator implements ConstraintValidator<ValidCoordinates, GeoCoordinates> {

    @Override
    public void initialize(ValidCoordinates deviceConfigurationIsPresentAndActive) {

    }

    @Override
    public boolean isValid(GeoCoordinates coordinates, ConstraintValidatorContext constraintValidatorContext) {
        return true;
    }
}
