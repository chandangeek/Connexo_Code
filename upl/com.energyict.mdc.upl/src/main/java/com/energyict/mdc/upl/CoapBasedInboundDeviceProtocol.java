package com.energyict.mdc.upl;

import com.energyict.mdc.upl.io.CoapBasedExchange;

/**
 * Adds behavior to {@link InboundDeviceProtocol}.
 * that is expected by the communication platform for Coap inbound
 * communication to detect what device is actually communicating
 * and what it is trying to say.
 */
public interface CoapBasedInboundDeviceProtocol extends InboundDeviceProtocol {

    /**
     * Initializes this protocol, providing it with a {@link CoapBasedExchange}
     * that can be used to read the binary data that is required
     * to identify the device that is communicating.
     * Note that the protocol can use the CoapBasedExchange
     * to send answers back to the device.
     *
     * @param exchange The CoapBasedExchange
     */
    void init(CoapBasedExchange exchange);

}