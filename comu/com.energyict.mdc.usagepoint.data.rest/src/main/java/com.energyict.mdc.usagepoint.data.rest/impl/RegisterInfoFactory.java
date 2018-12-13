/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

/**
 * Factory to represents register info object
 */
public interface RegisterInfoFactory extends ChannelDataFactory<UsagePointRegisterInfo, UsagePointRegisterType> {
    @Override
    default UsagePointRegisterType getChannelType() {
        return new UsagePointRegisterType();
    }
}
