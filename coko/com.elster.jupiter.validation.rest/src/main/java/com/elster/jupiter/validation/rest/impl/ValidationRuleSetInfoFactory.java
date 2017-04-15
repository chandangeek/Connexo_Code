/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;

import javax.inject.Inject;

class ValidationRuleSetInfoFactory {

    private final ValidationService validationService;

    @Inject
    ValidationRuleSetInfoFactory(ValidationService validationService) {
        this.validationService = validationService;
    }

    ValidationRuleSetInfo asInfo(ValidationRuleSet validationRuleSet) {
        return new ValidationRuleSetInfo(validationRuleSet);
    }

    ValidationRuleSetInfo asFullInfo(ValidationRuleSet validationRuleSet) {
        ValidationRuleSetInfo info = asInfo(validationRuleSet);
        info.isInUse = validationService.isValidationRuleSetInUse(validationRuleSet);
        return info;
    }
}
