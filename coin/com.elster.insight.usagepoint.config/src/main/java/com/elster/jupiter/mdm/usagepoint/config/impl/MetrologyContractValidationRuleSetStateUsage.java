/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.fsm.State;
/**
 * Models the link between {@link MetrologyContractValidationRuleSetUsage} and {@link State}.
 */
public interface MetrologyContractValidationRuleSetStateUsage {

    MetrologyContractValidationRuleSetUsage getMetrologyContractValidationRuleSetUsage();

    State getState();
}