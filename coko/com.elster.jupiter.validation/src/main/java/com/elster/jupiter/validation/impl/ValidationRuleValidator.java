package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRule;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidationRuleValidator implements ConstraintValidator<ValidValidationRule, ValidationRule> {

        @Inject
        public ValidationRuleValidator() {

        }

         @Override
         public void initialize(ValidValidationRule constraintAnnotation) {

         }

         @Override
         public boolean isValid(ValidationRule validationRule, ConstraintValidatorContext context) {
            return true;
         }

}
