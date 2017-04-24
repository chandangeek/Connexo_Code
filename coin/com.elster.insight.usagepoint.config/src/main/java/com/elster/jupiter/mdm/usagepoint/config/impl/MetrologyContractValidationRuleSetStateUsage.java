/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.validation.ValidationRuleSet;

/**
 * Models the link between {@link MetrologyContractValidationRuleSetUsage} and {@link com.elster.jupiter.fsm.State}.
 */
public interface MetrologyContractValidationRuleSetStateUsage {

    MetrologyContractValidationRuleSetUsage getMetrologyContractValidationRuleSetUsage();

    State getState();
}