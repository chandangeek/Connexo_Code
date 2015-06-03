package com.elster.jupiter.issue.impl.records.validator;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { CreationRulePropertiesValidator.class,
                            CreationRuleActionPropertiesValidator.class,
                            IssueActionPropertiesValidator.class })
public @interface HasValidProperties {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
