/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config;

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
     * @return A set of {@link DefaultTransition} where this check should be available. Default is none of default ones.
     */
    default Set<DefaultTransition> getOptionalDefaultTransitions() {
        return Collections.emptySet();
    }

    /**
     * @return A set of {@link DefaultTransition} where this check is mandatory (can't be unselected by user). Default is none of default ones.
     */
    default Set<DefaultTransition> getRequiredDefaultTransitions() {
        return Collections.emptySet();
    }

    /**
     * Service method; should not be overridden.
     */
    default boolean isApplicableForTransition(State fromState, State toState) {
        return isOptionalForTransition(fromState, toState) || isRequiredForTransition(fromState, toState);
    }

    /**
     * @return {@code true} if this check should be available for the transition represented by {code fromState} and {code toState}.
     * Default is: available for all default transitions returned from {@link #getOptionalDefaultTransitions()} plus for all custom transitions.
     */
    default boolean isOptionalForTransition(State fromState, State toState) {
        return isForTransition(fromState, toState, getOptionalDefaultTransitions()) || !DefaultTransition.getDefaultTransition(fromState, toState).isPresent();
    }

    /**
     * @return {@code true} if this check should be mandatory (can't be unselected by user) for the transition represented by {code fromState} and {code toState}.
     * Default is: mandatory for all default transitions returned from {@link #getRequiredDefaultTransitions()}, and only.
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
