/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;

@ProviderType
public interface ValidationScope {
    ChannelsContainer getChannelsContainer();

    Map<Channel, Range<Instant>> getValidationScope();
}
