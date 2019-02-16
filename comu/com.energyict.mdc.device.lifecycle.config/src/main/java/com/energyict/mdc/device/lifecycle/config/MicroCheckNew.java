/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.util.HasName;

public interface MicroCheckNew extends HasName {

    String getKey();

    String getDescription();

    String getCategory();

    String getCategoryName();

    /**
     * Marks micro check as mandatory for specific transition between two states
     *
     * @return true if micro check is mandatory
     */
    default boolean isMandatoryForTransition(State fromState, State toState) {
        return false;
    }
}