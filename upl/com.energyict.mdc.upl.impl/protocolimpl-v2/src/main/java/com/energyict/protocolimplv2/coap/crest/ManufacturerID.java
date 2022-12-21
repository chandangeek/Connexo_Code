/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.coap.crest;

public enum ManufacturerID {

    Actaris((byte) 0x04),
    Falcon((byte) 0x15);

    final byte id;

    ManufacturerID(byte id) {
        this.id = id;
    }

    public static ManufacturerID forId(byte id) {
        for (ManufacturerID manufacturerID : ManufacturerID.values()) {
            if (manufacturerID.id == id) {
                return manufacturerID;
            }
        }
        return null;
    }
}
