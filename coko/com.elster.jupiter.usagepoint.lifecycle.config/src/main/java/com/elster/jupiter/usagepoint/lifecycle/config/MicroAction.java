/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

import java.util.Collections;
import java.util.List;

/**
 * Models a number of tiny actions that will be executed by the
 * usage point life cycle engine as part of an {@link UsagePointTransition}.
 */
@ConsumerType
public interface MicroAction extends HasName {

    String getKey();

    String getDescription();

    String getCategory();

    String getCategoryName();

    /**
     * Property spec name must be unique among all micro actions available for {@link UsagePointTransition}.
     * It is good idea to append {@link #getKey()} as a prefix for property name.
     *
     * @return list of action's properties
     */
    default List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    /**
     * Marks micro action as mandatory for specific transition between two states
     *
     * @return true if micro action is mandatory
     */
    default boolean isMandatoryForTransition(UsagePointState fromState, UsagePointState toState) {
        return false;
    }
}
