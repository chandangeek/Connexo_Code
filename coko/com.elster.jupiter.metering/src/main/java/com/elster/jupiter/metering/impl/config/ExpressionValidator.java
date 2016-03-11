package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.orm.associations.Reference;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ExpressionValidator implements ConstraintValidator<ValidExpression, Reference<ExpressionNode>> {

    @Override
    public void initialize(ValidExpression constraintAnnotation) {
    }

    @Override
    public boolean isValid(Reference<ExpressionNode> node, ConstraintValidatorContext context) {
        try {
            if (node.isPresent()) {
                node.get().validate();
            }
            return true;
        } catch (InvalidNodeException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        }
    }
}
