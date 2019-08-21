/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface PartialConnectionInitiationTaskBuilder extends PartialOutboundConnectionTaskBuilder<PartialConnectionInitiationTaskBuilder, PartialConnectionInitiationTask> {
    PartialConnectionInitiationTaskBuilder setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties);
}
