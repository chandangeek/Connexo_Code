package com.elster.jupiter.fsm.impl.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.elster.jupiter.fsm.MessageSeeds.Keys.CAN_NOT_BE_INACTIVE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsActiveEndPointConfigurationReferenceValidator.class})
public @interface IsActive {
    String message() default  "{" + CAN_NOT_BE_INACTIVE + "}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
