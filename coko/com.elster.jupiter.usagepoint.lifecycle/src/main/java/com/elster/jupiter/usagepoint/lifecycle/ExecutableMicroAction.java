/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.Map;

@ConsumerType
public interface ExecutableMicroAction extends MicroAction {

    /**
     * Perform {@link MicroAction} on specific {@link UsagePoint} at given time.
     *
     * @param usagePoint target object
     * @param transitionTime point in time when transition occurs
     * @param properties contains all properties (i.e. this map can contain properties from other actions)
     */
    void execute(UsagePoint usagePoint, Instant transitionTime, Map<String, Object> properties) throws ExecutableMicroActionException;
}
