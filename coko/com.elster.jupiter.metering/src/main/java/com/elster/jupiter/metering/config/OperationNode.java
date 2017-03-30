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
public interface OperationNode extends ExpressionNode {

    Operator getOperator();

    ExpressionNode getLeftOperand();

    ExpressionNode getRightOperand();

    /**
     * Gets the {@link ExpressionNode} that will be used as
     * a replacement for zero values in the divisor ExpressionNode
     * for the {@link Operator#SAFE_DIVIDE} operation.
     *
     * @return The replacement expression or empty when the Operation is not safe divide.
     */
    Optional<ExpressionNode> getZeroReplacement();

}