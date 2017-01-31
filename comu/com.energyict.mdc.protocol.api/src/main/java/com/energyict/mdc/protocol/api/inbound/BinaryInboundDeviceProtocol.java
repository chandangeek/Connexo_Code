/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.inbound;

import com.energyict.mdc.io.ComChannel;

/**
 * Adds behavior to {@link InboundDeviceProtocol}.
 * that is expected by the ComServer for binary inbound
 * communication to detect what device is actually communicating
 * and what it is trying to tell.<p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (08:39)
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
    public void initComChannel(ComChannel comChannel);

}