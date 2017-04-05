/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ChannelsContainer;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;

@ConsumerType
public interface ValidationPropertyResolver {

    /**
     * Provides a {@link ValidationPropertyProvider} for a specific {@link ChannelsContainer}
     *
     * @param channelsContainer target {@link ChannelsContainer}
     * @return {@link ValidationPropertyProvider} or Optional.empty()
     * if no properties are overridden on the {@link ValidationPropertyDefinitionLevel} returned by {@link ValidationPropertyResolver#getLevel()}
     */
    Optional<ValidationPropertyProvider> resolve(ChannelsContainer channelsContainer);

    /**
     * Returns a {@link ValidationPropertyDefinitionLevel} specifying
     * on which level this {@link ValidationPropertyResolver} gets validation properties
     */
    ValidationPropertyDefinitionLevel getLevel();

}
