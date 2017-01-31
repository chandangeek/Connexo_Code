/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.common.rest;

import com.elster.jupiter.time.TimeDuration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MinValidatorForTimeDuration implements ConstraintValidator<MinTimeDuration, TimeDuration> {

	private TimeDuration minValue;

	public void initialize(MinTimeDuration minValue) {
		this.minValue = TimeDuration.seconds((int) minValue.value());
	}

	public boolean isValid(TimeDuration value, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( value == null ) {
			return true;
		}
        return minValue.compareTo(value)<=0;
	}
}
