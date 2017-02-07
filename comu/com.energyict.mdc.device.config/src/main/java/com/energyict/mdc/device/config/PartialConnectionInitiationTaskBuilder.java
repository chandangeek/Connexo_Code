/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.energyict.mdc.device.config.impl.PartialConnectionInitiationTaskImpl;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface PartialConnectionInitiationTaskBuilder extends PartialOutboundConnectionTaskBuilder<PartialConnectionInitiationTaskBuilder, PartialConnectionInitiationTaskImpl> {

}
