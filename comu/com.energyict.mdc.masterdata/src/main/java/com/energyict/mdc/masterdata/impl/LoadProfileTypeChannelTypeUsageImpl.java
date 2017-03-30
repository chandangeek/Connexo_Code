/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeChannelTypeUsage;
import com.energyict.mdc.masterdata.MeasurementType;

import java.time.Instant;

/**
 * Models the fact that a {@link LoadProfileType} uses a ChannelType.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class LoadProfileTypeChannelTypeUsageImpl implements LoadProfileTypeChannelTypeUsage {
    private Reference<LoadProfileType> loadProfileType = ValueReference.absent();
    private Reference<ChannelType> channelType = ValueReference.absent();
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    // For ORM layer only
    LoadProfileTypeChannelTypeUsageImpl() {
        super();
    }

    LoadProfileTypeChannelTypeUsageImpl(LoadProfileType loadProfileType, ChannelType channelType) {
        this();
        this.loadProfileType.set(loadProfileType);
        this.channelType.set(channelType);
    }

    @Override
    public LoadProfileType getLoadProfileType() {
        return loadProfileType.get();
    }

    @Override
    public ChannelType getChannelType() {
        return channelType.get();
    }

    public boolean sameChannelType(MeasurementType measurementType) {
        return this.getChannelType().getId() == measurementType.getId();
    }

}