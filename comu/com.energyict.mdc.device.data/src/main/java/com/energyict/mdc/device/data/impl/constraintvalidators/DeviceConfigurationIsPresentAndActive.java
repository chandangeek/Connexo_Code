package com.energyict.mdc.device.data.impl.constraintvalidators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Copyrights EnergyICT
 * Date: 7/7/14
 * Time: 11:55 AM
 */
@Target( { FIELD , ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { DeviceConfigIsPresentAndActiveValidator.class })
public @interface DeviceConfigurationIsPresentAndActive {
    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
