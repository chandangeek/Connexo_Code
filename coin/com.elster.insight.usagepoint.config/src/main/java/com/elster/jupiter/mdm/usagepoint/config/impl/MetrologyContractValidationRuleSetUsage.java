/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.List;

/**
 * Models the link between {@link MetrologyContract} and {@link ValidationRuleSet}.
 */
public interface MetrologyContractValidationRuleSetUsage {

    MetrologyContract getMetrologyContract();

    ValidationRuleSet getValidationRuleSet();

    List<State> getStates();
}