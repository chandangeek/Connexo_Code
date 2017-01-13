package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.config.MetrologyContract;

/**
 * Models the link between {@link MetrologyContract} and {@link EstimationRuleSet}.
 */
public interface MetrologyContractEstimationRuleSetUsage {

    MetrologyContract getMetrologyContract();

    EstimationRuleSet getEstimationRuleSet();

    long getPosition();

    void setPosition(long position);
}