package com.elster.jupiter.properties;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.elster.jupiter.properties.impl.HasValidPropertiesValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasValidPropertiesValidator.class })
public @interface HasValidProperties {

    String message() default "";
    
    String propertyNotInSpecMessage() default "";
    
    String requiredPropertyMissingMessage() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}