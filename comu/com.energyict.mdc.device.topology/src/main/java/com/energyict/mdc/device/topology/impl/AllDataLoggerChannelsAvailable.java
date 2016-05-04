package com.energyict.mdc.device.topology.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Constraint that indicates that a Device can not be his own gateway.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 3:56 PM
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {AllDataLoggerChannelsAvailableValidator.class})
public @interface AllDataLoggerChannelsAvailable {
    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}

