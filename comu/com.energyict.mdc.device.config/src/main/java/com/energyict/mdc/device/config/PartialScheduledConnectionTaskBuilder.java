package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.impl.PartialConnectionInitiationTaskImpl;
import com.energyict.mdc.device.config.impl.PartialScheduledConnectionTaskImpl;

import aQute.bnd.annotation.ProviderType;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 11:42
 */
@ProviderType
public interface PartialScheduledConnectionTaskBuilder extends PartialOutboundConnectionTaskBuilder<PartialScheduledConnectionTaskBuilder, PartialScheduledConnectionTaskImpl> {

    PartialScheduledConnectionTaskBuilder comWindow(ComWindow communicationWindow);

    PartialScheduledConnectionTaskBuilder connectionStrategy(ConnectionStrategy connectionStrategy);

    PartialScheduledConnectionTaskBuilder allowSimultaneousConnections(boolean simultaneousConnectionsAllowed);

    PartialScheduledConnectionTaskBuilder asDefault(boolean asDefault);

    PartialScheduledConnectionTaskBuilder initiationTask(PartialConnectionInitiationTaskImpl connectionInitiationTask);
}
