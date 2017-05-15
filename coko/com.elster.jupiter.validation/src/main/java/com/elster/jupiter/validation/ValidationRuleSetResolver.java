/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.RangeSet;

import java.time.Instant;
import java.util.Map;

@ConsumerType
public interface ValidationRuleSetResolver {

    Map<ValidationRuleSet, RangeSet<Instant>> resolve(ValidationContext validationContext);

    boolean isValidationRuleSetInUse(ValidationRuleSet ruleset);
}
