package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.UmiObjectPart;

public class WriteObjPartCmdPayload extends ReadObjPartRspPayload {
    public WriteObjPartCmdPayload(UmiObjectPart objectPart, byte[] value) {
        super(objectPart, value);
    }

    public WriteObjPartCmdPayload(byte[] rawPayload) {
        super(rawPayload);
    }
}
