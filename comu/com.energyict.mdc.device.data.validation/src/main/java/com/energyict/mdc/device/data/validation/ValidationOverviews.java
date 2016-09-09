/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Models a set of {@link ValidationOverview}s
 * organized by {@link EndDeviceGroup}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-07 (13:46)
 */
@ProviderType
public interface ValidationOverviews {

    /**
     * Returns the List of {@link EndDeviceGroup}s
     * for which a {@link ValidationOverview} is available.
     *
     * @return The List of ValidationOverview
     */
    List<EndDeviceGroup> getGroups();

    /**
     * Gets the {@link ValidationOverview}s for all the {@link com.energyict.mdc.device.data.Device}s
     * in the specified {@link EndDeviceGroup}.
     * This will throw an {@link IllegalArgumentException} if the specified
     * EndDeviceGroup was not previously returned by {@link #getGroups()}.
     *
     * @param group The {@link EndDeviceGroup}
     * @return The ValidationOverview
     * @throws IllegalArgumentException Thrown if the specified group was not previously returned by getGroups()
     */
    List<ValidationOverview> getDeviceOverviews(EndDeviceGroup group);

    List<ValidationOverview> allOverviews();

}