/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;

import java.util.function.Predicate;

/**
 * Represents usage point channel type
 */
public class UsagePointChannelType implements UsagePointChannelRepresentationType {

        @Override
        public Predicate<Channel> getFilterPredicate() {
            return Channel::isRegular;
        }

        @Override
        public MessageSeeds getNoSuchElementMessageSeed() {
            return MessageSeeds.NO_SUCH_CHANNEL_FOR_USAGE_POINT;
        }
}
