package com.elster.insight.usagepoint.config;

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
    }

    Mode getMode();

    /**
     * Gets the human readable version of the formula.
     * This is a temporary feature that is used to
     * render the actual expression tree in the UI.
     *
     * @return The human readable version of this Formula
     * @deprecated Will be dropped as soon as the UI has support for rederning the expression tree
     */
    @Deprecated
    String getDescription();

    /**
     * Delete this {@link Formula}
     */
    void delete();

    /**
     * Update this {@link Formula}
     */
    void save();

}