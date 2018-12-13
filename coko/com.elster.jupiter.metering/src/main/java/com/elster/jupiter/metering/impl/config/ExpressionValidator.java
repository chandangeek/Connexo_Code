/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.orm.associations.Reference;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ExpressionValidator implements ConstraintValidator<ValidExpression, Reference<ServerFormula>> {

    @Override
    public void initialize(ValidExpression constraintAnnotation) {
    }

    @Override
    public boolean isValid(Reference<ServerFormula> formula, ConstraintValidatorContext context) {
        try {
            if (formula.isPresent()) {
                if (formula.get().getMode().equals(Formula.Mode.AUTO)) {
                    formula.get().getExpressionNode().validate();
                }
            }
            return true;
        } catch (InvalidNodeException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        }
    }

}