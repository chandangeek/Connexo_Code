package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.UmiCode;

public class WriteObjCmdPayload extends ReadObjRspPayload {
    public WriteObjCmdPayload(byte[] rawPayload) {
        super(rawPayload);
    }

    public WriteObjCmdPayload(UmiCode code, byte[] value) {
        super(code, value);
    }
}
