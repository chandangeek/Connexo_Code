/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;

import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

/**
 * Models pre-transition checks for {@link UsagePointTransition}.
 */
@ConsumerType
public interface MicroCheck extends HasName {

    String getKey();

    String getDescription();

    String getCategory();

    String getCategoryName();

    /**
     * Marks micro check as mandatory for specific transition between two states
     *
     * @return true if micro check is mandatory
     */
    default boolean isMandatoryForTransition(UsagePointState fromState, UsagePointState toState) {
        return false;
    }
}
