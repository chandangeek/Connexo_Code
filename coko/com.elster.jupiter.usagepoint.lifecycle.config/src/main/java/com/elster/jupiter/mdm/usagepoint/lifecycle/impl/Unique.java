package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Models the constraint that an object should be unique.
 * It is up to the validator to choose the property or properties
 * of the object that make it unique.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-19 (13:07)
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {UniqueUsagePointLifeCycleNameValidator.class, UniqueUsagePointTransitionValidator.class})
public @interface Unique {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}