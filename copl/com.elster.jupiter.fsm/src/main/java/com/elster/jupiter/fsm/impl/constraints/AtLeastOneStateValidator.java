package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.impl.FinateStateMachineImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link AtLeastOneState} constraint against a {@link FinateStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (11:08)
 */
public class AtLeastOneStateValidator implements ConstraintValidator<AtLeastOneState, FinateStateMachine> {

    @Override
    public void initialize(AtLeastOneState constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(FinateStateMachine stateMachine, ConstraintValidatorContext context) {
        if (stateMachine.getStates().isEmpty()) {
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(FinateStateMachineImpl.Fields.STATES.fieldName()).addConstraintViolation()
                .disableDefaultConstraintViolation();
            return false;
        }
        else {
            return true;
        }
    }

}