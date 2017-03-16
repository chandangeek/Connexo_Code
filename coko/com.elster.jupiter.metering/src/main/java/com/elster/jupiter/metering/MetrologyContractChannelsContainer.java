/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.MetrologyContract;

import aQute.bnd.annotation.ProviderType;

/**
 * Represents specific case of {@link ChannelsContainer}, storing usage point related
 * (output) channels grouped by common {@link MetrologyContract}.
 */
@ProviderType
public interface MetrologyContractChannelsContainer extends ChannelsContainer {
    MetrologyContract getMetrologyContract();
}
