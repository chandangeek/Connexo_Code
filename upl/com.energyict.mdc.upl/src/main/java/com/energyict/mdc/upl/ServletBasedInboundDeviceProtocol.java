package com.energyict.mdc.upl;

import com.energyict.mdc.protocol.ComChannel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds behavior to {@link InboundDeviceProtocol}.
 * that is expected by the communication platform for servlet based inbound
 * communication to detect what device is actually communicating
 * and what it is trying to say.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-04 (11:55)
 */
public interface ServletBasedInboundDeviceProtocol extends InboundDeviceProtocol {

    /**
     * Initializes this protocol, providing it with a {@link ComChannel}
     * that can be used to read the binary data that is required
     * to identify the device that is communicating.
     * Note that the protocol can use the HttpServletResponse
     * to send answers back to the device.
     *
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     */
    void init (HttpServletRequest request, HttpServletResponse response);

}