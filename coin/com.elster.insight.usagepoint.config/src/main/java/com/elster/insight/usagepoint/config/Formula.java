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

}