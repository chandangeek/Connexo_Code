/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface ValidationRuleSetResolver {

    List<ValidationRuleSet> resolve(ValidationContext validationContext);

    boolean isValidationRuleSetInUse(ValidationRuleSet ruleset);
}
