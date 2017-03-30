/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

/**
 * Models an {@link IPBasedInboundComPort} that is using UDP/IP based infrastructure.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (11:18)
 */
public interface UDPBasedInboundComPort extends IPBasedInboundComPort {

    /**
     * Gets the size of the buffer that is used to communicate.
     * Setting the size of the buffer to larger values will
     * reduce the number of packets that are required to communicate
     * with the device at the cost of bigger memory footprint.
     * Note that therefore, the server will definitely
     * impose an upper-limit on the size of the buffer.
     *
     * @return The size of the buffer
     */
    public Integer getBufferSize();

    public void setBufferSize(Integer size);

    interface UDPBasedInboundComPortBuilder extends IpBasedInboundComPortBuilder<UDPBasedInboundComPortBuilder, UDPBasedInboundComPort> {
        public UDPBasedInboundComPortBuilder bufferSize(Integer bufferSize);
    }

}