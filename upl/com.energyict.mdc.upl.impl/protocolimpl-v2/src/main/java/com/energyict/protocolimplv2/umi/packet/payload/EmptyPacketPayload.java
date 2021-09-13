package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;

public class EmptyPacketPayload extends LittleEndianData {
    public EmptyPacketPayload() {
        super(0);
    }
}
