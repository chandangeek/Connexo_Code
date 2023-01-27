/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.coap.crest;

import com.energyict.protocolimpl.utils.ProtocolTools;

public class FalconFrameSndUp implements CrestSensorConst {

    private final ManufacturerID manufacturerID;
    private final String serialNumber;

    public FalconFrameSndUp(byte[] bytes) {
        manufacturerID = ManufacturerID.forId(bytes[12]);
        serialNumber = ProtocolTools.bytesToHex(ProtocolTools.reverseByteArray(ProtocolTools.getSubArray(bytes, 7, 11)));
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public ManufacturerID getManufacturerID() {
        return manufacturerID;
    }
}
