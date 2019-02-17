/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.util.HasName;

import java.util.Collections;
import java.util.Set;

public interface MicroCheckNew extends HasName {

    String getKey();

    String getDescription();

    String getCategory();

    String getCategoryName();

    /**
     * Contains set of default transition's which use micro check as optional
     *
     * @return set of default transition's
     */
    default Set<DefaultTransition> getOptionalDefaultTransitions() {
        return Collections.emptySet();
    }

    /**
     * Contains set of default transition's which use micro check as required
     *
     * @return set of default transition's
     */
    default Set<DefaultTransition> getRequiredDefaultTransitions() {
        return Collections.emptySet();
    }

    /**
     * Marks micro check as applicable for specific transition between two states
     *
     * @return true if micro check is applicable
     */
    default boolean isApplicableForTransition(State fromState, State toState) {
        return isOptionalForTransition(fromState, toState) || isRequiredForTransition(fromState, toState);
    }

    /**
     * Marks micro check as optional for specific transition between two states
     *
     * @return true if micro check is optional
     */
    default boolean isOptionalForTransition(State fromState, State toState) {
        return isForTransition(fromState, toState, getOptionalDefaultTransitions());
    }

    /**
     * Marks micro check as required for specific transition between two states
     *
     * @return true if micro check is required
     */
    default boolean isRequiredForTransition(State fromState, State toState) {
        return isForTransition(fromState, toState, getRequiredDefaultTransitions());
    }

    /**
     * Marks micro check as exist for specific transition between two states
     *
     * @return true if micro check is exist
     */
    default boolean isForTransition(State fromState, State toState, Set<DefaultTransition> defaultTransitions) {
        return DefaultTransition.getDefaultTransition(fromState, toState)
                .filter(defaultTransitions::contains)
                .isPresent();
    }
}