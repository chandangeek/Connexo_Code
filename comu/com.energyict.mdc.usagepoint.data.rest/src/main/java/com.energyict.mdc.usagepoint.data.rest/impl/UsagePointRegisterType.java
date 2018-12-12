/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;

import java.util.function.Predicate;

/**
 * Represents usage point register type
 */
public class UsagePointRegisterType implements UsagePointChannelRepresentationType {

    @Override
    public Predicate<Channel> getFilterPredicate() {
        return channel -> !channel.isRegular();
    }

    @Override
    public MessageSeeds getNoSuchElementMessageSeed() {
        return MessageSeeds.NO_SUCH_REGISTER_FOR_USAGE_POINT;
    }
}
