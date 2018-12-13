/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.inbound;

/**
 * Adds behavior to {@link com.energyict.mdc.upl.InboundDeviceProtocol}.
 * that is expected by the communication platform for servlet based inbound
 * communication to detect what device is actually communicating
 * and what it is trying to say.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-21 (15:53)
 */
public interface ServletBasedInboundDeviceProtocol extends InboundDeviceProtocol, com.energyict.mdc.upl.ServletBasedInboundDeviceProtocol {
}