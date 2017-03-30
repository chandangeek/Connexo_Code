/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.methods.MulticastICMethods;

import java.io.IOException;

public class MulticastIC extends AbstractCosemObject {

    private static final ObisCode DEFAULT_OBISCODE = ObisCode.fromString("0.0.128.0.23.255");

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public MulticastIC(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static ObisCode getDefaultObisCode() {
        return DEFAULT_OBISCODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.MULTICAST_IC.getClassId();
    }

    /**
     * Send a given payload to a given group of devices on the G3 network.
     * The payload will be multicasted on the G3 network as is (i.e. the user is assumed to provide the required WPDU and DLMS wrappers himself).
     */
    public void sendMulticastPacket(Structure multicastPacket) throws IOException {
        methodInvoke(MulticastICMethods.SEND_MULTICAST_PACKET, multicastPacket);
    }
}