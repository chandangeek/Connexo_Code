/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

/**
 * Created by igh on 17/03/2016.
 */
@ProviderType
public interface FunctionCallNode extends ExpressionNode {

    Function getFunction();

    /**
     * Gets the {@link AggregationLevel} iff this FunctionCallNode
     * is for a function that requires an argument of type AggregationLevel.
     *
     * @return A flag that indicates if the called Function {@link Function#requiresAggregationLevel() requires}
     *         an argument of type AggregationLevel
     */
    Optional<AggregationLevel> getAggregationLevel();

}