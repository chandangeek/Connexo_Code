package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.Formula;

/**
 * Adds behavior to {@link Formula} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:00)
 */
interface ServerFormula extends Formula {

    /**
     * Returns the {@link ExpressionNode} that
     * represents the way this Formula should be calculated.
     *
     * @return The ExpressionNode
     */
    ExpressionNode expressionNode();

}