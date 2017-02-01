/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;


import com.elster.jupiter.validation.ValidationRuleSetVersion;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public interface IValidationRuleSetVersion extends ValidationRuleSetVersion{

    List<IValidationRule> getRules();
    Instant getNotNullStartDate();
    Instant getNotNullEndDate();

    IValidationRule cloneRule(IValidationRule iValidationRule);
}
