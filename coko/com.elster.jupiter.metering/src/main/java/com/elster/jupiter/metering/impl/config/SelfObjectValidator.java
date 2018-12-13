/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import javax.validation.ConstraintValidatorContext;

public interface SelfObjectValidator {

    boolean validate(ConstraintValidatorContext context);
}
