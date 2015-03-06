package com.energyict.mdc.device.data.impl.constraintvalidators;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

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
