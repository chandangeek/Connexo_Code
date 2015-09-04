package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Copyrights EnergyICT
 * Date: 18/04/14
 * Time: 09:00
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { UniqueComSchedulePerDeviceValidator.class })
public @interface UniqueComSchedulePerDevice {

    String message() default "{" + MessageSeeds.Keys.DUPLICATE_COMTASK_SCHEDULING + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
