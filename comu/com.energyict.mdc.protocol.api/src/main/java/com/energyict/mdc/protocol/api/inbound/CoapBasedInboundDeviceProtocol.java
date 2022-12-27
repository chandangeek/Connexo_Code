/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.inbound;

import com.energyict.mdc.common.protocol.InboundDeviceProtocol;

/**
 * Adds behavior to {@link com.energyict.mdc.upl.InboundDeviceProtocol}.
 * that is expected by the communication platform for servlet based inbound
 * communication to detect what device is actually communicating
 * and what it is trying to say.
 */
public interface CoapBasedInboundDeviceProtocol extends InboundDeviceProtocol, com.energyict.mdc.upl.CoapBasedInboundDeviceProtocol {
}