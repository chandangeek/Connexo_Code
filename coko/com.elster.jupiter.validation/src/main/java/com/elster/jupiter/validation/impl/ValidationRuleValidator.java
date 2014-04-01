package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.ValidatorNotFoundException;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class ValidationRuleValidator implements ConstraintValidator<ValidValidationRule, ValidationRule> {

        private ValidationService validationService;

        @Inject
        public ValidationRuleValidator(ValidationService validationService) {
            this.validationService = validationService;
        }

         @Override
         public void initialize(ValidValidationRule constraintAnnotation) {

         }

         @Override
         public boolean isValid(ValidationRule validationRule, ConstraintValidatorContext context) {
            return true;
         }

}
