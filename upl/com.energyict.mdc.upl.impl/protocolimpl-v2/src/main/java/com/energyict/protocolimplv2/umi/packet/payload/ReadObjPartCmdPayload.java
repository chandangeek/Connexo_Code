package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.types.UmiObjectPart;

public class ReadObjPartCmdPayload implements IData {
    private final UmiObjectPart objectPart;

    public ReadObjPartCmdPayload(UmiObjectPart objectPart) {
        this.objectPart = objectPart;
    }

    public ReadObjPartCmdPayload(byte[] rawPayload) {
        this.objectPart = new UmiObjectPart(rawPayload);
    }

    @Override
    public byte[] getRaw() {
        return this.objectPart.getRaw();
    }

    @Override
    public int getLength() {
        return this.objectPart.getLength();
    }

    public UmiObjectPart getObjectPart() {
        return objectPart;
    }
}
