/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

public enum DeviceProtocolCapabilities {

    /**
     * Indication that a device can initiate its own protocol session but is not
     * necessarily also in control of the communication connection (e.g. when connected to a gateway)
     */
    PROTOCOL_SESSION,
    /**
     * Indicates that this device is able to server as a <i>Master</i> for logical slave devices
     */
    PROTOCOL_MASTER,
    /**
     * Indicates that this device is not able to create its own protocol session, but relies on his
     * <i>Master</i> to read/contact the physical device.
     */
    PROTOCOL_SLAVE;

}