/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.mdm.usagepoint.config.rest.EstimationRuleSetInfo;

public class PurposeEstimationRuleSetInfo extends EstimationRuleSetInfo {

    public boolean isActive;

    public PurposeEstimationRuleSetInfo() {

    }

    PurposeEstimationRuleSetInfo(EstimationRuleSet ruleSet, boolean isActive){
        super(ruleSet);
        this.isActive = isActive;
    }
}
