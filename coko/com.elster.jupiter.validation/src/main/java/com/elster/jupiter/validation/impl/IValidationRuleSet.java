package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.List;

public interface IValidationRuleSet extends ValidationRuleSet {

    List<IValidationRule> getRules();
    List<IValidationRuleSetVersion> getVersions();

}
