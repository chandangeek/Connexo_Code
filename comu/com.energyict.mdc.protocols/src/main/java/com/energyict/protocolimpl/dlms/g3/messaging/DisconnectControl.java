/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.messaging;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.TypeEnum;

import java.io.IOException;

public class DisconnectControl {

    public static final ObisCode DISCONNECT_OBIS = ObisCode.fromString("0.0.96.3.10.255");

    public static final TypeEnum OPEN = new TypeEnum(0);
    public static final TypeEnum CLOSE = new TypeEnum(1);
    public static final TypeEnum ARM = new TypeEnum(2);

    private final DlmsSession session;

    public DisconnectControl(final DlmsSession session) {
        this.session = session;
    }

    /**
     * Open the contactor of the meter
     *
     * @throws java.io.IOException
     */
    public final void open() throws IOException {
        this.session.getLogger().info("Opening contactor with obisCode [" + DISCONNECT_OBIS + "] ...");
        this.session.getCosemObjectFactory().getDisconnector(DISCONNECT_OBIS).writeControlState(OPEN);
        this.session.getLogger().info("Contactor with obisCode [" + DISCONNECT_OBIS + "] successfully opened.");
    }

    /**
     * Arm the contactor of the meter
     *
     * @throws java.io.IOException
     */
    public final void arm() throws IOException {
        this.session.getLogger().info("Arming contactor with obisCode [" + DISCONNECT_OBIS + "] ...");
        this.session.getCosemObjectFactory().getDisconnector(DISCONNECT_OBIS).writeControlState(ARM);
        this.session.getLogger().info("Contactor with obisCode [" + DISCONNECT_OBIS + "] successfully armed.");
    }

    /**
     * Close the contactor of the meter
     *
     * @throws java.io.IOException
     */
    public final void close() throws IOException {
        this.session.getLogger().info("Closing contactor with obisCode [" + DISCONNECT_OBIS + "] ...");
        this.session.getCosemObjectFactory().getDisconnector(DISCONNECT_OBIS).writeControlState(CLOSE);
        this.session.getLogger().info("Contactor with obisCode [" + DISCONNECT_OBIS + "] successfully closed.");
    }

}
