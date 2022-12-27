/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;

/**
 * Models an {@link UDPInboundComPort} that is using UDPBasedInboundComPortBuilder builder.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (11:18)
 */
public interface UDPBasedInboundComPort extends UDPInboundComPort {

    interface UDPBasedInboundComPortBuilder extends UDPInboundComPort.UDPInboundComPortBuilder<UDPBasedInboundComPortBuilder, UDPBasedInboundComPort> {
    }

}