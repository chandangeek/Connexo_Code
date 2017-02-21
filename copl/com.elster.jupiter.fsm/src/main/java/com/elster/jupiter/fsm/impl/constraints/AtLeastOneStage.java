/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.StageSet;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Models the constraint that a {@link StageSet}
 * should have at least one {@link Stage}.
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {AtLeastOneStageValidator.class})
public @interface AtLeastOneStage {

    String message() default "{" + MessageSeeds.Keys.AT_LEAST_ONE_STAGE + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}