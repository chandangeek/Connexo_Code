/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ChannelsContainer;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;

@ConsumerType
public interface EstimationPropertyResolver {

    /**
     * Provides a {@link EstimationPropertyProvider} for a specific {@link ChannelsContainer}
     *
     * @param channelsContainer target {@link ChannelsContainer}
     * @return {@link EstimationPropertyProvider} or Optional.empty()
     * if no properties are overridden on the {@link EstimationPropertyDefinitionLevel} returned by {@link EstimationPropertyResolver#getLevel()}
     */
    Optional<EstimationPropertyProvider> resolve(ChannelsContainer channelsContainer);

    /**
     * Returns a {@link EstimationPropertyDefinitionLevel} specifying
     * on which level this {@link EstimationPropertyResolver} gets validation properties
     */
    EstimationPropertyDefinitionLevel getLevel();

}
