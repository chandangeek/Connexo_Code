/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates the constraint that an {@link com.elster.jupiter.metering.groups.EndDeviceGroup}
 * is used in maximum one {@link com.energyict.mdc.device.data.kpi.DataCollectionKpi}
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { MustHaveUniqueEndDeviceGroupValidator.class })
public @interface MustHaveUniqueEndDeviceGroup {

    String message();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
