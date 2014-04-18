package com.energyict.mdc.scheduling.model.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Models the validation constraints for a {@link com.energyict.mdc.scheduling.TemporalExpression}.
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