/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the formula of a {@link ReadingTypeDeliverable}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (12:52)
 */
@ProviderType
public interface Formula extends HasId {

    enum Mode {
        /**
         * Switches on type, unit and time based aggregation of
         * values calculated by a Formula and disables usage
         * of complex functions at the same time as those
         * will cause errors when combined with the auto mechanism.
         */
        AUTO,

        /**
         * Enables the usage of complex functions and disables
         * automatic type, unit and time based aggregation of
         * values. If any of those are needed then the user
         * will have to do those manually by using the appropriate
         * complex functions on the elements of the formula
         * that need type, unit or time based aggregation.
         */
        EXPERT;

        /**
         * Tests if this Mode supports the specified {@link Function}.
         *
         * @param function The Function
         * @return A flag that indicates if this Mode supports the Function
         */
        public boolean supports(Function function) {
            return function.supportedBy(this);
        }

    }

    Mode getMode();

    /**
     * Returns the {@link ExpressionNode} that
     * represents the way this Formula should be calculated.
     *
     * @return The ExpressionNode
     */
    ExpressionNode getExpressionNode();

    /**
     * Update this {@link Formula}
     */
    void save();

}