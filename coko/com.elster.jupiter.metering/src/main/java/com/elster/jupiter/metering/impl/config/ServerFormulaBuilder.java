package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.ExpressionNodeBuilder;
import com.elster.jupiter.metering.config.FormulaBuilder;

/**
 * Adds behavior to {@link FormulaBuilder} that is reserved for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-21 (15:18)
 */
public interface ServerFormulaBuilder extends FormulaBuilder {
    FormulaBuilder init(ExpressionNodeBuilder nodeBuilder);
    FormulaBuilder init(ExpressionNode expressionNode);
}