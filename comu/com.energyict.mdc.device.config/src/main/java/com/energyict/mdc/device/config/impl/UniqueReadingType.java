package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.protocol.api.device.MultiplierMode;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraint that the reading type of a {@link com.energyict.mdc.device.config.ProductSpec}
 * should be unique for all ProductSpecs, i.e. there cannot be 2 ProductSpecs with the same reading type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-04 (16:39)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { UniqueReadingTypeValidator.class })
public @interface UniqueReadingType {

    String message() default "{" + MessageSeeds.Constants.READING_TYPE_ALREADY_EXISTS_KEY + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}