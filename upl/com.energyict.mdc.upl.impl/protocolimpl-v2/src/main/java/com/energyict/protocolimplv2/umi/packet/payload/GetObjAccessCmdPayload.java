package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.UmiCode;

public class GetObjAccessCmdPayload extends ReadObjCmdPayload {
    public GetObjAccessCmdPayload(UmiCode umiCode) {
        super(umiCode);
    }

    public GetObjAccessCmdPayload(byte[] rawPayload) {
        super(rawPayload);
    }
}
