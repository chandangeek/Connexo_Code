/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.protocol;

import com.energyict.mdc.common.pluggable.Pluggable;

/**
 * Models the behavior that is expected by the ComServer for inbound
 * communication to detect what device is actually communicating
 * and what it is trying to tell.<p>
 * Currently, the discovery can work on binary data
 * or on data provided by servlet technology.
 * This component will indicate that to the ComServer
 * by returning the appropriate InputDataType.
 * When binary data is required, the component <strong>MUST</strong>
 * implement the {@link BinaryInboundDeviceProtocol}.
 * When servlet technology is used, the component <strong>MUST</strong>
 * implement the {@link ServletBasedInboundDeviceProtocol}.
 *
 * @since 2012-06-21 (13:34)
 */
public interface InboundDeviceProtocol extends Pluggable, com.energyict.mdc.upl.InboundDeviceProtocol {

}