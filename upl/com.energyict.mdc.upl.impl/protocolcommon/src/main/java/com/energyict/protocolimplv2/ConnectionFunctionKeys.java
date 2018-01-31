/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2;

/**
 * Enum listing up the different ConnectionFunctions.<br/>
 * Note that the enum name
 * <ul>
 * <li>should be unique</li>
 * <li>should not be altered afterwards</li>
 * </ul>
 * because the name is used as connection function translation key.
 * <br/>
 *
 * If adding new function keys, please be sure to add matching translation to com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnectionFunctionTranslationKeys
 * (of bundle mdc.protocol.pluggable)
 *
 * @author Stijn Vanhoorelbeke
 * @since 19.06.17 - 15:46
 */
public enum ConnectionFunctionKeys {

    /**
     * Indication that the connection is established to the device, using the gateway connection of the master device
     * (such as an RtuPlusServer, or a Beacon 3100).
     */
    GATEWAY,

    /**
     * Indication that the connection is established to the mirror of the data concentrator. In this case, no direct communication
     * to the downstream device is done.
     */
    MIRROR,

    /**
     * Indication that the connection is established for the inbound communication and is used for communication
     * to the downstream device.
     */
    INBOUND;
}