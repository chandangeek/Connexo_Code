package com.elster.jupiter.orm.associations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target( { FIELD , ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = IsPresentValidator.class)
@Documented

public @interface IsPresent {
	
    String message() default "{com.elster.jupiter.orm.associations.isPresent}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
}

