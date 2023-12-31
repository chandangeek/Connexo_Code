/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.common.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

import java.util.Collections;
import java.util.Set;

@ConsumerType
public interface MicroCheck extends HasName {

    /**
     * @return Unique key of the check. {@link #equals(Object)} & {@link #hashCode()} must be implemented on the base of this key.
     */
    String getKey();

    /**
     * @return Translatable name of the check.
     */
    @Override
    String getName();

    /**
     * @return Translatable description of the check.
     */
    String getDescription();

    /**
     * @return Key of the check category. Can be taken as {@link MicroCategory#name()} or can be custom.
     */
    String getCategory();

    /**
     * @return Translatable name of the category. Can be taken as {@code thesaurus.getFormat(MicroCategoryTranslationKey.XXX).format()} or can be custom.
     */
    String getCategoryName();

    /**
     * @return A set of {@link DefaultTransition} where this check should be available. Default is none of default transitions.
     * Overriding this method without {@link #isOptionalForTransition(State, State)} would mean that the check is available on returned default transitions and on all custom ones.
     * {@link #getRequiredDefaultTransitions()} takes precedence on this method, i.e. if some transition is returned from both methods, the check is considered mandatory for it.
     */
    default Set<DefaultTransition> getOptionalDefaultTransitions() {
        return Collections.emptySet();
    }

    /**
     * @return A set of {@link DefaultTransition} where this check is mandatory (can't be unselected by user). Default is none of default ones.
     * Overriding this method without {@link #isRequiredForTransition(State, State)} would mean that the check is mandatory on returned default transitions only.
     * This method takes precedence on {@link #getOptionalDefaultTransitions()}, i.e. if some transition is returned from both methods, the check is considered mandatory for it.
     */
    default Set<DefaultTransition> getRequiredDefaultTransitions() {
        return Collections.emptySet();
    }

    /**
     * @return {@code true} if this check should be available for the transition represented by {code fromState} and {code toState}.
     * Default is: available for all default transitions returned from {@link #getOptionalDefaultTransitions()} plus for all custom transitions.
     * {@link #isRequiredForTransition(State, State)} takes precedence on this method, i.e. if for some transition both methods return true, the check is considered mandatory.
     */
    default boolean isOptionalForTransition(State fromState, State toState) {
        return isForTransition(fromState, toState, getOptionalDefaultTransitions()) || !DefaultTransition.getDefaultTransition(fromState, toState).isPresent();
    }

    /**
     * @return {@code true} if this check should be mandatory (can't be unselected by user) for the transition represented by {code fromState} and {code toState}.
     * Default is: mandatory for all default transitions returned from {@link #getRequiredDefaultTransitions()}, and only.
     * This method takes precedence on {@link #isOptionalForTransition(State, State)}, i.e. if for some transition both methods return true, the check is considered mandatory.
     */
    default boolean isRequiredForTransition(State fromState, State toState) {
        return isForTransition(fromState, toState, getRequiredDefaultTransitions());
    }

    /**
     * @return true if transition represented by {code fromState} and {code toState} is present among {code defaultTransitions}.
     */
    static boolean isForTransition(State fromState, State toState, Set<DefaultTransition> defaultTransitions) {
        return DefaultTransition.getDefaultTransition(fromState, toState)
                .filter(defaultTransitions::contains)
                .isPresent();
    }
}
