/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

/**
 * Factory to represent channel info object
 */
public interface ChannelInfoFactory extends ChannelDataFactory<UsagePointChannelInfo, UsagePointChannelType> {

    @Override
    default UsagePointChannelType getChannelType(){
        return new UsagePointChannelType();
    }
}
