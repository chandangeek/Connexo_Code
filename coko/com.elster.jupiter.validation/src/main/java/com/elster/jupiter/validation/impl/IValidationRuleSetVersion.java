package com.elster.jupiter.validation.impl;


import com.elster.jupiter.validation.ValidationRuleSetVersion;

import java.time.Instant;
import java.util.List;

public interface IValidationRuleSetVersion extends ValidationRuleSetVersion{

    List<IValidationRule> getRules();

    Instant getNotNullStartDate();
    void setEndDate(Instant endDate);
    Instant getNotNullEndDate();
}
