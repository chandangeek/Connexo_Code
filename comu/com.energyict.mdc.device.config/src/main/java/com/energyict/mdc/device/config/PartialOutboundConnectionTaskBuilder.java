package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.impl.PartialConnectionInitiationTaskImpl;
import com.energyict.mdc.device.config.impl.PartialScheduledConnectionTaskImpl;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 11:42
 */
public interface PartialOutboundConnectionTaskBuilder extends PartialScheduledConnectionTaskBuilder<PartialOutboundConnectionTaskBuilder, PartialScheduledConnectionTaskImpl> {

    PartialOutboundConnectionTaskBuilder comWindow(ComWindow communicationWindow);

    PartialOutboundConnectionTaskBuilder connectionStrategy(ConnectionStrategy connectionStrategy);

    PartialOutboundConnectionTaskBuilder allowSimultaneousConnections(boolean simultaneousConnectionsAllowed);

    PartialOutboundConnectionTaskBuilder asDefault(boolean asDefault);

    PartialOutboundConnectionTaskBuilder initiatonTask(PartialConnectionInitiationTaskImpl connectionInitiationTask);
}
