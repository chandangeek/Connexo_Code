package com.energyict.mdc.upl;

import com.energyict.mdc.protocol.ComChannel;

/**
 * Adds behavior to {@link InboundDeviceProtocol}.
 * that is expected by the communication platform for binary inbound
 * communication to detect what device is actually communicating
 * and what it is trying to tell us.<p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-04 (12:00)
 */
public interface BinaryInboundDeviceProtocol extends InboundDeviceProtocol {

    /**
     * Initializes this protocol, providing it with a {@link ComChannel}
     * that can be used to read the binary data that is required
     * to identify the device that is communicating.
     * Note that the protocol can use the same ComChannel
     * to send answers or additional questions/queries back
     * to the device.
     *
     * @param comChannel The ComChannel
     */
    void initComChannel(ComChannel comChannel);

}