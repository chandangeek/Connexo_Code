/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.energyict.mdc.common.ComWindow;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface PartialScheduledConnectionTaskBuilder extends PartialOutboundConnectionTaskBuilder<PartialScheduledConnectionTaskBuilder, PartialScheduledConnectionTask> {

    PartialScheduledConnectionTaskBuilder comWindow(ComWindow communicationWindow);

    PartialScheduledConnectionTaskBuilder connectionStrategy(ConnectionStrategy connectionStrategy);

    PartialScheduledConnectionTaskBuilder setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections);

    PartialScheduledConnectionTaskBuilder asDefault(boolean asDefault);

    PartialScheduledConnectionTaskBuilder initiationTask(PartialConnectionInitiationTask connectionInitiationTask);
}
