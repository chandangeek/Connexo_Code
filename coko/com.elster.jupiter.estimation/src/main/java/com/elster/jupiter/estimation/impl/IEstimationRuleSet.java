/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ReadingType;

import java.util.List;
import java.util.Set;

public interface IEstimationRuleSet extends EstimationRuleSet {

    @Override
    List<IEstimationRule> getRules();

    @Override
    List<IEstimationRule> getRules(Set<? extends ReadingType> readingTypes);
}
