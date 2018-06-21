/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;

public class PurposeValidationRuleSetInfo extends ValidationRuleSetInfo {

    public boolean isActive;

    public PurposeValidationRuleSetInfo() {
    }

    public PurposeValidationRuleSetInfo(ValidationRuleSet ruleSet, boolean isActive) {
        super(ruleSet);
        this.isActive = isActive;
    }

}
