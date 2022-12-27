/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.coap.crest;

import com.energyict.protocolimpl.utils.ProtocolTools;

public class ActarisFrame6 implements CrestSensorConst {

    private final ManufacturerID manufacturerID;
    private final String fabricationNumber;

    public ActarisFrame6(byte[] bytes) {
        manufacturerID = ManufacturerID.forId(bytes[12]);
        fabricationNumber = ProtocolTools.bytesToHex(ProtocolTools.reverseByteArray(ProtocolTools.getSubArray(bytes, 21, 25)));
    }

    public String getFabricationNumber() {
        return fabricationNumber;
    }

    public ManufacturerID getManufacturerID() {
        return manufacturerID;
    }
}
