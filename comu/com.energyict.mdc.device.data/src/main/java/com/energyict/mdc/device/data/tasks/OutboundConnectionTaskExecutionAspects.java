package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;

/**
 * Extends the {@link ConnectionTaskExecutionAspects} for {@link OutboundConnectionTask}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-13 (14:38)
 */
public interface OutboundConnectionTaskExecutionAspects extends ConnectionTaskExecutionAspects {

    /**
     * Creates a connection with the related device and returns
     * a {@link ComChannel} that allows to communicate
     * with the related device.
     *
     * @param comPort the used ComPort for this task
     * @return The ComChannel
     * @throws ConnectionException Indicates a failure to connect to the related device
     */
    public ComChannel connect(ComPort comPort) throws ConnectionException;

    /**
     * Terminates the connection with the device and release resources.<br></br>
     * E.g.: for modem-based connectionTypes, we should hang up the modem and release the line
     * Note: the implementer should not close the actual {@link ComChannel}, cause this is done elsewhere.
     *
     * @param comChannel the ComChannel used to communicate
     * @throws ConnectionException Indicates a failure during disconnect
     */
    public void disconnect(ComChannel comChannel) throws ConnectionException;

}