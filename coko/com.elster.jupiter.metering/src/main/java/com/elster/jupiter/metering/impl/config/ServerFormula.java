/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.impl.aggregation.IntervalLength;

import java.util.List;

/**
 * Adds behavior to {@link Formula} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:00)
 */
public interface ServerFormula extends Formula {

    /**
     * Delete this {@link Formula}
     */
    void delete();

    /**
     * Get the greatest interval used (as requirement) in the formula
     */
    IntervalLength getIntervalLength();

    /**
     * Get all intervals used (as requirement) in the formula
     */
    List<IntervalLength> getIntervalLengths();

    @Override
    ServerExpressionNode getExpressionNode();

    void updateExpression(ServerExpressionNode nodeValue);

}