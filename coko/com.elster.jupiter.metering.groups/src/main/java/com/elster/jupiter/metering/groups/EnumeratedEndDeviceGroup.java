/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.conditions.Subquery;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface EnumeratedEndDeviceGroup extends EndDeviceGroup, EnumeratedGroup<EndDevice> {
    String TYPE_IDENTIFIER = "EEG";

    /**
     * @return A {@link Subquery} that gives all amrIds of {@link EndDevice EndDevices} in this group.
     * @param amrSystems {@link AmrSystem AmrSystems}.
     */
    Subquery getAmrIdSubQuery(AmrSystem... amrSystems);
}
