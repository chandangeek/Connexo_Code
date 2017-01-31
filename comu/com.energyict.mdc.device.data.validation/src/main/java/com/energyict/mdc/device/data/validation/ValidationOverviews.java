/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
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

    List<ValidationOverview> allOverviews();

}