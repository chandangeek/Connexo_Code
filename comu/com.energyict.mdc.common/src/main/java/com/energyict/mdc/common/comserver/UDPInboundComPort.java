/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;

/**
 * Models an {@link IPBasedInboundComPort} that is using UDP/IP based infrastructure.
 */
public interface UDPInboundComPort extends IPBasedInboundComPort {

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

    interface UDPInboundComPortBuilder<B extends UDPInboundComPortBuilder<B, C>, C extends UDPInboundComPort>
            extends IpBasedInboundComPortBuilder<B, C> {
        public B bufferSize(Integer bufferSize);
    }
}