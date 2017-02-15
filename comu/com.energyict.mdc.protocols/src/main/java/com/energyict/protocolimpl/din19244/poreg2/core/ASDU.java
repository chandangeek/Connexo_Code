/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.core;

public enum ASDU {

    ReadRegister(140),
    RegisterResponse(130),
    WriteRegister(141),
    Continue(69),
    Disconnect(144),
    SetTime(145),
    ProfileData(147),
    ProfileDataResponse(133),
    Firmware(65),
    FirmwareResponse(1),
    SetTimeResponse(4);

    private int id;

    public int getId() {
        return id;
    }

    public byte[] getIdBytes() {
        return new byte[]{(byte) id};
    }

    ASDU(int id) {
        this.id = id;
    }
}