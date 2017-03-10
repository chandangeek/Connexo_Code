/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.validation.ValidationRuleSet;

/**
 * Models the link between {@link MetrologyContract} and {@link ValidationRuleSet}.
 */
public interface MetrologyContractValidationRuleSetUsage {

    MetrologyContract getMetrologyContract();

    ValidationRuleSet getValidationRuleSet();
}