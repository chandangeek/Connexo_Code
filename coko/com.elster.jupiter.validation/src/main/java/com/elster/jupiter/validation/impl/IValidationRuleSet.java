/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.List;

public interface IValidationRuleSet extends ValidationRuleSet {

    List<IValidationRule> getRules();
    List<IValidationRuleSetVersion> getRuleSetVersions();
    List<IValidationRuleSetVersion> getRuleSetVersions(int start, int limit);

}
