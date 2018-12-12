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
 * Models the constraint that a {@link FiniteStateMachine}
 * should have exactly one initial {@link com.elster.jupiter.fsm.State}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (10:33)
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {ExactlyOneInitialStateValidator.class})
public @interface ExactlyOneInitialState {

    String message() default "{" + MessageSeeds.Keys.EXACTLY_ONE_INITIAL_STATE + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}