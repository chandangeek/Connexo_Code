/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.common.TimeDuration;
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
