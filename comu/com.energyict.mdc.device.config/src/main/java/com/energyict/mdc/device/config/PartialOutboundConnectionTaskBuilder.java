package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.impl.ConnectionStrategy;
import com.energyict.mdc.device.config.impl.PartialOutboundConnectionTask;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 11:42
 */
public interface PartialOutboundConnectionTaskBuilder extends PartialScheduledConnectionTaskBuilder<PartialOutboundConnectionTaskBuilder, PartialOutboundConnectionTask> {

    PartialOutboundConnectionTaskBuilder comWindow(ComWindow communicationWindow);

    PartialOutboundConnectionTaskBuilder connectionStrategy(ConnectionStrategy connectionStrategy);

    PartialOutboundConnectionTaskBuilder allowSimultaneousConnections(boolean simultaneousConnectionsAllowed);
}
