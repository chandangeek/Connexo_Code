package com.energyict.mdc.device.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the validation constraints for a {@link com.energyict.mdc.device.config.TemporalExpression}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-06 (08:51)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasTemporalExpressionValidator.class })
public @interface HasValidTemporalExpression {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}