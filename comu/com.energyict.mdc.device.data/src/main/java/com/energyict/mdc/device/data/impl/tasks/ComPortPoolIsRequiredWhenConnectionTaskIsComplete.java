package com.energyict.mdc.device.data.impl.tasks;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraint that the ComPort is present when the ConnectionTask is not marked as Incomplete.
 *
 * Copyrights EnergyICT
 * Date: 7/1/14
 * Time: 10:26 AM
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ComPortPoolIsRequiredWhenConnectionTaskIsCompleteValidator.class })
public @interface ComPortPoolIsRequiredWhenConnectionTaskIsComplete {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
