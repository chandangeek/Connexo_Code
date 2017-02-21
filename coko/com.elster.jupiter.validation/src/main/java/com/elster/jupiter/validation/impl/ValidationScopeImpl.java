/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.validation.ValidationScope;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;

class ValidationScopeImpl implements ValidationScope {
    private ChannelsContainer channelsContainer;
    private Map<Channel, Range<Instant>> validationScope;

    ValidationScopeImpl(ChannelsContainer channelsContainer, Map<Channel, Range<Instant>> validationScope) {
        this.channelsContainer = channelsContainer;
        this.validationScope = validationScope;
    }

    @Override
    public ChannelsContainer getChannelsContainer() {
        return channelsContainer;
    }

    @Override
    public Map<Channel, Range<Instant>> getValidationScope() {
        return validationScope;
    }
}
