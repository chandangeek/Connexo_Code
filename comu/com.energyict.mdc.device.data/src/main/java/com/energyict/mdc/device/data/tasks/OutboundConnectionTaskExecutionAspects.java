package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Extends the {@link ConnectionTaskExecutionAspects} for {@link OutboundConnectionTask}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-13 (14:38)
 */
@ProviderType
public interface OutboundConnectionTaskExecutionAspects extends ConnectionTaskExecutionAspects {

    /**
     * Creates a connection with the related device and returns
     * a {@link ComChannel} that allows to communicate
     * with the related device.
     *
     * @param comPort The used ComPort for this task
     * @return The ComChannel
     * @throws ConnectionException Indicates a failure to connect to the related device
     */
    public ComChannel connect(ComPort comPort) throws ConnectionException;

    /**
     * Creates a connection with the related device and returns
     * a {@link ComChannel} that allows to communicate
     * with the related device.
     * The connection properties will be validated to be
     * original so it not possible to override properties
     * that have been configured before. Attempting to do
     * so will throw an IllegalArgumentException
     *
     * @param comPort The used ComPort for this task
     * @param properties The properties to establish the connection
     * @return The ComChannel
     * @throws ConnectionException Indicates a failure to connect to the related device
     * @throws IllegalArgumentException Thrown when the list of properties were not original
     *                                  and considered an attempt to override previously configured properties
     */
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException;

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