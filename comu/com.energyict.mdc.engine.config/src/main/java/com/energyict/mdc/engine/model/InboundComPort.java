package com.energyict.mdc.engine.model;

/**
 * Models a {@link ComPort} that is dedicated to inbound communication.
 * Inbound ComPorts will wait for connection attempts against the port from a device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (17:03)
 */
public interface InboundComPort extends ComPort {

    /**
     * Gets the {@link InboundComPortPool} in which this
     * InboundComPort is contained.
     *
     * @return The InboundComPortPool
     */
    public InboundComPortPool getComPortPool();
    public void setComPortPool(InboundComPortPool comPortPool);

    /**
     * Indicate that this InboundComPort is TCP-based
     *
     * @return true if this port is an instance of {@link TCPBasedInboundComPort}, false otherwise
     */
    public boolean isTCPBased();

    /**
     * Indicate that this InboundComPort is UDP-based
     *
     * @return true if this port is an instance of {@link UDPBasedInboundComPort}, false otherwise
     */
    public boolean isUDPBased();

    /**
     * Indicate that this InboundComPort is Modem-based
     *
     * @return true if this port is an instance of {@link ModemBasedInboundComPort}, false otherwise
     */
    public boolean isModemBased();

    /**
     * Indicate that this InboundComPort is Servlet-based
     *
     * @return true if this port is an instance of {@link com.energyict.mdc.engine.model.impl.ServletBasedInboundComPort}, false otherwise
     */
    public boolean isServletBased();

}