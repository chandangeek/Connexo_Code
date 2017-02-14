/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.ValidationRuleSet;

public class ValidationRuleSetInUseLocalizedException extends LocalizedException {

    public ValidationRuleSetInUseLocalizedException(Thesaurus thesaurus, ValidationRuleSet validationRuleSet) {
        super(thesaurus, MessageSeeds.RULE_SET_IN_USE, validationRuleSet.getName());
    }
}
