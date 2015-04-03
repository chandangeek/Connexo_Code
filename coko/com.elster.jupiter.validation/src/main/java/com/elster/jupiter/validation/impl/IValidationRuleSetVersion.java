package com.elster.jupiter.validation.impl;


import com.elster.jupiter.validation.ValidationRuleSetVersion;

import java.util.List;

public interface IValidationRuleSetVersion extends ValidationRuleSetVersion{

    List<IValidationRule> getRules();
    List<IValidationRule> getRules(int start, int limit);

}
