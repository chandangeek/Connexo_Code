package com.energyict.mdc.device.data.impl.tasks;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Copyrights EnergyICT
 * Date: 16/04/14
 * Time: 09:47
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ConnectionTaskIsRequiredWhenNotUsingDefaultValidator.class })
public @interface ConnectionTaskIsRequiredWhenNotUsingDefault {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
