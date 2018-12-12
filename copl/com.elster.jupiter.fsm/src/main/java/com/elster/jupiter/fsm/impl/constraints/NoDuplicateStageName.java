/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Models the constraint that a {@link com.elster.jupiter.fsm.StageSet}
 * cannot have a {@Link Stage} with a duplicate name.
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {NoDuplicateStageNameValidator.class})
public @interface NoDuplicateStageName {

    String message() default "{" + MessageSeeds.Keys.NO_DUPLICATE_STAGE_NAME + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}