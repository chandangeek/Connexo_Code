package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class EstimationRuleSetInUseLocalizedException extends LocalizedException {

    public EstimationRuleSetInUseLocalizedException(Thesaurus thesaurus, EstimationRuleSet estimationRuleSet) {
        super(thesaurus, MessageSeeds.RULE_SET_IN_USE, estimationRuleSet.getName());
    }
}
