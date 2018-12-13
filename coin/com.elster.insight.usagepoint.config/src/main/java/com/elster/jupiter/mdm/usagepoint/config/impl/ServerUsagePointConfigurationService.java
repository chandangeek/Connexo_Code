/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.List;

public interface ServerUsagePointConfigurationService extends UsagePointConfigurationService {

    List<ValidationRuleSet> getValidationRuleSets(State state);
}
