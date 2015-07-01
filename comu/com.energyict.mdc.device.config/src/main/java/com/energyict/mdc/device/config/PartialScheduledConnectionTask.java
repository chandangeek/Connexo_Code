package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.impl.PartialConnectionInitiationTaskImpl;
import com.energyict.mdc.protocol.api.ConnectionType;

import aQute.bnd.annotation.ProviderType;

/**
 * Partial version of a OutboundConnectionTask.
 *
 * @author sva
 * @since 21/01/13 - 15:49
 */
@ProviderType
public interface PartialScheduledConnectionTask extends PartialOutboundConnectionTask {

    /**
     * Gets the time window during which communication with the device
     * is allowed or <code>null</code> if the {@link ConnectionType}
     * specifies that it does not support ComWindows.
     *
     * @return The ComWindow
     */
    public ComWindow getCommunicationWindow();

    /**
     * Gets the {@link ConnectionStrategy} that calculates
     * the next time a connection will be established.
     *
     * @return The ConnectionStrategy
     */
    public ConnectionStrategy getConnectionStrategy();

    /**
     * Returns the {@link PartialConnectionInitiationTask} that will execute first
     * to initiate the connection to the device before actually connecting to it.
     *
     * @return The PartialConnectionInitiationTask that will initiate the connection to the device
     */
    public PartialConnectionInitiationTask getInitiatorTask();

    /**
     * Returns whether this PartialConnectionTask is allowed to perform simultaneous connections to the same endPoint
     *
     * @return true if simultaneous connections are allowed, false otherwise
     */
    public boolean isSimultaneousConnectionsAllowed();

    void setComWindow(ComWindow comWindow);

    void setConnectionStrategy(ConnectionStrategy connectionStrategy);

    void setAllowSimultaneousConnections(boolean allowSimultaneousConnections);

    void setInitiationTask(PartialConnectionInitiationTask partialConnectionInitiationTask);

    void setDefault(boolean asDefault);

    void setInitiationTask(PartialConnectionInitiationTaskImpl partialConnectionInitiationTask);
}
