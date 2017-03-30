/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.impl.PartialConnectionInitiationTaskImpl;
import com.energyict.mdc.device.config.impl.PartialScheduledConnectionTaskImpl;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface PartialScheduledConnectionTaskBuilder extends PartialOutboundConnectionTaskBuilder<PartialScheduledConnectionTaskBuilder, PartialScheduledConnectionTaskImpl> {

    PartialScheduledConnectionTaskBuilder comWindow(ComWindow communicationWindow);

    PartialScheduledConnectionTaskBuilder connectionStrategy(ConnectionStrategy connectionStrategy);

    PartialScheduledConnectionTaskBuilder setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections);

    PartialScheduledConnectionTaskBuilder asDefault(boolean asDefault);

    PartialScheduledConnectionTaskBuilder initiationTask(PartialConnectionInitiationTaskImpl connectionInitiationTask);
}
