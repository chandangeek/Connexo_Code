/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;

/**
 * Models an {@link IPBasedInboundComPort} that is using TCP/IP based infrastructure.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (11:18)
 */
public interface TCPBasedInboundComPort extends IPBasedInboundComPort {

    interface TCPBasedInboundComPortBuilder extends IpBasedInboundComPortBuilder<TCPBasedInboundComPortBuilder, TCPBasedInboundComPort> {
    }

}