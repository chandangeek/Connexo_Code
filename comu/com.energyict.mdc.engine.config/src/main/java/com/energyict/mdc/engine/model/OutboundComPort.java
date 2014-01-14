package com.energyict.mdc.engine.model;

import com.energyict.mdc.engine.model.impl.ServerOutboundComPort;

/**
 * Models a {@link ComPort} that is dedicated to outbound communication.
 * Outbound ComPorts will sit in a {@link ComPortPool pool}
 * waiting for the communication server to start communications
 * with a device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (17:18)
 */
public interface OutboundComPort extends ComPort {

    /**
     * The maximum number of simultaneous connections.
     * If you have a need for more simultaneous connection,
     * feel free to create another outbound com port of the
     * same type and set the remaining number of connections you need.
     */
    public static final int MAXIMUM_NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 1000;

    interface OutboundComPortBuilder extends ComPort.Builder<OutboundComPortBuilder, ServerOutboundComPort>{
        public OutboundComPortBuilder numberOfSimultaneousConnections(int number);
    }

}