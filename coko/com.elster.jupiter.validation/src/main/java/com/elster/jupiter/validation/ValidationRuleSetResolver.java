/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@ConsumerType
public interface ValidationRuleSetResolver {

    Map<ValidationRuleSet, List<Range<Instant>>> resolve(ValidationContext validationContext);

    boolean isValidationRuleSetInUse(ValidationRuleSet ruleset);
}
